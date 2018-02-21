package com.IsraelSantiago.HhManagementApp.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.ErrorHandler;
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
 * ReportActivity.class
 *
 * Java class that provides control over the activity_report.xml
 *
 * This class provides the logic necessary to generate reports
 *
 * @author Israel Santiago
 * @version 2.0
 * @see Household class
 */
public class ReportActivity extends MenuActivity implements View.OnClickListener {

    //instance fields
    Button goButton;
    private Spinner memberSpinner, categorySpinner, monthSpinner;
    String categoryString, monthString;
    RelativeLayout relativeLayout;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    View listEntryView;
    ListView reportListView;
    TextView totalTextView;
    private ProgressBar progressBar;
    private Household household = DataHolder.getInstance().getHousehold();
    AlertDialog.Builder builder;
    Member selectedMember;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        DataHolder.getInstance().setActivity(this);

        //set the reference to the widgets
        goButton = (Button) findViewById(R.id.go_button);
        goButton.setOnClickListener(this);

        memberSpinner = (Spinner) findViewById(R.id.report_member_spinner);
        memberSpinner.setAdapter(household.getMembersArrayAdapter(this));
        categorySpinner = (Spinner) findViewById(R.id.report_category_spinner);
        String[] categoryItems=  getResources().getStringArray(R.array.allCategoryArr);
        categorySpinner.setAdapter(setCustomAdapter(categoryItems));
        monthSpinner = (Spinner) findViewById(R.id.report_month_spinner);

        household.retrieveListOfMonthDates();

        monthSpinner.setAdapter(setCustomAdapter(household.getAvailableMonthsList()));
        relativeLayout = (RelativeLayout) findViewById(R.id.report_relative_layout);

        progressBar = Utilities.setProgressBar(this);
        relativeLayout.addView(progressBar);

        sharedPreferences = getSharedPreferences("SavedValues",Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.go_button:
                progressBar.setVisibility(View.VISIBLE);
                selectedMember = (Member) memberSpinner.getSelectedItem();
                categoryString = categorySpinner.getSelectedItem().toString();
                monthString = monthSpinner.getSelectedItem().toString();

                String report =
                        household.retrieveReport(selectedMember.getEmail(),categoryString,monthString);
                progressBar.setVisibility(View.GONE);

                String error = ErrorHandler.handleResult(report);

                if(!error.equals("none")) {
                    Utilities.showToast(error);
                } else {
                    displayReportDialog(Household.formatReportForList(report));

                }
                break;
        }
    }

    /***
     * setCustomAdapter()
     *
     * method that returns an array adapter given that the parameter provided is a list array.
     * Implemented to reduce the amount of code lines
     *
     * @param list a List<String> data type
     * @return an Array adapter
     */
    private ArrayAdapter<String> setCustomAdapter(List<String> list) {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    /***
     * setCustomAdapter()
     *
     * method that returns an array adapter given that the parameter provided is an string array.
     * Implemented to reduce the amount of code lines
     *
     * @param list a String[] data type
     * @return an Array adapter
     */
    private ArrayAdapter<String> setCustomAdapter(String[] list) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    /***
     * displayReportDialog()
     *
     * Method that displays the report inside a dialog builder pop up.
     * @param hashMapList a List<HashMap<String, String>> type
     */
    private void displayReportDialog(List<HashMap<String, String>> hashMapList){
        builder = new AlertDialog.Builder(this);
        listEntryView = LayoutInflater.from(this).inflate(R.layout.activity_report_list_view,null);
        reportListView = (ListView) listEntryView.findViewById(R.id.records_list_view);

        String total = "Total: " +
                Utilities.formatCurrency(Float.parseFloat(hashMapList.get(0).get("Total")));
        hashMapList.remove(0);

        totalTextView = (TextView) listEntryView.findViewById(R.id.report_total_text_view);
        totalTextView.setText(total);
        builder.setCancelable(false);

        String[] from = new String[] {"Name", "Description", "Amount", "Date", "Category"};
        int[] to = new int[] {R.id.record_name_text_view,R.id.record_description_text_view,
                R.id.record_amount_text_view,R.id.record_date_text_view,
                R.id.record_category_text_view};

        final SimpleAdapter adapter = new SimpleAdapter(this, hashMapList,R.layout.listview_report_record,from,to) {
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
        reportListView.setAdapter(adapter);

        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.cancel();
            }
        };
        builder.setPositiveButton("Back",dialogListener );
        builder.setView(listEntryView).show();
    }
}
