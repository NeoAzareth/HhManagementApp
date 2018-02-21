package com.IsraelSantiago.HhManagementApp.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.Household;
import com.IsraelSantiago.HhManagementApp.model.Member;
import com.IsraelSantiago.HhManagementApp.model.Utilities;

import java.util.HashMap;
import java.util.List;

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
 *
 *
 * AdminActivity.class
 *
 * Java class that provides control for activity_admin.xml view.
 *
 * It allows for the admin of a given household management over other users; allowing pending users
 * to enter a household, deleting or changing the status of other members. Lastly, the other function
 * of the Admin is to update the rent for the household.
 *
 * @author Israel Santiago
 * @version 2.0
 * @see com.IsraelSantiago.HhManagementApp.model.Admin
 */
public class AdminActivity extends MenuActivity implements AdapterView.OnItemClickListener {

    //instance fields
    RelativeLayout relativeLayout;
    Button changeRentButton;
    private ListView memberListView;
    private List<HashMap<String,String>> memberHashMapList;
    View textEntryView;
    private AlertDialog.Builder builder;
    private DialogInterface.OnClickListener dialogClickListener;
    private String rentAmount;
    private ProgressBar progressBar;
    private EditText rentAmountEditText;
    private HashMap<String, String> selectedMemberHashMap;
    private Member user = DataHolder.getInstance().getMember();
    private Household household = DataHolder.getInstance().getHousehold();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        DataHolder.getInstance().setActivity(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.activity_admin_relative_layout);
        changeRentButton = (Button) findViewById(R.id.admin_change_rent_button);
        memberListView = (ListView) findViewById(R.id.member_records_list_view);

        changeRentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDialogForChangeRent("");
                builder.show();
            }
        });
        memberListView.setOnItemClickListener(this);
        displayMemberList();
        displayDialogForChangeRent("");

        progressBar = Utilities.setProgressBar(this);
        relativeLayout.addView(progressBar);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedMemberHashMap = memberHashMapList.get(position);
        setDialogForMemberManagement();
        builder.show();
    }

    /***
     * displayMembersList()
     *
     * Method that displays the member list
     */
    public void displayMemberList(){

        memberHashMapList = household.getMembersAsHashMapList();

        String[] from = new String[] {"Name", "Status","Email"};
        int[] to = new int[] {R.id.member_record_name_text_view,
            R.id.member_record_status_text_view,R.id.member_record_email_text_view};

        final SimpleAdapter adapter =
                new SimpleAdapter(this, memberHashMapList,R.layout.listview_member_record,from,to) {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position,convertView,parent);
                if (position % 2 != 0)
                    view.setBackgroundColor(Color.argb(255, 224, 243, 250));
                else
                    view.setBackgroundColor(Color.argb(250, 255, 255, 255));
                return view;
            }
        };
        memberListView.setAdapter(adapter);
    }

    /***
     * displayDialogForChangeRent()
     *
     * sets the properties for a dialog builder in order to provide the user the means to update the
     * rent of the household.
     *
     * @param feedback string to be passed to dialog for error to notify the user of errors in input
     */
    private void displayDialogForChangeRent(String feedback) {
        builder = new AlertDialog.Builder(this);

        textEntryView = LayoutInflater.from(this).inflate(R.layout.single_edit_text_dialog_layout, null);
        rentAmountEditText = (EditText)textEntryView.findViewById(R.id.single_edit_text);
        rentAmountEditText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        rentAmountEditText.setHint("New Rent Amount");

        builder.setCancelable(false);
        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE://logic for changing the rent
                        dialog.cancel();
                        rentAmount = rentAmountEditText.getText().toString();

                        if (validateRentAmount()) {
                            if(user.changeRentAmount(rentAmount)) {
                                household.setHouseholdRent(rentAmount);
                                Utilities.showToast("Rent Updated!");
                            } else {
                                Utilities.showToast("Could not change rent...");
                            }
                        }

                        break;

                    case DialogInterface.BUTTON_NEGATIVE://cancel dialog box
                        dialog.cancel();
                        break;
                }
            }
        };

        builder.setTitle("Enter the new rent amount.");
        builder.setPositiveButton("Update Rent",dialogClickListener)
                .setNegativeButton("Never mind",dialogClickListener);
        builder.setMessage(feedback);
        builder.setView(textEntryView);
    }

    /***
     * setDialogForMemberManagement()
     *
     * method that changes the dialog builder properties for options that allow member management.
     */
    private void setDialogForMemberManagement(){
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        String email = selectedMemberHashMap.get("Email");
        String name = selectedMemberHashMap.get("Name");
        String status = selectedMemberHashMap.get("Status").toLowerCase();
        boolean isAdmin = household.isHouseholdAdmin(email);

        final Integer selectedUserID = household.getMemberID(email);

        dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE://update the user status
                        progressBar.setVisibility(View.VISIBLE);
                        dialog.cancel();
                        if (user.manageHouseholdUser(selectedUserID,"update") && household.retrieveHouseholdMembers()
                                && user.refreshUserInfo()) {
                            Utilities.showToast("User status updated!");
                            displayMemberList();
                        } else {
                            Utilities.showToast("Couldn't update user status...");
                        }
                        break;
                    case DialogInterface.BUTTON_NEUTRAL://delete the user status
                        dialog.cancel();
                        if (user.manageHouseholdUser(selectedUserID,"delete") && household.retrieveHouseholdMembers()
                                && user.refreshUserInfo()) {
                            Utilities.showToast("User removed!");
                            displayMemberList();
                        } else {
                            Utilities.showToast("Failed to remove user...");
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE://cancel the dialog box
                        dialog.cancel();
                        break;
                }
                progressBar.setVisibility(View.GONE);
            }
        };

        builder.setMessage("Current status is \""+ Utilities.capitalizeString(status)+"\"");
        builder.setNegativeButton("Cancel",dialogClickListener);
        if (status.equals("not done") && isAdmin){
            builder.setMessage("Nothing to do here...");
            builder.setNegativeButton("Dismiss",dialogClickListener);
        } else if (status.equals("done") && isAdmin) {
            builder.setPositiveButton("Reset status",dialogClickListener);
        } else if (!isAdmin) {
            builder.setNeutralButton("Remove member",dialogClickListener);
            if (status.equals("done")) {
                builder.setPositiveButton("Reset status",dialogClickListener);
            } else if (status.equals("pending")) {
                builder.setNeutralButton("Reject user",dialogClickListener);
                builder.setPositiveButton("Allow member",dialogClickListener);
            }
        }
        builder.setTitle("Options for "+ name);
    }

    /***
     * validateRentAmount()
     *
     * method that validates the rent amount for empty field, no valid float number and
     * @return true if the new rent is valid
     */
    private boolean validateRentAmount(){

        if (Utilities.missingInfo(rentAmount)) {
            displayDialogForChangeRent("Please enter the new rent amount...");
            builder.show();
        } else if (!Utilities.isValidFloat(rentAmount)) {
            displayDialogForChangeRent("Only numbers allowed...");
            builder.show();
        } else if (!Utilities.isValidAmount(rentAmount, 10000.00f)) {
            displayDialogForChangeRent("Rent amount must not exceed $10,000.00");
            builder.show();
        } else {
            return true;
        }
        return false;
    }

}
