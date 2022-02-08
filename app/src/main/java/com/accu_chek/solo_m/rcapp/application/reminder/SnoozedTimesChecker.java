/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: SnoozedTimesChecker
 * Brief: 
 *
 * Create Date: 12/1/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.accu_chek.solo_m.rcapp.application.config.ConfigParameter;
import com.accu_chek.solo_m.rcapp.application.config.ReadConfig;
import com.accu_chek.solo_m.rcapp.application.safety.SafetyString;
import com.accu_chek.solo_m.rcapp.application.setting.generalsetting.SettingUtils;
import com.accu_chek.solo_m.rcapp.application.util.Debug;

public final class SnoozedTimesChecker
{

    private static final String TAG = SnoozedTimesChecker.class.getSimpleName();

    private static HashMap<Integer, Integer> mSnoozeNumberMap = new HashMap<Integer, Integer>();

    private SnoozedTimesChecker()
    {
        // Empty constructor. To indicate this is a utility class.
    }
    
    /**
     * SPEMWR24 The RC shall limit the amount of possible successive snooze
     * activations for the same reminder to maxReminderSnoozeTimesNOM.
     * 
     * @param alarmCode
     * @return
     * @return boolean [out] Delete pre line return if exist. Parameter
     *         Description
     */
    static boolean isSnoozedNumberInRange(int alarmCode)
    {
        boolean result = true;
        Integer entry = mSnoozeNumberMap.get(alarmCode);
        if (entry != null)
        {
            int snoozeNumber = entry.intValue();
            int maxSnoozeNumber = getMaxSnoozeNumber();
            
            if (snoozeNumber >= maxSnoozeNumber)
            {
                result = false;
            }
        }
        else
        {
//            Debug.printI(TAG, "no record in mSnoozeNumberMap...");
        }
        
        return result;
    }

    static void add(int alarmCode)
    {
        int snoozeNumber = 0;
        Integer result = mSnoozeNumberMap.get(alarmCode);
        
        if (result != null)
        {
            snoozeNumber = result.intValue() + 1;
        }
        else
        {
            snoozeNumber = 1;
        }
        mSnoozeNumberMap.put(alarmCode, snoozeNumber);
        
        showInfo();
    }

    static void clear(int alarmCode)
    {
        mSnoozeNumberMap.put(alarmCode, 0);
        
        showInfo();
    }

    static void showInfo()
    {
        Iterator<Map.Entry<Integer, Integer>> iterator = mSnoozeNumberMap.entrySet().iterator();
        
        Debug.printI(TAG, " ");
        Debug.printI(TAG, "[Enter] show info of snooze number...");
        Debug.printI(TAG, " ");

        while (iterator.hasNext())
        {
            Entry<Integer, Integer> entry = (Entry<Integer, Integer>) iterator.next();
            Debug.printI(TAG, "alarm request code: " + entry.getKey() + " snooze times: " + entry.getValue());
        }
        Debug.printI(TAG, " ");
    }
    
    private static int getMaxSnoozeNumber()
    {
        SafetyString cmKey = SettingUtils.convertSafeString(ConfigParameter.MAX_SNOOZE_TIMES);
        int maxNumber = ReadConfig.getIntegerDataByKey(cmKey).get();
                
//        Debug.printI(TAG, "max snooze number is: " + maxNumber);
        
        return maxNumber;
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
// [Reminder] 1. add comment 2. coding rule
