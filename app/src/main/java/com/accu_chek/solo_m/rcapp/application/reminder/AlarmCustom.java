/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: AlarmCustom
 * Brief: 
 *
 * Create Date: 11/30/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import com.accu_chek.solo_m.rcapp.application.emwrservice.EMWRList;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData.UrlType;

import android.content.Context;
import android.net.Uri;

public class AlarmCustom extends AlarmFirstAlertOfDay
{

    AlarmCustom(Context context, AlarmData data) 
    {
        super(context, data);
    }

    @Override
    Uri onDBUri() 
    {
        return UrlType.reminderCustomUri;
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
		return EMWRList.EMW48322.getCodeId();
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
