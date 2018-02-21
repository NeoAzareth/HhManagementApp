package com.IsraelSantiago.HhManagementApp.controller;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
 * OverviewActivity.class
 *
 * Class that handles the logic for the overview activity. As the name implies, it shows an overview
 * of the status of the current household that the user belongs to; by greeting the user by name,
 * showing the household that the user currently belongs to as well as showing the rent amount for
 * the current household. It also shows the user's current bills in the cycle as well as other user's
 * status.
 *
 * It also initializes the alarm that handles the android notifications
 *
 * @author Israel Santiago
 * @see AlarmManagerBroadcastReceiver for notification logic
 * @version 2.0
 *
 */
public class OverviewActivity extends MenuActivity{

    private Member user = DataHolder.getInstance().getMember();
    private Household household;
    private TextView greetUserTextView, houseHoldNameTextView, householdRentTextView;
    private TableLayout myBillsTL, membersStatusTL;
    private TableRow myBillsLabels, membersStatusLabels;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    String date;
    RelativeLayout overviewRelativeLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        DataHolder.getInstance().setActivity(this);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMMM dd, yyy", Locale.US);

        //set references to widgets
        greetUserTextView = (TextView)findViewById(R.id.greetUserTextView);
        houseHoldNameTextView = (TextView)findViewById(R.id.householdNameTextView);
        householdRentTextView = (TextView)findViewById(R.id.houseHoldRentTextView);
        myBillsLabels = (TableRow)findViewById(R.id.labelsTableRow);
        membersStatusLabels = (TableRow)findViewById(R.id.labelsMemberStatusTR);
        myBillsTL = (TableLayout)findViewById(R.id.myBillsTableLayout);
        membersStatusTL = (TableLayout)findViewById(R.id.membersStatusTableLayout);
        overviewRelativeLayout = (RelativeLayout)findViewById(R.id.overview_relative_layout);

        //set the loading progress bar
        progressBar = Utilities.setProgressBar(this);
        overviewRelativeLayout.addView(progressBar);

        //start the alarm every time this activity is loaded
        AlarmManagerBroadcastReceiver alarm = new AlarmManagerBroadcastReceiver();

        switch (user.getUserStatus()) {
            case "not done":
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


    @Override
    public void onPause(){
        super.onPause();
    }


    @Override
    public void onResume(){
        super.onResume();
        setWidgets();
    }

    /**
     * setWidgets()
     *
     * method that changes the info on the widgets so that is matches the current user
     */
    public void setWidgets(){
        progressBar.setVisibility(View.VISIBLE);

        //Retrieves household info from the database
        household = new Household(user.getHouseholdID());
        if( user.retrieveUserBills() && household.retrieveHouseholdInfo() &&
                household.retrieveHouseholdMembers()) {
            DataHolder.getInstance().setHousehold(household);
        }

        date = dateFormat.format(calendar.getTime());

        //sets some widgets to greet the user
        String greetUser = "Hello " + Utilities.capitalizeString(user.getFirstName() + ".");
        greetUserTextView.setText(greetUser);
        String welcomeToHH = "Welcome to "+
                household.getHouseholdName() + " overview page,";
        houseHoldNameTextView.setText(welcomeToHH);
        String showRent = "Rent as of "+ date + ": "
                + Utilities.formatCurrency(household.getHouseHoldRent());
        householdRentTextView.setText(showRent );

        //populates the tables with info regarding the user's bills and other household members
        myBillsTL.removeAllViews();
        populateMyBillsTable();
        membersStatusTL.removeAllViews();
        populateMembersStatusTable();

        progressBar.setVisibility(View.GONE);
    }

    /***
     * populateMembersStatusTable()
     *
     * populates the table members with the current household members other than the current user
     *  if there are no other members, changes the view of the table to that there are no other members
     */
    public void populateMembersStatusTable(){
        if (household.getMembers().size() > 1 ) {
            membersStatusTL.addView(membersStatusLabels);
            int rowNum = 1;
            for(Member member: household.getMembers()){
                if (!member.getUserID().equals(user.getUserID())) {
                    TableRow tr = member.getUserStatusAsRow(this);
                    setRowBackgroundColor(tr,rowNum);
                    membersStatusTL.addView(tr);
                    rowNum++;
                }
            }
        } else {
            membersStatusLabels.setVisibility(View.GONE);
            TableRow tr = new TableRow(this);
            setAlternateTableView(tr,R.string.no_members_string);
            membersStatusTL.addView(tr);
        }
    }

    /***
     * populateMyBillsTable()
     *
     * populates the bills table with the current user bills. Just like the previous, it shows an
     * alternative view if there are no bills
     */
    public void populateMyBillsTable(){
        if(user.getBills() != null && user.getBills().size()>0){
            myBillsTL.addView(myBillsLabels);
            int rowNum = 1;
            for (Bill bill: user.getBills()){
                TableRow tr = bill.getBillAsOverviewTR(this);
                setRowBackgroundColor(tr,rowNum);
                myBillsTL.addView(tr);
                rowNum++;
            }
        } else {
            myBillsLabels.setVisibility(View.GONE);
            TableRow tr = new TableRow(this);
            setAlternateTableView(tr,R.string.no_bills_string);
            myBillsTL.addView(tr);
        }
    }

    /***
     * setRowBackgroundColor()
     *
     * implemented to reduce the amount of repetition. Sets the background color of a given table row
     * to a color specified by the lineNumber parameter.
     *
     * @param tableRow TableRow
     * @param lineNumber int
     */
    private void setRowBackgroundColor(TableRow tableRow, int lineNumber) {
        if (lineNumber % 2 != 0) {
            tableRow.setBackgroundColor(Color.argb(255, 224, 243, 250));
        } else {
            tableRow.setBackgroundColor(Color.argb(250, 255, 255, 255));
        }
    }

    /***
     * setAlternateTableView()
     *
     * Sets a given tableRow properties to display a given resourceString.
     * The int must be a resource number such as R.string.no_bills_string.
     *
     * @param tableRow TableRow
     * @param resourceString int, a resource number from R.string.name_of_the_string
     */
    private void setAlternateTableView(TableRow tableRow, int resourceString) {
        TextView tv = new TextView(this);
        tv.setText(resourceString);
        tv.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT,4f
        ));
        tableRow.addView(tv);
        tableRow.setBackgroundColor(Color.argb(255, 255, 153, 153));
        tableRow.setPadding(20,20,0,20);
    }
}
