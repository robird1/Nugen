/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: AlarmRepeat
 * Brief: 
 *
 * Create Date: 11/30/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.accu_chek.solo_m.rcapp.application.util.Debug;
import com.accu_chek.solo_m.rcapp.data.operationhandler.AbstractReminderTable;

public abstract class AlarmRepeat extends AlarmGeneral
{

    private static final String TAG = AlarmRepeat.class.getSimpleName();
    private Context mContext = null;
    private AlarmData mData = null;

    AlarmRepeat(Context context, AlarmData data)
    {
        super(context, data);
        mContext = context;
        mData = data;
    }

    @Override
    protected void doDismissAction()
    {
        boolean isRepeat = mData.getRepeatStatus();

        Debug.printI(TAG, "[Enter] doDismissAction()");
        
        if (isRepeat)
        {
            mData.update(mContext, getNextTriggerTime());
        }
        else
        {
            super.doDismissAction();  
            
            Debug.printI(TAG, "super.doDismissAction()");
        }

    }
    
    protected long onRepeatInterval()
    {
        return TimeUnit.DAYS.toMillis(1);
    }
    
    private long getNextTriggerTime()
    {
        long newTime = 0;
        AbstractReminderTable record = getDBRecord();
        
        if (record != null)
        {
            long baseTime = ReminderUtils.getCalendarTime(record.getTime().getString()).getTimeInMillis();
           
            newTime = baseTime + onRepeatInterval();

        }
        
        Debug.printI(TAG, "Set repeatedly alarm. Next triggered time is: " + ReminderUtils.getTimeInfo(getNextTriggerTime()));

        return newTime;
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
