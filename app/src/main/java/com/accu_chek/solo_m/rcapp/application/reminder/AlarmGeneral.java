/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: AlarmGeneral
 * Brief: 
 *
 * Create Date: 11/30/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;

import com.accu_chek.solo_m.rcapp.application.config.ConfigParameter;
import com.accu_chek.solo_m.rcapp.application.config.ReadConfig;
import com.accu_chek.solo_m.rcapp.application.safety.CRCTool;
import com.accu_chek.solo_m.rcapp.application.safety.SafetyNumber;
import com.accu_chek.solo_m.rcapp.application.safety.SafetyString;
import com.accu_chek.solo_m.rcapp.application.util.Debug;
import com.accu_chek.solo_m.rcapp.data.nugendata.DatabaseModel;
import com.accu_chek.solo_m.rcapp.data.operationhandler.AbstractReminderTable;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData;

public abstract class AlarmGeneral
{
    
    abstract Uri onDBUri();
    abstract int onChangeLayout();
    
    private static final String TAG = AlarmGeneral.class.getSimpleName();
    private Context mContext = null;
    private AlarmData mData = null;
    
    AlarmGeneral(Context context, AlarmData data)
    {
        mContext = context;
        mData = data;
    }
    
    protected void doSnoozeAction() 
    {
        long nextTirggeredTime = System.currentTimeMillis() + getSnoozeInterval();

        Debug.printI(TAG, "[Enter] doSnoozeAction()");

        activateAlarm(nextTirggeredTime);
    }

    protected void doDismissAction()
    {
        Debug.printI(TAG, "[Enter] doDismissAction()");

        // remove this alarm from active alarm list
        updateAlarmList(mContext, mData);
    }
    
    protected void doOKAction()
    {
        Debug.printI(TAG, "[Enter] doOKAction()");

        doDismissAction();
    }
    
    protected Context getContext()
    {
        return mContext;
    }
    
    protected AlarmData getAlarmData()
    {
        return mData;
    }

    protected void activateAlarm(long nextTirggeredTime)
    {
        Debug.printI(TAG, "next triggered time: " + ReminderUtils.getTimeInfo(nextTirggeredTime));

        mData.setTime(nextTirggeredTime);
        new ReminderServiceStub().setAlarm(mContext, mData);
    }

    protected AbstractReminderTable getDBRecord() 
    {
        AbstractReminderTable record = null;
        DatabaseModel model = new DatabaseModel(onDBUri());
        ArrayList<IDBData> result = model.queryData(mContext, new DBTools.QueryByAlarmCode(mData.getRequestCode()));
        
        if (result != null)
        {
            boolean isResultAbnormal = result.size() > 1;
            if (isResultAbnormal)
            {
                Debug.printI(TAG, "result.size() > 1 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

            }
            
            record = (AbstractReminderTable) result.get(0);
        }
        else
        {
            Debug.printI(TAG, "getDBRecord() is null !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        
        return record;
    }

    protected long getSnoozeInterval()
    {
        long interval = 1 * 60 * 1000;
        final String key = ConfigParameter.REMINDER_SNOOZE_TIME;
        final SafetyString cmKey = new SafetyString(key, CRCTool.generateCRC16(key.getBytes()));
        final SafetyNumber<Integer> safeValue = ReadConfig.getIntegerDataByKey(cmKey);
        if (safeValue != null)
        {
            // TODO
//            interval = safeValue.get() * 60 * 1000;
            interval = 25 * 1000;

//            Debug.printI(TAG, "snooze interval: "+ safeValue.get());
        }
        
        return interval;
    }

    // TODO Consider the cases of pressing OK button.
    protected void updateAlarmList(Context context, AlarmData data)
    {
        Debug.printI(TAG, "[Enter] updateAlarmList()");

        // remove this alarm from active alarm list
        data.delete(context);  
        
        ReminderServiceStub.showActiveAlarmInfo(context);

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
