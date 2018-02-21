package com.IsraelSantiago.HhManagementApp.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.Member;
import com.IsraelSantiago.HhManagementApp.model.User;
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
 * LoginActivity
 *
 * This activity provides the connection between the UI and the User class.
 * Used for user login, new user registration and user password
 * retrieval/resetting, as well as validation for the aforementioned actions.
 *
 * @author Israel Santiago
 * @see User class for actual implementation of actions
 * @version 2.0
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    //instance fields
    private EditText emailEditText;
    private EditText passwordEditText, resetPasswordEditText;
    TextView signUpTextView,passResetTextView;
    Button signInButton;
    private SharedPreferences savedValues;
    private CheckBox rememberEmailCheckBox;
    String email,pass;
    private Editor editor;
    private AlertDialog.Builder dialog;
    View textEntryView;
    RelativeLayout relativeLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DataHolder.getInstance().setActivity(this);

        savedValues = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = savedValues.edit();

        //reference to widgets
        signInButton = (Button)findViewById(R.id.sign_in_button);
        emailEditText = (EditText) findViewById(R.id.email_text_field);
        passwordEditText = (EditText)findViewById(R.id.password_text_field);
        signUpTextView = (TextView)findViewById(R.id.sign_up);
        passResetTextView = (TextView)findViewById(R.id.passResetTV);
        rememberEmailCheckBox = (CheckBox)findViewById(R.id.rememberEmailCheckBox);
        relativeLayout = (RelativeLayout) findViewById(R.id.login_layout);

        //set the loading progress bar
        progressBar = Utilities.setProgressBar(this);
        relativeLayout.addView(progressBar);

        //set the listeners
        signInButton.setOnClickListener(this);
        signUpTextView.setOnClickListener(this);
        passResetTextView.setOnClickListener(this);
        rememberEmailCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("rememberEmail",true);
                    editor.apply();
                } else {
                    editor.remove("rememberEmail");
                    editor.remove("email");
                    editor.commit();
                }
            }
        });

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onPause() {
        if(rememberEmailCheckBox.isChecked()){
            email = emailEditText.getText().toString();
            editor.putString("email",email);
            editor.commit();
        }
        Log.d("LOGIN ON PAUSE ","CALLLLLLLEDD");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        emailEditText.setText(savedValues.getString("email",null));
        rememberEmailCheckBox.setChecked(savedValues.getBoolean("rememberEmail",false));

        //auto login

        email = savedValues.getString("autoLoginEmail","");
        pass = savedValues.getString("autoLoginPass","");
        if(!email.equals("")&& !pass.equals("") && new User(email,pass).login()) {
            progressBar.setVisibility(View.VISIBLE);
            Intent intent;
            Member member = DataHolder.getInstance().getMember();
            if(member.getHouseholdID().equals(0) || member.getUserStatus().equals("pending")){
                intent = new Intent(getApplicationContext(),NoHouseholdActivity.class);
                startActivity(intent);
            } else {
                intent = new Intent(getApplicationContext(),OverviewActivity.class);
                startActivity(intent);
            }
        }

    }

    @Override
    public void onClick(View v) {
        //progressBar.setVisibility(View.VISIBLE);
        Intent intent;
        switch (v.getId()) {

            case R.id.sign_up:
                intent = new Intent(getApplicationContext(),RegistrationActivity.class);
                startActivity(intent);
                break;

            case R.id.passResetTV:
                setDialogProperties("");
                dialog.show();
                break;

            case R.id.sign_in_button:
                email = emailEditText.getText().toString();
                pass = passwordEditText.getText().toString();
                if(Utilities.missingInfo(email,pass)){
                    Utilities.showToast("Enter your email and password!");
                } else {
                    Log.d("LoginActivity","validation completed attempting Login ");
                    if(new User(email,pass).login()){
                        Member member = DataHolder.getInstance().getMember();
                        if(member.getHouseholdID().equals(0) || member.getUserStatus().equals("pending")){
                            intent = new Intent(getApplicationContext(),NoHouseholdActivity.class);
                            startActivity(intent);
                        } else {
                            intent = new Intent(getApplicationContext(),OverviewActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                break;
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Method that sets a dialog box for resetting password
     * @param feedback a string used to notify the user of errors
     */
    private void setDialogProperties(String feedback){
        dialog = new AlertDialog.Builder(LoginActivity.this);
        textEntryView = LayoutInflater.from(this).inflate(R.layout.single_edit_text_dialog_layout,null);
        resetPasswordEditText = (EditText)textEntryView.findViewById(R.id.single_edit_text);
        resetPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        dialog.setTitle("Enter your email address: ");
        dialog.setMessage(feedback);
        dialog.setCancelable(false);
        dialog.setView(textEntryView);

        dialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                email = resetPasswordEditText.getText().toString();
                dialog.cancel();
                if(validateEmail()) {
                    new User(email).resetPassword();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    /**
     * Method that validates the email address for the forgot password feature
     * @return true if the email is valid
     */
    private boolean validateEmail(){
        String error;
        if (Utilities.missingInfo(email)) {
            error = "Field is blank...";
        } else if (!Utilities.isValidEmail(email)) {
            error = "Invalid email address...";
        } else if (!Utilities.isOnDB(email,"hhm_users","UserID","Email")){
            error = "Email not found... \n"+"check your email and try again";
        } else {
            return true;
        }
        setDialogProperties(error);
        dialog.show();
        return false;
    }
}
