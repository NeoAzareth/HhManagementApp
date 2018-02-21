package com.IsraelSantiago.HhManagementApp.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.AlarmManagerBroadcastReceiver;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.Member;
import com.IsraelSantiago.HhManagementApp.model.Patterns;
import com.IsraelSantiago.HhManagementApp.model.Utilities;

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
 * SettingsActivity.class
 *
 * Java class that provides the control for the activity_settings.xml view.
 *
 * This class allows the user to change their password or leave (delete if user is an admin) a
 * household
 *
 * @author Israel Santiago
 * @version 2.0
 */
public class SettingsActivity extends MenuActivity implements View.OnClickListener{

    //Instance fields
    Button changePasswordButton, leaveOrDeleteButton;
    RelativeLayout relativeLayout;
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener dialogInterface;
    private ProgressBar progressBar;
    private EditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private View textEntryView;
    private String currentPasswordString, newPasswordString, confirmNewPasswordString;
    private boolean changePasswordInProgress = false;
    private Member user = DataHolder.getInstance().getMember();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        DataHolder.getInstance().setActivity(this);

        //set the reference to widgets
        changePasswordButton = (Button) findViewById(R.id.settings_change_password_button);
        changePasswordButton.setOnClickListener(this);
        leaveOrDeleteButton = (Button) findViewById(R.id.settings_delete_or_leave_button);
        leaveOrDeleteButton.setOnClickListener(this);

        if (user.getUserStatus().equals("not in") || user.getUserStatus().equals("pending")) {
            leaveOrDeleteButton.setVisibility(View.GONE);
        } else {
            leaveOrDeleteButton.setVisibility(View.VISIBLE);
        }

        if (user.getUserLevel().equals("admin")) {
            leaveOrDeleteButton.setText(R.string.delete_household_button_string);
        } else {
            leaveOrDeleteButton.setText(R.string.leave_household_button_string);
        }
        relativeLayout = (RelativeLayout) findViewById(R.id.settings_relative_layout);

        setDialogInterfaceForChangePassword("");

        //progressbar
        progressBar = Utilities.setProgressBar(this);
        relativeLayout.addView(progressBar);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_change_password_button:
                setDialogInterfaceForChangePassword("");
                builder.show();
                break;
            case R.id.settings_delete_or_leave_button:
                setDialogInterfaceForDeleteOrLeaveHousehold("");
                builder.show();
                break;
        }
    }

    /***
     * setDialogInterfaceForChangePassword()
     *
     * set the properties for the dialog builder with an UI to change the user's password
     *
     * @param feedback string used as error output
     */
    private void setDialogInterfaceForChangePassword(String feedback){
        builder = new AlertDialog.Builder(this);

        textEntryView = LayoutInflater.from(this).inflate(R.layout.settings_change_password_layout, null);
        currentPasswordEditText = (EditText)textEntryView.findViewById(R.id.first_edit_text);
        newPasswordEditText = (EditText)textEntryView.findViewById(R.id.second_edit_text);
        confirmNewPasswordEditText = (EditText)textEntryView.findViewById(R.id.third_edit_text);
        confirmNewPasswordEditText.setHint("Confirm New Password");
        if (changePasswordInProgress) {
            currentPasswordEditText.setText(currentPasswordString);
            newPasswordEditText.setText(newPasswordString);
        } else {
            currentPasswordEditText.setHint("Current Password");
            newPasswordEditText.setHint("New Password");
        }
        builder.setCancelable(false);

        dialogInterface = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.cancel();
                        progressBar.setVisibility(View.VISIBLE);
                        changePasswordInProgress = true;
                        currentPasswordString = currentPasswordEditText.getText().toString();
                        newPasswordString = newPasswordEditText.getText().toString();
                        confirmNewPasswordString = confirmNewPasswordEditText.getText().toString();

                        if (validateChangePasswordForm()) {
                            if (user.changePassword(currentPasswordString, confirmNewPasswordString)){
                                changePasswordInProgress = false;
                                Utilities.showToast("Password Updated!");
                            } else {
                                setDialogInterfaceForChangePassword(
                                        "Current Password does not match our records");
                                builder.show();
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        changePasswordInProgress = false;
                }
                progressBar.setVisibility(View.GONE);
            }
        };

        builder.setPositiveButton("Update",dialogInterface)
                .setNegativeButton("Cancel", dialogInterface);
        builder.setTitle("Enter the following info:");
        builder.setMessage(feedback);
        builder.setView(textEntryView);
    }

    /***
     * setDialogInterfaceForDeleteOrLeaveHousehold()
     *
     * Changes the properties of the dialog builder with a UI for leaving or deleting a household
     *
     * @param feedback string used for invalid data error output
     */
    private void setDialogInterfaceForDeleteOrLeaveHousehold(String feedback){
        builder = new AlertDialog.Builder(this);

        textEntryView = LayoutInflater.from(this).inflate(R.layout.single_edit_text_dialog_layout, null);
        currentPasswordEditText = (EditText)textEntryView.findViewById(R.id.single_edit_text);
        currentPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        currentPasswordEditText.setHint("Enter Password");

        builder.setCancelable(false);
        dialogInterface = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.cancel();
                        currentPasswordString = currentPasswordEditText.getText().toString();
                        if (Utilities.missingInfo(currentPasswordString)) {
                            setDialogInterfaceForDeleteOrLeaveHousehold("Enter your password!!!!!");
                            builder.show();
                        } else if (!user.simpleVerifyPass(currentPasswordString)) {
                            setDialogInterfaceForDeleteOrLeaveHousehold("Incorrect password...");
                            builder.show();
                        } else {
                            if (user.leaveHousehold() && user.refreshUserInfo()) {
                                Utilities.showToast("Success! redirecting...");
                                AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();
                                alarm.cancelAlarm(getApplicationContext());
                                Intent intent = new Intent(getApplicationContext(),NoHouseholdActivity.class);
                                startActivity(intent);
                            } else {
                                setDialogInterfaceForDeleteOrLeaveHousehold("Password does not " +
                                        "match our records");
                                builder.show();
                            }
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
                progressBar.setVisibility(View.GONE);
            }
        };

        builder.setPositiveButton("Yes I am!",dialogInterface)
                .setNegativeButton("On second thought...",dialogInterface);

        String householdName = DataHolder.getInstance().getHousehold().getHouseholdName();
        if(DataHolder.getInstance().getMember().getUserLevel().equals("admin")){
            builder.setTitle("Are you sure you want to DELETE " + householdName +"?");
        } else {
            builder.setTitle("Are you sure you want to LEAVE " + householdName +"?");
        }
        builder.setMessage(feedback);

        builder.setView(textEntryView);
    }

    /***
     * validateChangePasswordForm()
     *
     * method that validates the change password fields for empty fields, new password strength and
     * confirmation of new password
     * @return boolean true on success
     */
    private boolean validateChangePasswordForm(){
        if (Utilities.missingInfo(currentPasswordString,newPasswordString,confirmNewPasswordString)) {
            setDialogInterfaceForChangePassword("All fields are required!");
            builder.show();
        } else if (!Utilities.isValidPattern(Patterns.PASSWORD,newPasswordString)) {
            setDialogInterfaceForChangePassword("Password must contain: \n" +
                    "At least 8 characters long \n" +
                    "At least 1 number \n" +
                    "At least 1 upper case letter \n" +
                    "At least 1 lower case letter \n" +
                    "No whitespace allowed");
            builder.show();
        } else if (!Utilities.passwordsMatch(newPasswordString,confirmNewPasswordString)) {
            setDialogInterfaceForChangePassword("Passwords do not match!");
            builder.show();
        } else {
            return true;
        }
        return false;
    }
}
