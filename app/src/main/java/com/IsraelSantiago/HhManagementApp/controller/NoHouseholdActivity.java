package com.IsraelSantiago.HhManagementApp.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.Household;
import com.IsraelSantiago.HhManagementApp.model.Member;
import com.IsraelSantiago.HhManagementApp.model.Utilities;

/***
 * NoHouseholdActivity.class
 *
 * Java class that provides the control over the activity_no_household_user.xml
 *
 * Once a new user successfully registers, this activity will be the first one to show. It gives the
 * user the options of joining an existing household or creating a new one in which the creator will
 * have admin capabilities.
 *
 * @author Israel Santiago
 * @version 2.0
 */
public class NoHouseholdActivity extends MenuActivity implements View.OnClickListener{

    //instance fields
    private Button createAHouseholdButton;
    private Button joinAHouseholdButton;
    private TextView selectActivityGreeting;
    private EditText householdNameEditText, householdRentEditText;
    private Member user = DataHolder.getInstance().getMember();
    private Household household;
    RelativeLayout newUserSelectionRL;
    private ProgressBar progressBar;
    private AlertDialog.Builder dialog;
    private View textEntryView;
    private String householdName, householdRent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_household_user);

        DataHolder.getInstance().setActivity(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //set references to widgets
        createAHouseholdButton = (Button)findViewById(R.id.create_a_household_button);
        joinAHouseholdButton = (Button)findViewById(R.id.join_a_household_button);
        selectActivityGreeting = (TextView)findViewById(R.id.selectActivityGreeting);
        newUserSelectionRL = (RelativeLayout)findViewById(R.id.new_user_selection_rlayout);

        progressBar = Utilities.setProgressBar(this);
        newUserSelectionRL.addView(progressBar);

        //calls the ifPendingSetAlternateLayout
        ifPendingSetAlternateLayout();
    }


    // exit and back
    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        ifPendingSetAlternateLayout();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_a_household_button:
                setDialogPropertiesForCreateHousehold("");
                dialog.show();
                break;

            case R.id.join_a_household_button:
                setDialogPropertiesForJoinHousehold("");
                dialog.show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pending_member_menu, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * ifPendingSetAlternateLayout()
     *
     * this method checks if the user status is pending, if so blocks the create household button
     * and set the join household button to cancel pending status
     */
    private void ifPendingSetAlternateLayout(){
        if (!user.getHouseholdID().equals(0)) {
            household = new Household(user.getHouseholdID());
            household.retrieveHouseholdInfo();
        }
        if(user.getUserStatus().equals("pending")){
            String info = "You are currently pending to join " +household.getHouseholdName() +".";
            selectActivityGreeting.setText(info);
            createAHouseholdButton.setText(R.string.pending_button_string);
            createAHouseholdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (user.refreshUserInfo() && user.getUserStatus().equals("not done")) {
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(),OverviewActivity.class);
                        startActivity(intent);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Utilities.showToast("Your status is still set to " + user.getUserStatus());
                    }
                }
            });
            joinAHouseholdButton.setText(R.string.cancel_button_string);
            joinAHouseholdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //sets the listener to call a different method
                    progressBar.setVisibility(View.VISIBLE);
                if (user.cancelJoinHouseholdRequest() && user.refreshUserInfo()) {
                    progressBar.setVisibility(View.GONE);
                    ifPendingSetAlternateLayout();
                }
                }
            });
        } else {
            String greeting = "Welcome " + user.getFullName() + ". \n " +
                    "Choose from the two options below:";
            selectActivityGreeting.setText(greeting);
            createAHouseholdButton.setText(R.string.create_household_button_string);
            joinAHouseholdButton.setText(R.string.join_a_household_button_string);
            joinAHouseholdButton.setOnClickListener(this);
            createAHouseholdButton.setOnClickListener(this);
        }
    }

    /***
     * setDialogPropertiesForCreateHousehold()
     *
     * changes the properties of the dialog builder with fields necessary to create a new household.
     *
     * @param feedback string used as output for user error input
     */
    private void setDialogPropertiesForCreateHousehold(String feedback){
        dialog = new AlertDialog.Builder(this);

        textEntryView = LayoutInflater.from(this).inflate(R.layout.double_edit_text_layout, null);
        householdNameEditText = (EditText)textEntryView.findViewById(R.id.top_edit_text);
        householdRentEditText = (EditText)textEntryView.findViewById(R.id.bottom_edit_text);

        householdNameEditText.setHint("Household Name");
        householdNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        householdRentEditText.setHint("Rent Amount");
        householdRentEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        dialog.setTitle("Enter the household name and rent: ");
        dialog.setMessage(feedback);
        dialog.setCancelable(false);
        dialog.setView(textEntryView);

        dialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                householdName = householdNameEditText.getText().toString();
                householdRent = householdRentEditText.getText().toString();
                dialog.cancel();
                progressBar.setVisibility(View.VISIBLE);
                if(validateHouseholdName("create") && validateRentAmount() && isHouseholdNameAvailable()){
                    household = new Household(householdName,Float.parseFloat(householdRent));
                    if (household.createHousehold() && user.refreshUserInfo()) {
                        Intent intent = new Intent(getApplicationContext(),OverviewActivity.class);
                        startActivity(intent);
                    }
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

    /***
     * setDialogPropertiesForJoinHousehold()
     *
     * changes the properties of the dialog builder with a field to join an existing household.
     *
     * @param feedback string used to notify the user of error on user input
     */
    private void setDialogPropertiesForJoinHousehold(String feedback){
        dialog = new AlertDialog.Builder(this);

        textEntryView = LayoutInflater.from(this).inflate(R.layout.single_edit_text_dialog_layout, null);
        householdNameEditText = (EditText)textEntryView.findViewById(R.id.single_edit_text);
        householdNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        dialog.setTitle("Enter the household's name: ");
        dialog.setMessage(feedback);
        dialog.setCancelable(false);
        dialog.setView(textEntryView);

        dialog.setPositiveButton("Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                householdName = householdNameEditText.getText().toString();
                dialog.cancel();

                if(validateHouseholdName("join")&& doesHouseholdExists()) {
                    if (user.joinHousehold(householdName) && user.refreshUserInfo()) {
                        ifPendingSetAlternateLayout();
                    }
                }

            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    /***
     * validateHouseholdName()
     *
     * validates the household name for empty field, scape chars or extra blank space
     *
     * @return boolean true if valid input
     */
    private boolean validateHouseholdName(String option) {

        switch (option) {
            case "create":
                if (Utilities.missingInfo(householdName)) {
                    setDialogPropertiesForCreateHousehold("Enter a household name................");
                    dialog.show();
                } else if (!Utilities.isValidLength(householdName,50)) {
                    setDialogPropertiesForCreateHousehold("Please, no more than fifty characters for" +
                            "the household name");
                    dialog.show();

                } else if (Utilities.hasInvalidScapeChars(householdName)) {
                    setDialogPropertiesForCreateHousehold("No extra empty lines or tab white space allowed");
                    dialog.show();
                } else if (Utilities.hasExtraWhiteSpaceBetweenWords(householdName)) {
                    setDialogPropertiesForCreateHousehold("No more than one blank space allowed within words");
                    dialog.show();
                } else {
                    return true;
                }
                break;
            default:
                if (Utilities.missingInfo(householdName)) {
                    setDialogPropertiesForJoinHousehold("Enter a household name................");
                    dialog.show();
                } else {
                    return true;
                }
                break;
        }
        return false;
    }

    /***
     * doesHouseholdExists()
     *
     * method that checks if the household does exists in the database
     * @return true if the Household exist in the database
     */
    private boolean doesHouseholdExists() {
        if (Utilities.isOnDB(householdName,"hhm_households","HouseholdID","HouseholdName")){
            return true;
        } else {
            setDialogPropertiesForJoinHousehold("Household not found...");
            dialog.show();
            return false;
        }
    }

    /***
     * isHouseholdNameAvailable()
     *
     * checks if the name of a household is available on the database
     * @return true if the name is available
     */
    private boolean isHouseholdNameAvailable() {
        if (!Utilities.isOnDB(householdName,"hhm_households","HouseholdID","HouseholdName")){
            return true;
        } else {
            setDialogPropertiesForCreateHousehold("Household name in use...");
            dialog.show();
            return false;
        }
    }

    /***
     * validateRentAmount()
     *
     * validates the rent amount field for changing the rent. the rent field is validated for
     * blank input, non numerical values and rent not exceeding 10000.00
     *
     * @return true if the rent passes the aforemention validation
     */
    private boolean validateRentAmount(){
        if (Utilities.missingInfo(householdRent)) {
            setDialogPropertiesForCreateHousehold("Please enter the rent amount...");
            dialog.show();
        } else if (!Utilities.isValidFloat(householdRent)) {
            setDialogPropertiesForCreateHousehold("Only numbers allowed...");
            dialog.show();
        } else if (!Utilities.isValidAmount(householdRent, 10000.00f)) {
            setDialogPropertiesForCreateHousehold("Rent amount must not exceed $10,000.00");
            dialog.show();
        } else {
            return true;
        }
        return false;
    }
}
