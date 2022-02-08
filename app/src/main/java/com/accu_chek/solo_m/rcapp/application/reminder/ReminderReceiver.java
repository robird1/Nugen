/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: ReminderReceiver
 * Brief: 
 *
 * Create Date: 10/28/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;

import com.accu_chek.solo_m.rcapp.application.emwrservice.EMWRButtonCallback;
import com.accu_chek.solo_m.rcapp.application.emwrservice.EMWRList;
import com.accu_chek.solo_m.rcapp.application.emwrservice.NotifyMessage;
import com.accu_chek.solo_m.rcapp.application.emwrservice.NotifyProxy;
import com.accu_chek.solo_m.rcapp.application.reminder.EventObserver.AlarmType;
import com.accu_chek.solo_m.rcapp.application.util.CommonUtils;
import com.accu_chek.solo_m.rcapp.application.util.Debug;
import com.accu_chek.solo_m.rcapp.data.nugendata.DatabaseModel;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData.UrlType;

public class ReminderReceiver extends BroadcastReceiver
{

    private static final String TAG = ReminderReceiver.class.getSimpleName();
    private static final int EMWR_CODE_DOCTOR_VISIT = 48308;
    private static final int EMWR_CODE_LAB_TEST = 48309;
    private static final int EMWR_CODE_CUSTOM = 48311;
    private static final int EMWR_CODE_CHANGE_INFUSION_SET = 48301;
    private static final int EMWR_CODE_BASAL_INJECTION = 48304;
    private static final int EMWR_CODE_BG_TEST = 48302;
    private static final int EMWR_CODE_MISSED_BOLUS = 48303;
    private static final int EMWR_CODE_ALARM_CLOCK = 48310;
    private static final int EMWR_CODE_BG_AFTER_MEAL = 48307;
    private static final int EMWR_CODE_BG_AFTER_LOW = 48306;
    private static final int EMWR_CODE_BG_AFTER_HIGH = 48305;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        boolean isBootCompleted = intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED);
        boolean isAlarmReceived = intent.getAction().equals("alarm_action");
        boolean isMDIModeOn = intent.getAction().equals("com.accu_chek.solo_m.rcapp.presentation.setting.mdi.on");
        boolean isMPModeOn = intent.getAction().equals("com.accu_chek.solo_m.rcapp.presentation.setting.micropump.on");
        
        if (isBootCompleted)
        {
            Debug.printI(TAG, "Receive Intent.ACTION_BOOT_COMPLETED");

            context.startService(new Intent(context, ReminderService.class));  
            
            resetReminder(context);
            setObservers(context);

        }
        else if (isAlarmReceived)
        {
            Debug.printI(TAG, "[Enter] receive alarm action...");

            showReminderDialog(context.getApplicationContext(), intent);
        }
        else if (isMDIModeOn)
        {
            Debug.printI(TAG, "Receive the broadcast: com.accu_chek.solo_m.rcapp.presentation.setting.mdi.on");

            suspendInfusionSetReminder(context);
        }
        else if (isMPModeOn)
        {
            Debug.printI(TAG, "Receive the broadcast: com.accu_chek.solo_m.rcapp.presentation.setting.micropump.on");

            restartInfusionSetReminder(context);
        }
        else
        {
            Debug.printI(TAG, "Unknown broadcast....");
        }
        
    }

    // TODO
    private void resetReminder(Context context)
    {
//        int pumpExpiryDays = SettingUtils.getInt(context, ConfigParameter.PUMP_EXPIRY_DAYS);
//        
    }

    private void setObservers(Context context)
    {
//        Debug.printI(TAG, "[Enter] setObservers()");

        Handler handler = null;
        EventObserver observer = null;
        HandlerThread handlerThread = new HandlerThread("hyper reminder");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        observer = new EventObserver(context.getApplicationContext(), handler);
        
        context.getContentResolver().registerContentObserver(UrlType.bgUri, true, observer);
        context.getContentResolver().registerContentObserver(UrlType.logBookUri, true, observer);

    }
    
    private AlarmGeneral getAlarmInstance(Context context, AlarmData data)
    {
    	AlarmGeneral instance = null;
    	int emwrCode = data.getEMWRCode();
    	
    	Debug.printI(TAG, "getAlarmInstance(). emwrCode is " + emwrCode);
    	
    	switch (emwrCode)
    	{
    	case EMWR_CODE_DOCTOR_VISIT:
    		instance = new AlarmDoctorVisit(context, data);
    		break;
    	case EMWR_CODE_LAB_TEST:
    		instance = new AlarmLabTest(context, data);
    		break;
    	case EMWR_CODE_CUSTOM:
    		instance = new AlarmCustom(context, data);
    		break;
    	case EMWR_CODE_CHANGE_INFUSION_SET:
    	    instance = new AlarmChangeInfusionSet(context, data);
    		break;
        case EMWR_CODE_BASAL_INJECTION:
            instance = new AlarmBasalInjection(context, data);
            break;
        case EMWR_CODE_BG_TEST:
            instance = new AlarmBGTest(context, data);
            break;
        case EMWR_CODE_MISSED_BOLUS:
            instance = new AlarmMissedBolus(context, data);
            break;
        case EMWR_CODE_ALARM_CLOCK:
            instance = new AlarmClock(context, data);
            break;
        case EMWR_CODE_BG_AFTER_MEAL:
            instance = new AlarmBGAfterMeal(context, data);
            break;
        case EMWR_CODE_BG_AFTER_LOW:
            instance = new AlarmBGAfterLow(context, data);
            break;
        case EMWR_CODE_BG_AFTER_HIGH:
            instance = new AlarmBGAfterHigh(context, data);
            break;
		default:
    		// do nothing
			break;
    	}

    	return instance;
    }
    
    private void showReminderDialog(Context context, Intent intent)
    {
        AlarmData data = intent.getParcelableExtra(AlarmData.KEY_DATA);
        int emwrCode = data.getEMWRCode();
        NotifyMessage notifyMsg = setupDialogInfo(context, data, EMWRList.fromCode(emwrCode));

        NotifyProxy.showEMWR(context, notifyMsg);
    }

    private NotifyMessage setupDialogInfo(Context context, AlarmData data,
            EMWRList type)
    {
        NotifyMessage notifyMsg = new NotifyMessage(type);        
        boolean isValid = SnoozedTimesChecker.isSnoozedNumberInRange(data.getRequestCode());
        
        if (isValid)
        {
            Debug.printI(TAG, "snooze number <= max CM number");

            notifyMsg.setLeftRightButtonClickListener(new DismissButtonClick(
                    context, data), new SnoozeButtonClick(context, data));
        }
        else
        {
            // RCSWSPUI518 If the maximum number of snoozes has been performed
            // according to the SOLO M EMWR Specification, the RC shall present
            // the option to confirm the reminder as the one and only button.
            EMWRList updatedType = getUpdatedInfo(context, data);
            
            Debug.printI(TAG, "snooze number > max CM number");

            notifyMsg = new NotifyMessage(updatedType);
            notifyMsg.setCenterButtonClickListener(new OKButtonClick(context, data));
            
            // set the snoozed times to zero
            SnoozedTimesChecker.clear(data.getRequestCode());
        }
        
        return notifyMsg;
    }

    /**
     * SPEMWR25 In the [maxReminderSnoozeTimesNOM + 1] time of re-activation of
     * the reminder, the RC shall block the "RC Snooze" button usage in the
     * reminder screen for user acknowledgement. NOTE: In this case only the
     * confirmation of the reminder is possible.
     * 
     * RCSWSPUI518 If the maximum number of snoozes has been performed according
     * to the SOLO M EMWR Specification, the RC shall present the option to
     * confirm the reminder as the one and only button.
     * 
     * @param context
     * @param data
     * @return
     * @return EMWRList [out] Delete pre line return if exist. Parameter
     *         Description
     */
    private EMWRList getUpdatedInfo(Context context, AlarmData data) 
    {
		AlarmGeneral instance = getAlarmInstance(context, data);
		int confirmedCode = instance.onChangeLayout();
		return EMWRList.fromCode(confirmedCode);
	}

    /**
     * NUSWSP606 When MDI Mode is activated, the SW shall: (1) suspend the
     * change-infusion-set reminder, and (2) hide the GUI.
     * 
     */
    private void suspendInfusionSetReminder(Context context)
    {
        DatabaseModel model = new DatabaseModel(UrlType.reminderAlarmListUri);
        ArrayList<IDBData> result = model.queryData(context, null);
        
        if (result != null)
        {
            Iterator<IDBData> iterator = result.iterator();
            while (iterator.hasNext())
            {
                ReminderAlarmListTable record = (ReminderAlarmListTable) iterator.next();
                int emwrCode = CommonUtils.getOriginValue(record.getEMWRCode().getValueCH1(), record.getEMWRCode().getValueCH2());
                AlarmType type = AlarmType.fromEMWRCode(emwrCode);
                
                boolean isInfusionSetAlarm = (AlarmType.INFUSION_SET == type);

                if (isInfusionSetAlarm)
                {
                    int alarmCode = CommonUtils.getOriginValue(record.getAlarmRequestCode().getValueCH1(), record.getAlarmRequestCode().getValueCH2());
                    ReminderServiceStub serviceStub = new ReminderServiceStub();
                    AlarmData data = new AlarmData();
                    data.setEMWRCode(EMWRList.EMW48301.getCodeId());
                    data.setRequestCode(alarmCode);                    
                    serviceStub.cancelAlarm(context, data);
                }
            }
        }

    }

    private void restartInfusionSetReminder(Context context)
    {
        DatabaseModel model = new DatabaseModel(UrlType.reminderAlarmListUri);
        ArrayList<IDBData> result = model.queryData(context, null);
        
        if (result != null)
        {
            Iterator<IDBData> iterator = result.iterator();
            while (iterator.hasNext())
            {
                ReminderAlarmListTable record = (ReminderAlarmListTable) iterator.next();
                int emwrCode = CommonUtils.getOriginValue(record.getEMWRCode().getValueCH1(), record.getEMWRCode().getValueCH2());
                AlarmType type = AlarmType.fromEMWRCode(emwrCode);
                
                boolean isInfusionSetAlarm = (AlarmType.INFUSION_SET == type);

                if (isInfusionSetAlarm)
                {
                    int alarmCode = CommonUtils.getOriginValue(record.getAlarmRequestCode().getValueCH1(), record.getAlarmRequestCode().getValueCH2());
                    long time = CommonUtils.getOriginValue(record.getTime().getValueCH1(), record.getTime().getValueCH2());
                    int repeatCode = CommonUtils.getOriginValue(record.getRepeatStatus().getValueCH1(), record.getRepeatStatus().getValueCH2());
                    boolean repeatStatus = RepeatStatus.fromCode(repeatCode).getStatus(); 
                    
                    ReminderServiceStub serviceStub = new ReminderServiceStub();
                    AlarmData data = new AlarmData();
                    data.setEMWRCode(EMWRList.EMW48301.getCodeId());
                    data.setRepeatStatus(repeatStatus);
                    data.setRequestCode(alarmCode);
                    data.setTime(time);
                    serviceStub.setAlarm(context, data);
                }
            }
        }
        
    }

    private class DismissButtonClick implements EMWRButtonCallback
    {
        
        private Context mContext = null;
        private AlarmData mData = null;

        DismissButtonClick(Context context, AlarmData data)
        {
            mContext = context;
            mData = data;
        }
        
        @Override
        public void onClick()
        {
        	AlarmGeneral instance = getAlarmInstance(mContext, mData);
        	
        	SnoozedTimesChecker.clear(mData.getRequestCode());

        	instance.doDismissAction();
        }

    }
    
    
    private class SnoozeButtonClick implements EMWRButtonCallback
    {
        
        private Context mContext = null;
        private AlarmData mData = null;
        
        SnoozeButtonClick(Context context, AlarmData data)
        {
            mContext = context;
            mData = data;
        }

        @Override
        public void onClick()
        {
        	AlarmGeneral instance = getAlarmInstance(mContext, mData);
        	
        	SnoozedTimesChecker.add(mData.getRequestCode());

        	instance.doSnoozeAction();
        }
        
    }
    
    
    private class OKButtonClick implements EMWRButtonCallback
    {

        private Context mContext = null;
        private AlarmData mData = null;
        
        OKButtonClick(Context context, AlarmData data)
        {
            mContext = context;
            mData = data;
        }
        
		@Override
		public void onClick() 
		{
        	AlarmGeneral instance = getAlarmInstance(mContext, mData);
        	
        	SnoozedTimesChecker.clear(mData.getRequestCode());

        	instance.doOKAction();
		}
    	
    }

}

/*
 * ===========================================================================
 *
 * Revision history
 *  
 * ===========================================================================
 */
// [Reminder] add Reminder module
// [Reminder] add Reminder module
// [Reminder] add Reminder module
// [Reminder] update Reminder module
// [Reminder] update Reminder module
// [Reminder] update Reminder module
// [Reminder] fix bug of first alert of day alarm
