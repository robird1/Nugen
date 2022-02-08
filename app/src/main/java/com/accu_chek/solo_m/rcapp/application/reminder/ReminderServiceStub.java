/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: ReminderServiceStub
 * Brief: 
 *
 * Create Date: 10/29/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.accu_chek.solo_m.rcapp.application.reminder.EventObserver.AlarmType;
import com.accu_chek.solo_m.rcapp.application.util.CommonUtils;
import com.accu_chek.solo_m.rcapp.application.util.Debug;
import com.accu_chek.solo_m.rcapp.data.nugendata.DatabaseModel;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData.UrlType;

public class ReminderServiceStub extends IReminderAlarm.Stub
{
    
    private static final String TAG = ReminderServiceStub.class.getSimpleName();
    private Context mContext = null;
    
    public void setContext(Context context)
    {
        mContext = context;
    }
    
    @Override
    public void set(AlarmData data) throws RemoteException
    {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = null;
        Intent intent = new Intent("alarm_action");
        
        Debug.printI(TAG, "[Enter] ReminderServiceStub.set()");

        intent.putExtra("alarm_data", data);
        pi = PendingIntent.getBroadcast(mContext, data.getRequestCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        if (data.getRepeatStatus() == true)
        {
            am.setRepeating(AlarmManager.RTC_WAKEUP, data.getTime(), data.getRepeatInterval(), pi);
            
            Debug.printI(TAG, "Set repeatedly alarm at: " + ReminderUtils.getTimeInfo(data.getTime()));
            Debug.printI(TAG, "Repeat interval is: " + TimeUnit.MILLISECONDS.toDays(data.getRepeatInterval()));

        }
        else
        {
            am.set(AlarmManager.RTC_WAKEUP, data.getTime(), pi);
            
            Debug.printI(TAG, "Set once alarm at: " + ReminderUtils.getTimeInfo(data.getTime()));

        }
        
        updateAlarmList(data, true);

    }

    // TODO Replace integer argument with AlarmData argument ? 
    @Override
    public void cancel(AlarmData data) throws RemoteException
    {
        Debug.printI(TAG, "[Enter] ReminderServiceStub.cancel()");

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("alarm_action");
        int alarmId = data.getRequestCode();
        PendingIntent operation = PendingIntent.getBroadcast(mContext,
                alarmId, intent, PendingIntent.FLAG_NO_CREATE);
        if (operation != null)
        {
            Debug.printI(TAG, "operation != null");

            PendingIntent pi = PendingIntent.getBroadcast(mContext, alarmId, intent, 0);
            am.cancel(pi);
        }
        
        updateAlarmList(data, false);
        
        SnoozedTimesChecker.clear(alarmId);
    }

    public void setAlarm(Context context, AlarmData data)
    {
        mContext = context;
        
        try
        {
            set(data);
        }
        catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void cancelAlarm(Context context, AlarmData data)
    {
        mContext = context;
        
        try
        {
            cancel(data);
        }
        catch (RemoteException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateAlarmList(AlarmData data, boolean isActivateAlarm)
    {
        DatabaseModel model = new DatabaseModel(UrlType.reminderAlarmListUri);
        ArrayList<IDBData> queryResult = model.queryData(mContext, new DBTools.QueryByAlarmCode(data.getRequestCode()));

        if (isActivateAlarm)
        {
            if (queryResult == null)
            {
                data.store(mContext);
                
                Debug.printI(TAG, "data.store()");
            }
            else
            {
                data.update(mContext, data.getTime());
                
                Debug.printI(TAG, "data.update()");
            }
        }
        else   // The case of cancel alarm
        {
            int emwrCode = data.getEMWRCode();
            boolean isInfusionSetAlarm = (emwrCode == AlarmType.INFUSION_SET.getEMWRCode());
            
            if (!isInfusionSetAlarm)
            {
                int deleteCount = model.deleteData(mContext, new DBTools.DeleteByAlarmCode(data.getRequestCode()));
                
                Debug.printI(TAG, "deletedCount: " + deleteCount);
            }
            else
            {
                // Suspend but not delete the change infusion set alarm
                Debug.printI(TAG, "Suspend but not delete the change infusion set alarm...");
            }
        }
        
        showActiveAlarmInfo(mContext);
        
    }
    
    /**
     * For the purpose of debugging.
     *
     * @return None
     */
    public static void showActiveAlarmInfo(Context context)
    {
        DatabaseModel model = new DatabaseModel(UrlType.reminderAlarmListUri);
        ArrayList<IDBData> queryResult = model.queryData(context, null);

        Debug.printI(TAG, " ");
        Debug.printI(TAG, "[Enter] show info of active alarm list...");
        Debug.printI(TAG, " ");

        if (queryResult != null)
        {
            Iterator<IDBData> iterator = queryResult.iterator();
            while (iterator.hasNext())
            {
                ReminderAlarmListTable record = (ReminderAlarmListTable) iterator.next();
                int emwrCode = CommonUtils.getOriginValue(record.getEMWRCode().getValueCH1(), record.getEMWRCode().getValueCH2());
                int alarmCode = CommonUtils.getOriginValue(record.getAlarmRequestCode().getValueCH1(), record.getAlarmRequestCode().getValueCH2());
                long time = CommonUtils.getOriginValue(record.getTime().getValueCH1(), record.getTime().getValueCH2());
                String date = ReminderUtils.getTimeInfo(time);
                
                Debug.printI(TAG, "EMWR code: " + emwrCode + " Alarm code: " + alarmCode + " Triggered Time: " + date);
            }
        }
        else
        {
            Debug.printI(TAG, "No active alarms.........");
        }
        Debug.printI(TAG, " ");

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
// [Reminder] 1. add comment 2. coding rule
