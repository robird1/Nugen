/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: EventObserver
 * Brief: 
 *
 * Create Date: 11/17/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.accu_chek.solo_m.rcapp.application.common.NugenFrameworkConstants.CommonConstants;
import com.accu_chek.solo_m.rcapp.application.emwrservice.EMWRList;
import com.accu_chek.solo_m.rcapp.application.util.CommonUtils;
import com.accu_chek.solo_m.rcapp.application.util.Debug;
import com.accu_chek.solo_m.rcapp.data.nugendata.BGTable;
import com.accu_chek.solo_m.rcapp.data.nugendata.DBProvider;
import com.accu_chek.solo_m.rcapp.data.nugendata.DatabaseModel;
import com.accu_chek.solo_m.rcapp.data.nugendata.LogBookTable;
import com.accu_chek.solo_m.rcapp.data.operationhandler.AbstractReminderTable;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData.UrlType;

public class EventObserver extends ContentObserver
{
    
    /**
     * 
     */
    private static final int DISMISS_BG_ALARM_INTERVAL = 30;

    private static final String TAG = EventObserver.class.getSimpleName();
    
    private static final UriMatcher mURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // The index for applied in UriMatcher
    private static final int I_BG_INSERT = 1;
    private static final int I_LOGBOOK = 2;

    private Context mContext = null;
    
    static
    {
        mURIMatcher.addURI(DBProvider.AUTHORITY, DBProvider.BG_PATH.concat("/#"), I_BG_INSERT);
        mURIMatcher.addURI(DBProvider.AUTHORITY, DBProvider.LOG_BOOK_PATH.concat("/#"), I_LOGBOOK);

    }

    public EventObserver(Context context, Handler handler)
    {
        super(handler);
        
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri)
    {
        Debug.printI(TAG, "[Enter] onChange()");
        Debug.printI(TAG, "[Enter] uri: " + uri);

        super.onChange(selfChange, uri);
        
        switch (mURIMatcher.match(uri))
        {
        case I_BG_INSERT :
            Debug.printI(TAG, "[Enter] I_BG_INSERT");
            Debug.printI(TAG, "bG value: " + getBGValue(uri));

            checkThreshold(getBGValue(uri));
            break;
        case I_LOGBOOK :
            Debug.printI(TAG, "[Enter] I_AFTER_MEAL");

            checkAfterMealFlag();
            
            checkBasalInjection();
            
            break;
        default :
            // do nothing
            break;
        }
    }

    private int getBGValue(Uri uri)
    {
        DatabaseModel model = new DatabaseModel(UrlType.bgUri);
        BGTable record = (BGTable) model.getLastRecord(mContext);
        int valueCH1 = record.getBgValue().getValueCH1();
        int valueCH2 = record.getBgValue().getValueCH2();
        
        return CommonUtils.getOriginValue(valueCH1, valueCH2);
    }

    private void checkThreshold(int bgValue)
    {
        // RCSWSPUI13.1 After performing a bG measurement, any "After Low bG",
        // "After High bG", "After Meal" and ¡§bG test¡¨ reminders that are set to
        // display within the next thirty minutes shall be dismissed.
        dismissBGTypeReminders();
        
        checkBGHigh(bgValue);
        checkBGLow(bgValue);
        
        restartBGTestReminder();
    }

    /**
     * RCSWSPUI14.4 RC shall restart bG test reminder if a bG test is made.
     *
     */
    private void restartBGTestReminder()
    {
        DatabaseModel model = new DatabaseModel(UrlType.reminderBGTestUri);
        ArrayList<IDBData> result = model.queryData(mContext, null);
        
        restartAlarms(result);            
    }

    /**
     * RCSWSPUI13.1 After performing a bG measurement, any "After Low bG",
     * "After High bG", "After Meal" and "bG test" reminders that are set to
     * display within the next thirty minutes shall be dismissed.
     * 
     */
    private void dismissBGTypeReminders()
    {
        DatabaseModel model = new DatabaseModel(UrlType.reminderAlarmListUri);
        ArrayList<IDBData> result = model.queryData(mContext, null);
        
        if (result != null)
        {
            Iterator<IDBData> iterator = result.iterator();
            while (iterator.hasNext())
            {
                ReminderAlarmListTable record = (ReminderAlarmListTable) iterator.next();
                int emwrCode = CommonUtils.getOriginValue(record.getEMWRCode().getValueCH1(), record.getEMWRCode().getValueCH2());
                AlarmType type = AlarmType.fromEMWRCode(emwrCode);
                
                boolean isBGTestAlarm = (AlarmType.BG_TEST == type);
                boolean isAfterMealAlarm = (AlarmType.BG_AFTER_MEAL == type);
                boolean isBGLowAlarm = (AlarmType.BG_AFTER_LOW == type);
                boolean isBGHighAlarm = (AlarmType.BG_AFTER_HIGH == type);
                boolean isBGType = (isBGTestAlarm || isAfterMealAlarm || isBGLowAlarm || isBGHighAlarm);
                
                Debug.printI(TAG, "Search for active alarm list. iterator.next()");

                if (isBGType)
                {
                    long triggerdTime = CommonUtils.getOriginValue(record.getTime().getValueCH1(), record.getTime().getValueCH2());
//                    long triggerdTime = ReminderUtils.getCalendarTime(record.getTime().getString()).getTimeInMillis();
                    long minute = TimeUnit.MILLISECONDS.toMinutes(triggerdTime - System.currentTimeMillis());
                    
                    if (minute <= DISMISS_BG_ALARM_INTERVAL)
                    {
                        int alarmCode = CommonUtils.getOriginValue(record.getAlarmRequestCode().getValueCH1(), record.getAlarmRequestCode().getValueCH2());
                        AlarmData data = new AlarmData();
                        ReminderServiceStub serviceStub = new ReminderServiceStub();
                        
                        Debug.printI(TAG, "isBGType. minute <= DISMISS_BG_ALARM_INTERVAL");
                        data.setRequestCode(alarmCode);
                        serviceStub.cancelAlarm(mContext, data);
                    }
                    else
                    {
                        Debug.printI(TAG, "isBGType. minute > 30");
                    }
                }
            }
        }
    }

    private void checkBGHigh(int bGValue)
    {
        Debug.printI(TAG, "[Enter] checkBGHigh()");

        DatabaseModel bGHighModel = new DatabaseModel(UrlType.reminderBGAfterHighUri);
        AbstractReminderTable bgHighRecord = (AbstractReminderTable) bGHighModel.getLastRecord(mContext);

        if (bgHighRecord != null)
        {
            int stateCode = CommonUtils.getOriginValue(bgHighRecord.getState().getValueCH1(), bgHighRecord.getState().getValueCH2());
            boolean isActive = State.fromCode(stateCode).getValue();
            
            if (isActive)
            {
                int highThreshold = CommonUtils.getOriginValue(bgHighRecord.getBGThreshold().getValueCH1(), bgHighRecord.getBGThreshold().getValueCH2());

                if (bGValue > highThreshold)
                {
                    int minute = CommonUtils.getOriginValue(bgHighRecord.getRemindAfterMinute().getValueCH1(), bgHighRecord.getRemindAfterMinute().getValueCH2());
                    int requestCode = CommonUtils.getOriginValue(bgHighRecord.getAlarmRequestCode().getValueCH1(), bgHighRecord.getAlarmRequestCode().getValueCH2());
                    long time = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minute);
                    
                    ReminderServiceStub seviceStub = new ReminderServiceStub();
                    AlarmData data = new AlarmData();
                    data.setEMWRCode(EMWRList.EMW48305.getCodeId());
                    data.setRepeatStatus(false);
                    data.setRequestCode(requestCode);
                    data.setTime(time);
                    seviceStub.setAlarm(mContext, data);
                }
            }
        }
        
    }

    private void checkBGLow(int bGValue)
    {
        DatabaseModel bGLowModel = new DatabaseModel(UrlType.reminderBGAfterLowUri);
        AbstractReminderTable bgLowRecord = (AbstractReminderTable) bGLowModel.getLastRecord(mContext);

        if (bgLowRecord != null)
        {
            int stateCode = CommonUtils.getOriginValue(bgLowRecord.getState().getValueCH1(), bgLowRecord.getState().getValueCH2());
            boolean isActive = State.fromCode(stateCode).getValue();
            
            if (isActive)
            {
                int lowThreshold = CommonUtils.getOriginValue(bgLowRecord.getBGThreshold().getValueCH1(), bgLowRecord.getBGThreshold().getValueCH2());

                if (bGValue < lowThreshold)
                {
                    int minute = CommonUtils.getOriginValue(bgLowRecord.getRemindAfterMinute().getValueCH1(), bgLowRecord.getRemindAfterMinute().getValueCH2());
                    int requestCode = CommonUtils.getOriginValue(bgLowRecord.getAlarmRequestCode().getValueCH1(), bgLowRecord.getAlarmRequestCode().getValueCH2());
                    long time = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minute);

                    ReminderServiceStub seviceStub = new ReminderServiceStub();
                    AlarmData data = new AlarmData();
                    data.setEMWRCode(EMWRList.EMW48306.getCodeId());
                    data.setRepeatStatus(false);
                    data.setRequestCode(requestCode);
                    data.setTime(time);
                    seviceStub.setAlarm(mContext, data);
                }
            }
        }
    }
    
    private void checkAfterMealFlag()
    {
        DatabaseModel afterMealModel = new DatabaseModel(UrlType.reminderBGAfterMealUri);
        ReminderBGAfterMealTable afterMealRecord = (ReminderBGAfterMealTable) afterMealModel.getLastRecord(mContext);
        if (afterMealRecord != null)
        {
            int stateCode = CommonUtils.getOriginValue(afterMealRecord.getState().getValueCH1(), afterMealRecord.getState().getValueCH2());
            boolean isActive = State.fromCode(stateCode).getValue();
            
            if (isActive)
            {
                DatabaseModel logBookModel = new DatabaseModel(UrlType.logBookUri);
                LogBookTable logBookRecord = (LogBookTable) logBookModel.getLastRecord(mContext);
                int mealCode = CommonUtils.getOriginValue(logBookRecord.getMealtime().getValueCH1(), logBookRecord.getMealtime().getValueCH2());
                
                if (mealCode == CommonConstants.MEAL_TIME_BEFORE_MEAL)
                {
                    int requestCode = CommonUtils.getOriginValue(afterMealRecord.getAlarmRequestCode().getValueCH1(), afterMealRecord.getAlarmRequestCode().getValueCH2());
                    long time = CommonUtils.getOriginValue(logBookRecord.getTimestamp().getValueCH1(), logBookRecord.getTimestamp().getValueCH2());
                    int afterMinute = CommonUtils.getOriginValue(afterMealRecord.getRemindAfterMinute().getValueCH1(), afterMealRecord.getRemindAfterMinute().getValueCH2());
                    long remindTime = time + TimeUnit.MINUTES.toMillis(afterMinute);
                    
                    ReminderServiceStub seviceStub = new ReminderServiceStub();
                    AlarmData data = new AlarmData();
                    data.setEMWRCode(EMWRList.EMW48307.getCodeId());
                    data.setRepeatStatus(false);
                    data.setRequestCode(requestCode);
                    data.setTime(remindTime);
                    seviceStub.setAlarm(mContext, data);
                }

            }
        }
    }
    
    private void checkBasalInjection()
    {
        DatabaseModel logBookModel = new DatabaseModel(UrlType.logBookUri);
        LogBookTable logBookRecord = (LogBookTable) logBookModel.getLastRecord(mContext);
        int basalInsulin = CommonUtils.getOriginValue(logBookRecord.getBasalInsulinMDI().getValueCH1(), logBookRecord.getBasalInsulinMDI().getValueCH2());
        
        Debug.printI(TAG, "[Enter] checkBasalInjection()");
        
        if (basalInsulin != 0)
        {
            DatabaseModel model = new DatabaseModel(UrlType.reminderBasalInjectionUri);
            ArrayList<IDBData> result = model.queryData(mContext, null);
            
            restartAlarms(result);
        }
    }

    private void restartAlarms(ArrayList<IDBData> queryResult)
    {
        if (queryResult != null)
        {
            Iterator<IDBData> iterator = queryResult.iterator();
            while (iterator.hasNext())
            {
                AbstractReminderTable record = (AbstractReminderTable) iterator.next();
                int stateCode = CommonUtils.getOriginValue(record.getState().getValueCH1(), record.getState().getValueCH2());
                boolean isActive = State.fromCode(stateCode).getValue();
                
                if (isActive)
                {
                    int requestCode = CommonUtils.getOriginValue(record.getAlarmRequestCode().getValueCH1(), record.getAlarmRequestCode().getValueCH2());
                    int emwrCode = CommonUtils.getOriginValue(record.getEMWRCode().getValueCH1(), record.getEMWRCode().getValueCH2());
                    int repeatCode = CommonUtils.getOriginValue(record.getRepeatStatus().getValueCH1(), record.getRepeatStatus().getValueCH2()); 
                    boolean repeatStatus = RepeatStatus.fromCode(repeatCode).getStatus(); 
                    String sTime = record.getTime().getString();
                    long time = ReminderUtils.getCalendarTime(sTime).getTimeInMillis();

                    ReminderServiceStub seviceStub = new ReminderServiceStub();
                    AlarmData data = new AlarmData();
                    data.setEMWRCode(emwrCode);
                    data.setRepeatStatus(repeatStatus);
                    data.setRequestCode(requestCode);
                    data.setTime(time);
                    seviceStub.setAlarm(mContext, data);
                }
            }
        }
    }

    
    enum AlarmType
    {
        INFUSION_SET(UrlType.reminderInfusionSetUri, 48301),
        BASAL_INJECTION(UrlType.reminderBasalInjectionUri, 48304),
        ALARM_CLOCK(UrlType.reminderAlarmClockUri, 48310),
        BG_TEST(UrlType.reminderBGTestUri, 48302),
        BG_AFTER_MEAL(UrlType.reminderBGAfterMealUri, 48307),
        BG_AFTER_LOW(UrlType.reminderBGAfterLowUri, 48306),
        BG_AFTER_HIGH(UrlType.reminderBGAfterHighUri, 48305),
        MISSED_BOLUS(UrlType.reminderMissedBolusUri, 48303),
        DOCTOR_VISIT(UrlType.reminderDoctorVisitUri, 48308),
        LAB_TEST(UrlType.reminderLabTestUri, 48309),
        CUSTOM(UrlType.reminderLabTestUri, 48311);
        
        private Uri mUri = null;
        private int mCode = 0;
        
        AlarmType(Uri uri, int emwrCode)
        {
            mUri = uri;
            mCode = emwrCode;
        }
        
        DatabaseModel getDBModel()
        {
            return new DatabaseModel(mUri);
        }
        
        int getEMWRCode()
        {
            return mCode;
        }
        
        static AlarmType fromEMWRCode(int code)
        {
            AlarmType type = AlarmType.CUSTOM;
            
            for (AlarmType t : AlarmType.values())
            {
                int temp = t.getEMWRCode();
                
                if (temp == code)
                {
                    type = t;
                    break;
                }
            }
            
            return type;
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
// [Reminder] update Reminder module
// [Reminder] update Reminder module
// [Reminder] update Reminder module
