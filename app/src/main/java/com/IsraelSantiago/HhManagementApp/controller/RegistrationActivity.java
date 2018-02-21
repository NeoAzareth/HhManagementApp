package com.IsraelSantiago.HhManagementApp.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.Patterns;
import com.IsraelSantiago.HhManagementApp.model.User;
import com.IsraelSantiago.HhManagementApp.model.Utilities;

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
 * Registration Activity
 *
 * A class that provides the initialization of the Registration UI, as well as validation of the
 * user input for registration.
 *
 * @author Israel Santiago
 * @version 2.0
 * @see User class
 */
public class RegistrationActivity extends Activity {

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText,
                    confirmPasswordEditText;
    Button registrationButton;
    private String firstName, lastName, email, password,confirmPassword;
    RelativeLayout relativeLayout;
    private ProgressBar progressBar;
    private SharedPreferences savedValues;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actitvity_registration);

        DataHolder.getInstance().setActivity(this);

        //references to widgets
        firstNameEditText = (EditText)findViewById(R.id.first_name_edit_view);
        lastNameEditText = (EditText)findViewById(R.id.last_name_edit_view);
        emailEditText = (EditText)findViewById(R.id.email_registration_edit_view);
        passwordEditText = (EditText)findViewById(R.id.pasword_registration_edit_view);
        confirmPasswordEditText = (EditText)findViewById(R.id.confirm_password_registration_edit_view);
        registrationButton = (Button)findViewById(R.id.registration_button);
        relativeLayout = (RelativeLayout)findViewById(R.id.registration_relative_layout);

        //set the progress bar
        progressBar = Utilities.setProgressBar(this);
        relativeLayout.addView(progressBar);

        //set the listener for the registration button
        registrationButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //make progress bar visible so that the user knows the app is working
                progressBar.setVisibility(View.VISIBLE);

                //retrieve the user info from widgets
                firstName = firstNameEditText.getText().toString();
                lastName = lastNameEditText.getText().toString();
                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                confirmPassword = confirmPasswordEditText.getText().toString();

                //create a user object with that info
                User user = new User(firstName,lastName,email,password);

                //call the validation method(below) if validation succeeds the attempts registration
                //if fails the validation method provides user feedback to attempt to resolve the
                //issue
                if (validateNewUserInput() && user.register()){
                    //removes any saved data from the editor
                    editor.clear();
                    //auto login the new user
                    if(user.login()){
                        Intent intent = new Intent(getApplicationContext(),NoHouseholdActivity.class);
                        startActivity(intent);
                    } else {
                        Utilities.showToast("Failed to auto login... please try again later.");
                        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });

        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = savedValues.edit();
        editor.apply();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onPause() {
        firstName = firstNameEditText.getText().toString();
        lastName = lastNameEditText.getText().toString();
        email = emailEditText.getText().toString();
        editor.putString("FirstName",firstName);
        editor.putString("LastName",lastName);
        editor.putString("RegEmail",email);
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        firstNameEditText.setText(savedValues.getString("FirstName",""));
        lastNameEditText.setText(savedValues.getString("LastName",""));
        emailEditText.setText(savedValues.getString("RegEmail",""));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
           firstNameEditText.setText("");
           lastNameEditText.setText("");
           emailEditText.setText("");
        }

        return super.onKeyDown(keyCode, event);
    }

    /***
     * validateNewUserInput()
     *
     * Validates the registration form and provides user feedback for missing info, regular expression
     * for first name, last name, email and password; password matching and user email address uniqueness.
     *
     * @return true on success
     * @see Utilities class for validation patterns and methods
     */
    public boolean validateNewUserInput(){
        if (Utilities.missingInfo(firstName,lastName,email,password,confirmPassword)) {
            Utilities.showToast("All fields are required!");
        } else if (!Utilities.isValidPattern(Patterns.FIRST_NAME,firstName)) {
            Utilities.showToast("Invalid characters on First Name");
        } else if (!Utilities.isValidLength(firstName,50)) {
            Utilities.showToast("Please, no more than fifty characters on the First name");
        } else if (!Utilities.isValidPattern(Patterns.LAST_NAME,lastName)) {
            Utilities.showToast("Invalid characters on Last Name");
        } else if (!Utilities.isValidLength(lastName,50)) {
            Utilities.showToast("Pleas, no more than fifty characters on last name");
        } else if (!Utilities.isValidEmail(email)) {
            Utilities.showToast("Invalid Email Address");
        } else if (!Utilities.isValidLength(email,100)) {
            Utilities.showToast("Please, no more than 100 characters on email");
        } else if (!Utilities.isValidPattern(Patterns.PASSWORD,password)) {
            Utilities.showToast("Password must contain: \n" +
                    "At least 8 characters long \n" +
                    "At least 1 number \n" +
                    "At least 1 upper case letter \n" +
                    "At least 1 lower case letter \n" +
                    "No whitespace allowed");
        } else if (!Utilities.passwordsMatch(password,confirmPassword)) {
            Utilities.showToast("Passwords do not match!");
        } else if (!Utilities.isValidLength(password,255)) {
            Utilities.showToast("Please, no more than 255 characters on password");
        } else if (Utilities.isOnDB(email,"hhm_users","UserID","Email")) {
            Utilities.showToast("Email Address is already in use!");
        } else {
            return true;
        }
        return false;
    }
}
