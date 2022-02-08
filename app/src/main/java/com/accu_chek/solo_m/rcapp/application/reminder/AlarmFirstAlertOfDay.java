/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: AlarmFirstAlertOfDay
 * Brief: 
 *
 * Create Date: 11/30/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import java.util.Calendar;

import android.content.Context;

import com.accu_chek.solo_m.rcapp.application.util.Debug;
import com.accu_chek.solo_m.rcapp.data.operationhandler.AbstractReminderTable;

public abstract class AlarmFirstAlertOfDay extends AlarmGeneral
{
    
    private static final String TAG = AlarmFirstAlertOfDay.class.getSimpleName();
    private Context mContext = null;
    private AlarmData mData = null;
    
    AlarmFirstAlertOfDay(Context context, AlarmData data)
    {
        super(context, data);
        mContext = context;
        mData = data;
    }

    @Override
    public void doSnoozeAction()
    {
        AbstractReminderTable record = getDBRecord();
        
        Debug.printI(TAG, "[Enter] doSnoozeAction()");

        if (record != null)
        {
            long specifiedTime = getAlarmSpecifiedTime(record);
            boolean isSnoozedTimeValid = (System.currentTimeMillis() + getSnoozeInterval()) < specifiedTime;
            long nextTirggeredTime = 0;

            if (isSnoozedTimeValid)
            {
                Debug.printI(TAG, "isSnoozedTimeValid == true");

                nextTirggeredTime = System.currentTimeMillis() + getSnoozeInterval();
            }
            else
            {
                Debug.printI(TAG, "isSnoozedTimeValid == false");

                nextTirggeredTime = specifiedTime;
            }
            
            Debug.printI(TAG, "next triggered time: " + ReminderUtils.getTimeInfo(nextTirggeredTime));

            super.activateAlarm(nextTirggeredTime);
        }
    }
    
    @Override
    public void doDismissAction() 
    {
        AbstractReminderTable record = getDBRecord();
        
        Debug.printI(TAG, "[Enter] doDismissAction()");

        if (record != null)
        {
            long specifiedTime = getAlarmSpecifiedTime(record);
            boolean isSpecifiedTimeConfirm = System.currentTimeMillis() > specifiedTime;
            if (isSpecifiedTimeConfirm)
            {
                Debug.printI(TAG, "isSpecifiedTimeConfirm == true");

                // remove this alarm from active alarm list
                super.updateAlarmList(mContext, mData);
                
            }
            else    // confirmation from first alert of day
            {
                Debug.printI(TAG, "isSpecifiedTimeConfirm == false");

                super.activateAlarm(specifiedTime);
            }
        }           
        
    }

    private long getAlarmSpecifiedTime(AbstractReminderTable record) 
    {
        String date = record.getDate().getString();
        int year = ReminderUtils.getYear(date);
        int month = ReminderUtils.getMonth(date) - 1;
        int day = ReminderUtils.getDay(date);
        String time = record.getTime().getString();
        Calendar temp = ReminderUtils.getCalendarTime(time);
        int hourOfDay = temp.get(Calendar.HOUR_OF_DAY);
        int minute = temp.get(Calendar.MINUTE);
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, hourOfDay, minute);
        return c.getTimeInMillis();
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
// [Reminder] fix bug of first alert of day alarm
