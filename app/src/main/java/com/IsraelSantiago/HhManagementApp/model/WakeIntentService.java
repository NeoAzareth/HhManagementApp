package com.IsraelSantiago.HhManagementApp.model;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Original work by Seattle Central College students
 * Team members:
 * Colin Lin, colinhx@gmail.com
 * Sicheng Zhu, szhu0007@seattlecentral.edu
 * Israel Santiago, neoazareth@gmail.com
 *
 * GitHub link to previous version:
 * https://github.com/sicheng-zhu/HouseholdManagement
 *
 * Original idea Android App translation of Israel Santiago's Household Management Webb App
 * Link to Webb App https://neoazareth.com/HHManageWebApp/index.php
 *
 *
 * WakeIntentService.class
 *
 * An intend service used to wake and acquire a lock on the cpu... I think..
 *
 * Credits to Jonathan Hasenzahl, James Celona, Dhimitraq Jorgji
 * https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/
 */

public class WakeIntentService extends IntentService {


    public static final String LOCK_NAME_STATIC
            = "com.IsraelSantiago.HhManagementApp.model.ReminderService.Static";
    public static final String LOCK_NAME_LOCAL
            = "com.IsraelSantiago.HhManagementApp.model.ReminderService.Local";

    private static PowerManager.WakeLock wakeLockStatic = null;
    private PowerManager.WakeLock wakeLockLocal = null;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WakeIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        wakeLockLocal.release();

    }
    public static void acquireStaticLock(Context context){
        getLock(context).acquire(600000);
    }

    synchronized private static PowerManager.WakeLock getLock(Context context) {
        if(wakeLockStatic == null) {
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            wakeLockStatic = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,LOCK_NAME_STATIC);
            wakeLockStatic.setReferenceCounted(true);
        }
        return(wakeLockStatic);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLockLocal = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,LOCK_NAME_LOCAL);
        wakeLockLocal.setReferenceCounted(true);
    }

    @Override
    public void onStart(Intent intent, final int startId) {
        wakeLockLocal.acquire(600000);
        super.onStart(intent,startId);
        getLock(this).release();
    }
}
