/** 
 * ===========================================================================
 * Copyright 2015 Roche Diagnostics GmbH
 * All Rights Reserved
 * ===========================================================================
 *
 * Class name: ReminderService
 * Brief: 
 *
 * Create Date: 10/28/2015
 * $Revision: $
 * $Author: $
 * $Id: $
 */

package com.accu_chek.solo_m.rcapp.application.reminder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ReminderService extends Service
{
    
    private static final String TAG = "ReminderService";
    
    private ReminderServiceStub mServiceStub = new ReminderServiceStub();
    
    @Override  
    public void onCreate() 
    {  
        super.onCreate();  
        Log.d(TAG, "onCreate() executed");
        mServiceStub.setContext(getApplicationContext());
    }  
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) 
    {  
        Log.d(TAG, "onStartCommand() executed");  
        return super.onStartCommand(intent, flags, startId);  
    }  
      
    @Override  
    public void onDestroy() 
    {  
        super.onDestroy();  
        Log.d(TAG, "onDestroy() executed");  
    }  
    
    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "onBind() executed");  

        return mServiceStub;
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
