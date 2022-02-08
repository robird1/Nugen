/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: AlarmChangeInfusionSet
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
import android.net.Uri;

import com.accu_chek.solo_m.rcapp.application.emwrservice.EMWRList;
import com.accu_chek.solo_m.rcapp.application.util.CommonUtils;
import com.accu_chek.solo_m.rcapp.data.operationhandler.AbstractReminderTable;
import com.accu_chek.solo_m.rcapp.data.operationhandler.IDBData.UrlType;

public class AlarmChangeInfusionSet extends AlarmRepeat
{

    AlarmChangeInfusionSet(Context context, AlarmData data)
    {
        super(context, data);
        // TODO Auto-generated constructor stub
    }

    @Override
    Uri onDBUri() 
    {
        return UrlType.reminderInfusionSetUri;
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
		return EMWRList.EMW48312.getCodeId();
	}

    @Override
    protected long onRepeatInterval()
    {
        AbstractReminderTable record = getDBRecord();
        int hourInterval = CommonUtils.getOriginValue(record.getInterval().getValueCH1(), record.getInterval().getValueCH2());
        
        return TimeUnit.HOURS.toMillis(hourInterval);
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
