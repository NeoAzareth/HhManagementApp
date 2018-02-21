package com.IsraelSantiago.HhManagementApp.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
 * AlarmManagerBroadcastReceiver.class
 *
 * A broadcast receiver that manages the alarm for the notifications
 *
 * Credits to Rakesh Cusat(I believe he is the Author, if not I apologize...)
 * https://www.javacodegeeks.com/2012/09/android-alarmmanager-tutorial.html
 *
 * As well as Jonathan Hasenzahl, James Celona, Dhimitraq Jorgji
 * https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/
 */

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    //instance fields
    private Calendar calendar;
    public static final int NEXT_ALARM = 0;
    public static final int RESET_ALARM = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Alarm On receive"," Called");
        WakeIntentService.acquireStaticLock(context);

        context.startService(new Intent(context,ReminderService.class));
    }

    /***
     * setAlarm()
     *
     * method that sets the next alarm. parameter mode could have the value of 0 or 1, NEXT_ALARM and
     * RESET_ALARM respectively
     *
     * @param context application context
     * @param mode integer that could be 0 or 1,
     */
    public void setAlarm(Context context,int mode) {
        //cancel previous alarm
        cancelAlarm(context);
        //gets an instance of a calendar to see the time
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        //depending of the mode calls a reset alarm or set alarm
        if (mode == NEXT_ALARM) {
            setNextAlarm();
        } else if(mode == RESET_ALARM){
            setResetAlarm();
        }
        Log.d("Next alarm", simpleDateFormat.format(calendar.getTime()));

        long delay = calendar.getTimeInMillis();

        //long delay = 1000 * 15;

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,AlarmManagerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, delay,pendingIntent);
        Log.d("Alarm set", "--------------------------");
    }

    /***
     * cancelAlarm()
     *
     * Cancels any previous alarm related to this notification type
     * @param context application context
     */
    public void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context,0,intent,0);
        alarmManager.cancel(sender);
    }




    /***
     * getNextHourToRun)()
     *
     * takes a calendar object and determines the next hour to run. Currently the notifications are
     * set to run more aggressively as the month advances.
     * The 1st and 2nd of each month the app only notifies the user at 9 am and 9pm.
     * The 3rd and 4th: the notifications happen at 9am, 3pm and 9pm
     * The 5th and 6th: at 9am, 12pm, 3pm, 6pm and 9pm
     * from then until the 28th the notifications occur every hour from 9am to 9pm
     *
     * @param calendar calendar object
     * @return integer a number that represent the next hour to run
     */
    private Integer getNextHourToRun(Calendar calendar){
        int[] listHoursOneAndTwo = {9,21};
        int[] listHoursThreeAndFour = {9,15,21};
        int[] listHoursFiveAndSix = {9,12,15,18,21};

        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfTheDay = calendar.get(Calendar.HOUR_OF_DAY);

        switch (dayOfMonth) {
            case 1:
            case 2:
                for (int hour: listHoursOneAndTwo
                        ) {
                    if (hourOfTheDay<hour){
                        return hour;
                    }

                }
                break;
            case 3:
            case 4:
                for (int hour: listHoursThreeAndFour
                     ) {
                    if (hourOfTheDay<hour) {
                        return hour;
                    }

                }
                break;
            case 5:
            case 6:
                for (int hour: listHoursFiveAndSix
                     ) {
                    if (hourOfTheDay<hour) {
                        return hour;
                    }

                }
                break;
            default:
                if (hourOfTheDay<21) {
                    return hourOfTheDay+1;
                } else {
                    return 9;
                }
        }
        return 9;
    }

    /* ------------------------------------------------------------market for deletion

    /***
     * getNextDayToRun()
     *
     * takes a calendar object and returns the next day to run.
     *
     * @param calendar calendar object
     * @return integer that represent the next day to run
     *
    private Integer getNextDayToRun(Calendar calendar){

        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfTheDay = calendar.get(Calendar.HOUR_OF_DAY);

        if(dayOfMonth >= 29 || (dayOfMonth == 28 && hourOfTheDay >= 21)) {
            return 1;
        } else if (dayOfMonth < 28 && hourOfTheDay >= 21) {
            return dayOfMonth+1;
        } else {
            return dayOfMonth;
        }
    }

    /***
     * getNextMonthToRun()
     *
     * Takes a Calendar object and returns an integer that represents the next month to run.
     *
     * @param calendar calendar object
     * @return integer that represents the next month to run
     *
    private Integer getNextMonthToRun(Calendar calendar) {

        int hourOfTheDay = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = calendar.get(Calendar.MONTH);

        if (dayOfMonth >= 29 || (dayOfMonth == 28 && hourOfTheDay >= 21)) {
            if(monthOfYear == 12) {
                return 1;
            }
            return monthOfYear+1;
        }

        return monthOfYear;
    }

    /***
     * getNextYearToRun()
     *
     * takes a Calendar object and returns an integer that represents the next year to run.
     *
     * @param calendar Calendar object
     * @return integer that represent the next year to run
     *
    private Integer getNextYearToRun(Calendar calendar) {
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        if (monthOfYear == 12 && dayOfMonth >= 28 && hourOfDay >= 21) {
            return year+1;
        } else {
            return year;
        }

    }*/

    /***
     * setDayMonthAndYearToRun()
     *
     * Takes a Calendar object and sets its day, month and year based on the following schedule:
     *
     * Day: giving the hour is after 9pm (21 integer) set the day to next day. To make it easier to
     * code, the application only sends notifications the first 28 days of each month. Therefore, it
     * resets the day to 1 (first of the month) giving the time of the 28th day is pass 9pm. Any other
     * instance, it assumes the day has not ended and will not modify the calendar at all.
     *
     * Month: Using the same condition when the days resets to 1st of the month, it also resets the
     * month to next (current +1); However, before doing so, checks if the month is the last of the
     * year in which case it will reset the month from 12 (December) to 1 (January). If any of the
     * previous conditions does not happen it will leave the calendar unchanged.
     *
     * Year: Again, by recycling the reset to 1 (January) condition used by the month it will also
     * change the month to next year. any other instance it will leave the year unchanged.
     *
     * e.g:
     * giving the calendar has values of 2017/12/28 10:00 pm
     * it should change the calendar to values to 2018/1/1
     *
     * time is further modify by the getNextHourToRun() method
     *
     * @param calendar calendar object
     * @return the same calendar with possible different values.
     */
    private Calendar setDayMonthAndYearToRun(Calendar calendar) {

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        if (dayOfMonth >= 29 || (dayOfMonth == 28 && hourOfDay >= 21)){
            calendar.set(Calendar.DAY_OF_MONTH,1);//day reset to 1
            if (monthOfYear == 12) {
                calendar.set(Calendar.MONTH,1);//month reset to 1
                calendar.set(Calendar.YEAR,year+1);//year set to next year
            } else {
                calendar.set(Calendar.MONTH,monthOfYear+1);//month set to next month
            }
        } else if (dayOfMonth < 28 && hourOfDay >= 21) {
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth+1);//day set to next day
        } else {
            /*the calendar is left untouched, if none of the previous conditions took place this part
            assumes the next notification is happening the same day, month and year so there is no need
            for logic here

            else condition can be removed but I left it here to place this comment
            */
        }
        return calendar;
    }

    /***
     * setResetAlarm()
     *
     * simple method that reset the alarm for the next month. Used when the user set their status to
     * done and the notifications reset until the next month starts
     */
    private void setResetAlarm() {
        int monthOfYear = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        if (monthOfYear <12) {
            calendar.set(Calendar.MONTH,monthOfYear+1);
        } else {
            calendar.set(Calendar.MONTH,1);
            calendar.set(Calendar.YEAR,year+1);
        }
        calendar.set(Calendar.HOUR_OF_DAY,9);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
    }

    /***
     * setNextAlarm()
     *
     * sets the next notification alarm. With the use of some other functions
     *
     * @see this.setDayMonthAndYearToRun() and this.getNextHourToRun()
     */
    private void setNextAlarm() {
        int nextHour = getNextHourToRun(calendar);
        calendar = setDayMonthAndYearToRun(calendar);
        calendar.set(Calendar.HOUR_OF_DAY,nextHour);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MINUTE,0);
    }


}
