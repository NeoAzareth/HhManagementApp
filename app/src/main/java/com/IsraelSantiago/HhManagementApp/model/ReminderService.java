package com.IsraelSantiago.HhManagementApp.model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.SharedPreferences;
import android.util.Log;
import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.controller.LoginActivity;
import com.IsraelSantiago.HhManagementApp.controller.ManageBillsActivity;

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
 * ReminderService.class
 *
 * Modification of the code from website below to run on this app. Displays the add your bills
 * reminder notification.
 *
 * Credits to Jonathan Hasenzahl, James Celona, Dhimitraq Jorgji
 * https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/
 *
 * @author Israel Santiago
 * @version 1.0
 */

public class ReminderService extends WakeIntentService {

    //instance fields
    Calendar calendar;
    SimpleDateFormat dateFormat;
    Intent intent;
    private Integer userID;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public ReminderService() {
        super("ReminderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
        Log.d("Reminder service ","Started");
        SharedPreferences sharedPreferences = getSharedPreferences("SavedValues",MODE_PRIVATE);
        userID = sharedPreferences.getInt("userID",0);


        AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();

        Log.d("Reminder Service ", "user id: " +userID);
        if (!userID.equals(0)) {

            switch (getUserStatus()) {
                case"not done":
                    String contextText;
                    String messageText;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);

                    if (day == 1 && hour == 9) {
                        contextText = "Another day, another month... ";
                        messageText = "you may now enter your bills for the next period!\n" +
                                dateFormat.format(calendar.getTime());
                    } else {
                        contextText = "Friendly reminder!";
                        messageText = "Add your bills under the \"Manage bills\" feature or click the " +
                                "\"Yes I'm done\" button to stop receiving these notifications...\n"+
                        dateFormat.format(calendar.getTime());
                    }

                    displayNotification(contextText,messageText);
                    //set next alarm
                    alarm.setAlarm(this,AlarmManagerBroadcastReceiver.NEXT_ALARM);
                    break;
                case "done":
                    alarm.setAlarm(this,AlarmManagerBroadcastReceiver.RESET_ALARM);
                    break;
                default:
                    alarm.cancelAlarm(this);
                    break;
            }
        }

        super.onHandleIntent(intent);
    }

    /***
     * displayNotification()
     *
     * Display the notification. contextText the short form of the notification and messageText is the
     * complete form.
     *
     * @param contextText string
     * @param messageText string
     */
    private void displayNotification(String contextText, String messageText){
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (DataHolder.getInstance().getMember()!= null) {
            intent = new Intent(this, ManageBillsActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_CANCEL_CURRENT);


        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.household_management_logo_24)
                .setContentTitle("Household Management")
                .setContentIntent(pendingIntent)
                .setContentText(contextText)
                .setAutoCancel(true)
                .setStyle(new Notification.BigTextStyle().bigText(messageText))
                .build();

        final int NOTIFICATION_ID = 1;
        notificationManager.notify(NOTIFICATION_ID,notification);
    }

    /***
     * getUserStatus()
     *
     * queries a php script for the user status given a user has successfully login and its userID
     * was saved into the shared preferences.
     *
     * @return string
     */
    private String getUserStatus(){
        String data = Utilities.encodeData(ServerStrings.SERVICE_KEY,String.valueOf(userID));
        return new DBConnection().dbTransaction(ServerStrings.SERVICE,data);
    }
}
