package com.IsraelSantiago.HhManagementApp.model;

import android.app.Activity;
import android.content.Intent;

import com.IsraelSantiago.HhManagementApp.controller.NoHouseholdActivity;

/**
 * Simple class used to handle errors that come from the server scripts.
 *
 * Contains Strings with explanations of such errors as well as a couple of methods that will
 * output the error strings or handle the number 4 error.
 *
 * @author Israel Santiago
 * @version 1.0
 * @see //mobileErrors.php - server file
 */
public class ErrorHandler {

    //Error strings
    private final static String UNKNOWN_ERROR = "Unknown error has occurred, \n" +
            "please try again later...";
    private final static String ERROR_CODE_0 = "Wrong email or password";
    private final static String ERROR_CODE_1 = "Failed to retrieve data, \n" +
            "please try again later...";
    final static String ERROR_CODE_2 = "No records found";

    final static String ERROR_CODE_3 = "Failed to complete action, \n" +
            "please try again later...";
    final static String ERROR_CODE_4 = "Household no longer exists, \n" +
            "loading no household screen.";
    final static String ERROR_CODE_5 = "failed to send email";
    private final static String ERROR_CODE_6 = "Awaiting other members to create report...";
    private static String ERROR_CODe_7 = "All users are done but there are no bills to " +
            "generate the report...";
    private final static String NO_ERROR = "none";
    private final static String NULL_ERROR = "Server is down... \n " +
            "please try again later...";

    /***
     * Simple method that outputs an error string based on the errorCode provided
     *
     * @param errorCode string
     * @return one of the static error strings
     */
    public static String handleResult(String errorCode) {

        try {
            switch (errorCode) {
                case "<br />":
                    return UNKNOWN_ERROR;
                case "0":
                    return ERROR_CODE_0;
                case "1":
                    return ERROR_CODE_1;
                case "2":
                    return ERROR_CODE_2;
                case "3":
                    return ERROR_CODE_3;
                case "4":
                    return ERROR_CODE_4;
                case "5":
                    return ERROR_CODE_5;
                case "6":
                    return ERROR_CODE_6;
                case "7":
                    return ERROR_CODe_7;
                default:
                    return NO_ERROR;
            }
        } catch (NullPointerException e) {
            return NULL_ERROR;
        }
    }

    /***
     * Method that handles the error code 4.
     *
     * Error code 4 happens when the household no longer exists. Since the application is not fully
     * synced with the database, if an admin deletes the household, and a member tries to interact
     * with that household a few seconds later the app will crash -worst case scenario...
     *
     * Given the code error happened this method will set the DataHolder Household object to null,
     * make the Member/Admin object refresh its data and redirect the user to the NoHouseholdActivity.class
     */
    static void handleHouseholdNoLongerExistsError(){
        Member member = DataHolder.getInstance().getMember();
        Utilities.showToast(ERROR_CODE_4);
        if (member.refreshUserInfo() && member.retrieveUserBills()) {
            DataHolder.getInstance().setHousehold(null);
            Activity activity = DataHolder.getInstance().getActivity();
            Intent intent = new Intent(
                    activity.getApplication().getApplicationContext(),NoHouseholdActivity.class);
            activity.startActivity(intent);
        }

    }
}
