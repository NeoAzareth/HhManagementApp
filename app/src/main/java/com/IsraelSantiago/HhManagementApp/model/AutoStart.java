package com.IsraelSantiago.HhManagementApp.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/***
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
 * Simple AutoStart for the notifications
 */
public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();
            alarm.setAlarm(context,AlarmManagerBroadcastReceiver.NEXT_ALARM);
            Log.d("AutoStart ", "----------------------Alarm set");

        }
    }
}
