/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: AlarmBGAfterMeal
 * Brief: 
 *
 * Create Date: 2015/11/30
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import com.accu_chek.solo_m.rcapp.application.emwrservice.EMWRList;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData.UrlType;

import android.content.Context;
import android.net.Uri;

public class AlarmBGAfterMeal extends AlarmGeneral
{

    AlarmBGAfterMeal(Context context, AlarmData data)
    {
        super(context, data);
        // TODO Auto-generated constructor stub
    }

    @Override
    Uri onDBUri()
    {
        return UrlType.reminderBGAfterMealUri;
    }
    
    /**
     * RCSWSPUI518 If the maximum number of snoozes has been performed according
     * to the SOLO M EMWR Specification, the RC shall present the option to
     * confirm the reminder as the one and only button.
     * 
     * 
     * @return
     */
	@Override
	int onChangeLayout() 
	{
		return EMWRList.EMW48318.getCodeId();
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
