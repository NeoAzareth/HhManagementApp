package com.IsraelSantiago.HhManagementApp.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.AlarmManagerBroadcastReceiver;
import com.IsraelSantiago.HhManagementApp.model.Bill;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.Household;
import com.IsraelSantiago.HhManagementApp.model.Member;
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
 * ManageBillsActivity.class
 *
 * This activity provides the connection between the activity_manage_bills and the model classes that
 * provide the logic for the actions
 *
 * It allows users to manage their bills by adding, updating and deleting. As well as an option to close
 * their month period in order to notify other users(or admin) that they are done and stop receiving
 * notifications.
 *
 * @author Israel Santigo
 * @version 2.0
 * @see Member class
 * @see Household class
 */
public class ManageBillsActivity extends MenuActivity implements View.OnClickListener{

    //instance fields
    private Button addBillButton,doneButton;
    private ImageView imageView;
    private TableLayout manageBillsTableLayout;
    private Member user = DataHolder.getInstance().getMember();
    private Household household = DataHolder.getInstance().getHousehold();
    private TextView doneTextView,manageBillsTitleTextView, manageBillsInfoTextView;
    RelativeLayout manageBillsRL;
    private ProgressBar progressBar;
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener dialogClickListener;
    View textEntryView;
    private EditText billAmountEditText, billDescriptionEditText;
    private Spinner categorySpinner;
    private String billDescription,billAmount,billCategory;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ArrayAdapter<CharSequence> adapter;
    private Bill bill;
    private Boolean billInProgress = false;
    private TableRow myBillsLabels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bills);

        //adds shortcuts to the DataHolder class for activity and context
        DataHolder.getInstance().setActivity(this);

        billCategory = "category";

        //set references to the widgets
        imageView = (ImageView) findViewById(R.id.manageBillsImageView);
        manageBillsTableLayout = (TableLayout) findViewById(R.id.manageBillsTableLayout);
        addBillButton = (Button) findViewById(R.id.add_bill_button);
        doneButton = (Button) findViewById(R.id.doneButton);
        doneTextView = (TextView) findViewById(R.id.doneTextView);
        manageBillsRL = (RelativeLayout) findViewById(R.id.manage_bills_relative_layout);
        manageBillsTitleTextView = (TextView) findViewById(R.id.manage_bills_title_text_view);
        manageBillsInfoTextView = (TextView) findViewById(R.id.manage_bills_info_text_view);
        myBillsLabels = (TableRow) findViewById(R.id.myBillsLabels);

        //shared preferences
        sharedPreferences = getSharedPreferences("SavedValues",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();

        //set the adapter for the spinner
        adapter = ArrayAdapter.createFromResource(
                this,R.array.categoryArray,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        setDialogPropertiesForBillManagement("");

        //progressbar
        progressBar = Utilities.setProgressBar(this);
        manageBillsRL.addView(progressBar);

        showImageOrTable();

        removeEditAndDeleteButtons();

        //set the listeners
        addBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialogPropertiesForBillManagement("");
                builder.show();
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialogPropertiesForSetStatusToDone();
                builder.show();
            }
        });


        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }




    @Override
    public void onPause() {
        billDescription = billDescriptionEditText.getText().toString().trim();
        editor.putString("BillDescription",billDescription);
        billAmount = billAmountEditText.getText().toString().trim();
        editor.putString("BillAmount",billAmount);
        billCategory = categorySpinner.getSelectedItem().toString();
        editor.putString("BillCategory",billCategory);
        editor.commit();
        Log.d("On Pause",billDescription + " " + billAmount + " " + billCategory);
        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();

        billDescription = sharedPreferences.getString("BillDescription","");
        billDescriptionEditText.setText(billDescription);
        billAmount = sharedPreferences.getString("BillAmount","");
        billAmountEditText.setText(billAmount);
        billCategory = sharedPreferences.getString("BillCategory","category").toLowerCase();
        setCategorySpinner();
        Log.d("OnResume ", billDescription + " " + billAmount + " " + billCategory);
    }

    @Override
    public void onClick(View v) {
        setDialogForMenu();
        Integer billTag = (int)v.getTag();

        for(Bill bill:user.getBills()) {
            if(bill.getBillID().equals(billTag)) {
                this.bill = bill;
            }
        }
        if (!user.getUserStatus().equals("done")) {
            builder.setMessage(bill.getBillToString() + "... \n " +
                    "I want to: ").show();
        }
    }

    /***
     * showImageOrTable()
     *
     * method that alternates between showing the app image logo or a table with the current user's
     * bills; given the user has bills in the current period
     */
    private void showImageOrTable(){
        if(user.getBills()!= null && user.getBills().size()>0){
            imageView.setVisibility(View.GONE);
            manageBillsTableLayout.setVisibility(View.VISIBLE);
            displayUserBills();

        } else {
            manageBillsTableLayout.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    /***
     * displayUserBills()
     *
     * method that adds the user's bills to the table
     */
    private void displayUserBills(){
        if(user.getBills()!= null){
            manageBillsTableLayout.removeAllViews();
            manageBillsTableLayout.addView(myBillsLabels);
            int rowNum = 1;
            for(Bill bill:user.getBills()){
                TableRow tr = bill.getBillAsOverviewTR(this);
                tr.setOnClickListener(this);
                tr.setPadding(0,20,0,20);
                if(rowNum % 2 != 0){
                    tr.setBackgroundColor(Color.argb(255, 224, 243, 250));
                } else {
                    tr.setBackgroundColor(Color.argb(250, 255, 255, 255));
                }
                manageBillsTableLayout.addView(tr);
                rowNum++;
            }
        }
    }

    /***
     * setDialogMenu()
     *
     * changes the properties of the dialog builder so that it displays information about a selected/
     * touched bill as well as three buttons for canceling the pop up, deleting or editing the selection
     */
    private void setDialogForMenu() {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        dialogClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE://edit bill
                        dialog.cancel();
                        setDialogPropertiesForBillManagement("");
                        builder.show();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE://cancel/exit pop option menu
                        dialog.cancel();
                        bill = null;
                        break;

                    case DialogInterface.BUTTON_NEUTRAL://delete bill
                        dialog.cancel();
                        setDialogForDeletion();
                        break;
                }
            }
        };
        builder.setPositiveButton("Edit",dialogClickListener)
                .setNegativeButton("Cancel",dialogClickListener)
                .setNeutralButton("Delete",dialogClickListener);
    }

    /***
     * setDialogForDeletion()
     *
     * Upon selecting to delete a bill, this method changes the properties of the dialog builder to
     * provide the user with one last check before deleting the bill
     */
    private void setDialogForDeletion() {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        dialogClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.cancel();
                        if (bill.deleteBillInDB() & user.retrieveUserBills()) {
                            Log.d("bill deleted"," attempting retrieveUserBills()");
                            showImageOrTable();
                            resetSharedPreferences();
                            removeEditAndDeleteButtons();
                            displayUserBills();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        bill = null;
                        break;
                }
            }
        };

        builder.setPositiveButton("Yes",dialogClickListener)
                .setNegativeButton("No",dialogClickListener);
        builder.setMessage("Delete "+ bill.getBillDesc()+ "?").show();
    }

    /***
     * setDialogPropertiesForSetStatusToDone()
     *
     * changes the properties of the dialog builder with and UI that gives the user the option to set
     * their status to done and stop receiving notifications. If the method is successful on setting
     * the user status to done, calls the required methods to change the user UI and denies all further
     * bill management options
     */
    private void setDialogPropertiesForSetStatusToDone(){
        builder = null;
        builder = new AlertDialog.Builder(this);

        dialogClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.cancel();
                        progressBar.setVisibility(View.VISIBLE);
                        if (user.setUserStatus() && user.refreshUserInfo() && household.retrieveHouseholdMembers()) {
                            household.areAllUsersDone();
                            AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();
                            alarm.setAlarm(getApplicationContext(),AlarmManagerBroadcastReceiver.RESET_ALARM);
                            resetSharedPreferences();
                            showImageOrTable();
                            removeEditAndDeleteButtons();
                            displayUserBills();
                        }
                        progressBar.setVisibility(View.GONE);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            }
        };
        builder.setPositiveButton("Yes I am!",dialogClickListener)
                .setNegativeButton("On second thought",dialogClickListener);
        builder.setTitle("Are you sure?");
        builder.setMessage("You will not be able to modify your bills...");
        builder.setCancelable(false);
    }

    /***
     * resetSharedPreferences()
     *
     * method that resets the shared preferences to a default value as well as all other variables
     * used for editing bills
     */
    private void resetSharedPreferences(){

        billInProgress = false;
        bill = null;
        billCategory = "category";

        editor.remove("BillDescription");
        editor.remove("BillAmount");
        editor.remove("BillCategory");
        editor.commit();
    }

    /***
     * setDialogPropertiesForBillManagement()
     *
     * changes the dialog builder properties with options to add or edit bills
     *
     * @param feedback string used to notify the user of invalid data
     */
    private void setDialogPropertiesForBillManagement(String feedback) {

        builder = new AlertDialog.Builder(this);

        textEntryView = LayoutInflater.from(this).inflate(R.layout.manage_bill_layout, null);
        billDescriptionEditText = (EditText)textEntryView.findViewById(R.id.first_edit_text);
        billAmountEditText = (EditText)textEntryView.findViewById(R.id.second_edit_text);
        categorySpinner = (Spinner)textEntryView.findViewById(R.id.billCategorySpinner);

        categorySpinner.setAdapter(adapter);

        builder.setCancelable(false);
        if (bill != null) {//begin editing a bill
            billDescriptionEditText.setText(bill.getBillDesc());
            billAmountEditText.setText(bill.getBillAmountToString());
            billCategory = bill.getBillCategory().toLowerCase();
        } else if (billInProgress) {// bill is being edited but an user error input occurred
            billDescriptionEditText.setText(billDescription);
            billAmountEditText.setText(billAmount);
        } else {// bill is being added
            billDescriptionEditText.setHint("Bill Description");
            billAmountEditText.setHint("Bill Amount");
        }

        setCategorySpinner();


        dialogClickListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.cancel();
                        billDescription = billDescriptionEditText.getText().toString().trim();
                        billAmount = billAmountEditText.getText().toString().trim();
                        billCategory = categorySpinner.getSelectedItem().toString().trim();

                        if (validateBillInfo()) {//if validation is passed

                            boolean result;
                            if (bill != null) {//code for updating bills
                                bill.setBillDesc(billDescription);
                                bill.setBillAmount(billAmount);
                                bill.setBillCategory(billCategory);
                                result = bill.updateBillInDB();

                            } else {//otherwise a new bill object is created and inserted into the DB
                                Integer userID = user.getUserID();
                                Integer householdID = household.getHouseholdID();
                                result = new Bill(billDescription,Float.parseFloat(billAmount),billCategory)
                                        .saveBillInDB(householdID,userID);
                            }

                            if (result && user.retrieveUserBills()) {
                                resetSharedPreferences();
                                removeEditAndDeleteButtons();
                                showImageOrTable();
                                displayUserBills();
                            } else {
                                Utilities.showToast("Could not complete, try again later...");
                            }

                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        resetSharedPreferences();
                        dialog.cancel();
                        break;
                }
            }
        };

        builder.setPositiveButton("Submit",dialogClickListener)
                .setNegativeButton("Cancel",dialogClickListener);
        builder.setTitle("Enter the bill info: ");
        builder.setMessage(feedback);
        builder.setView(textEntryView);
    }

    /**
     * setCategorySpinner()
     *
     * sets the category spinner with the use of the local variable billCategory
     */
    public void setCategorySpinner(){
        int selection = 0;
        switch (billCategory){
            case "category":
                selection = 0;
                break;
            case "food":
                selection = 1;
                break;
            case "utility":
                selection = 2;
                break;
            case "maintenance":
                selection = 3;
                break;
            case "other":
                selection = 4;
                break;
        }
        categorySpinner.setSelection(selection);
    }

    /***
     * removeEditAndDeleteButtons()
     *
     * removes or shows the edit and delete buttons if the user has no bills or if it
     * has bills respectively
     */
    public void removeEditAndDeleteButtons(){
        if (user.getUserStatus().equals("done")) {
            addBillButton.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);
            doneTextView.setVisibility(View.GONE);
            manageBillsTitleTextView.setVisibility(View.GONE);
            manageBillsInfoTextView.setText(R.string.done_status_info_string);
            manageBillsInfoTextView.setVisibility(View.VISIBLE);
            manageBillsInfoTextView.setGravity(Gravity.START);
        } else if (user.getBills()== null || user.getBills().size() == 0) {
            doneButton.setVisibility(View.VISIBLE);
            doneTextView.setVisibility(View.VISIBLE);
            addBillButton.setVisibility(View.VISIBLE);
            manageBillsInfoTextView.setVisibility(View.GONE);
            manageBillsTitleTextView.setVisibility(View.GONE);
        } else {
            addBillButton.setVisibility(View.VISIBLE);
            doneButton.setVisibility(View.VISIBLE);
            doneTextView.setVisibility(View.VISIBLE);
            manageBillsInfoTextView.setVisibility(View.VISIBLE);
            manageBillsTitleTextView.setVisibility(View.VISIBLE);
        }
    }

    /***
     * validateBillInfo()
     *
     * validates the bill info and provides the proper feedback to the user if any error is found
     *
     * @return true if all info is valid
     */
    private boolean validateBillInfo() {

        String feedback;
        if (Utilities.missingInfo(billDescription,billAmount)) {
            feedback = "All fields are required!";
        } else if (Utilities.hasExtraWhiteSpaceBetweenWords(billDescription)) {
            feedback = "No more than one blank space within words...";
        } else if (Utilities.hasInvalidScapeChars(billDescription)) {
            feedback = "No extra lines or tab white space allowed...";
        } else if (!Utilities.isValidLength(billDescription,100)) {
            feedback = "Twenty words or less for description...";
        } else if (!Utilities.isValidFloat(billAmount)) {
            feedback = "Enter a valid number for the amount...";
        } else if (!Utilities.isNotZeroOrNegative(billAmount)) {
            feedback = "Amount must be greater than 0.00";
        } else if (!Utilities.isValidAmount(billAmount,1000.00f)) {
            feedback = "Amount must be less than $1000.00";
        } else if (billCategory.toLowerCase().equals("category")) {
            feedback = "Select a category from the dropdown list!";
        } else {
            return true;
        }
        billInProgress = true;
        setDialogPropertiesForBillManagement(feedback);
        builder.show();
        return false;
    }
}
