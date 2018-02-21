package com.IsraelSantiago.HhManagementApp.model;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableRow;
import android.widget.TextView;

import com.IsraelSantiago.HhManagementApp.R;


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
 * Bill.class
 *
 * Java class that instantiates bill objects. Allows bills to be saved, updated and deleted from
 * the database.
 *
 * Bills are objects that belong to users.
 *
 * @author Israel Santiago
 * @version 2.0
 */
public class Bill {

    //instance fields
    private int billID;
    private float billAmount;
    private String billDesc, billCategory, billDate;

    /***
     * Constructor with three parameters. Used by the ManageBillActivity.class to insert new bills
     * into the database.
     *
     * @param description string
     * @param amount float
     * @param category string
     */
    public Bill(String description, Float amount, String category ) {
        this.billDesc = description;
        this.billAmount = amount;
        this.billCategory = category;
    }

    /***
     * Constructor with five parameters. Used by the Member.class and Admin.class to create and add
     * the objects to an array that corresponds to the current user.
     *
     * @param billID integer
     * @param billAmount float
     * @param billDesc string
     * @param billCategory string
     * @param billDate string
     */
    Bill(int billID, float billAmount, String billDesc
            ,String billCategory, String billDate) {
        this.billID = billID;
        this.billAmount = billAmount;
        this.billDesc = billDesc;
        this.billCategory = billCategory;
        this.billDate = billDate;
    }


    //setters and getters
    public Integer getBillID() {return billID;}

    public String getBillDesc() {return billDesc;}
    public void setBillDesc(String desc) {this.billDesc = desc;}

    public String getBillAmountToString() {return String.valueOf(billAmount);}
    public void setBillAmount(String billAmount) {
        this.billAmount = Float.parseFloat(billAmount);
    }

    public String getBillCategory() {return billCategory;}
    public void setBillCategory(String billCategory) {
        this.billCategory = billCategory.toLowerCase();
    }

    /***
     * getBillToString()
     *
     * returns a string with some of the bill properties in the following order:
     *
     * billDescription: billAmount billCategory
     *
     * @return string
     */
    public String getBillToString() {
        return billDesc + ": " + Utilities.formatCurrency(billAmount) + " " + billCategory;
    }

    /***
     * getBillAsOverviewTR returns a row with most of the properties of the bill
     * @param context activity context
     * @return table row with most properties
     */
    public TableRow getBillAsOverviewTR(Context context){
        TableRow tr = new TableRow(context);
        tr.setTag(billID);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView descTV = new TextView(context);
        descTV.setText(billDesc);
        descTV.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT,
                2f
        ));
        descTV.setTextAppearance(R.style.style_table_elements);
        descTV.setPadding(15,20,0,20);
        tr.addView(descTV);

        TextView amountTV = new TextView(context);
        amountTV.setText(Utilities.formatCurrency(billAmount));
        amountTV.setGravity(Gravity.END);
        amountTV.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT,
                1f
        ));
        amountTV.setTextAppearance(R.style.style_table_elements);
        amountTV.setPadding(15,20,0,20);
        tr.addView(amountTV);

        TextView categoryTV = new TextView(context);
        categoryTV.setText(Utilities.capitalizeString(billCategory));
        categoryTV.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,1f
        ));
        categoryTV.setTextAppearance(R.style.style_table_elements);
        categoryTV.setPadding(15,20,15,20);
        categoryTV.setGravity(Gravity.END);
        tr.addView(categoryTV);

        return tr;
    }

    /***
     * saveBillInDB()
     *
     * as the name implies, this method saves the bills properties into the database. To do so,
     * it will require the user id and the household id
     *
     * @param householdID integer
     * @param userID integer
     * @return boolean true on success
     */
    public boolean saveBillInDB(Integer householdID, Integer userID) {

        String query = "INSERT INTO hhm_bills " +
                "VALUES(NULL," + billAmount
                +",'"+ billDesc +"','"+ billCategory
                +"',NOW(),"+householdID+", :value)";

        String packData = query + ServerStrings.PACK_KEY + userID;


        String data = DataHolder.getInstance().getMember().encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.DEFAULT).get();
        } catch (Exception e) {
            Log.d("Bill save",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_4)) {
            ErrorHandler.handleHouseholdNoLongerExistsError();
            return false;
        } else if(!error.equals("none")){
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Bill successfully added!");
            return true;
        }
        return false;
    }

    /***
     * updateBillInDB()
     *
     * updates the bill properties at the database level
     *
     * @return boolean true on success
     */
    public boolean updateBillInDB(){
        String query = "UPDATE hhm_bills " +
                "SET BillAmount = "+billAmount+"," +
                "BillDesc = '" +billDesc+"',"+
                "BillCategory = '"+billCategory+"',"+
                "BillDate = NOW() "+
                "WHERE BillID = :value";

        String packData = query + ServerStrings.PACK_KEY + billID;

        String data = DataHolder.getInstance().getMember().encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.DEFAULT).get();
        } catch (Exception e) {
            Log.d("Bill update",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_4)) {
            ErrorHandler.handleHouseholdNoLongerExistsError();
            return false;
        } else if(!error.equals("none")){
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Bill updated!");
            return true;
        }
        return false;
    }

    /***
     * deleteBillInDB()
     *
     * deletes the bill at the database level
     *
     * @return true on success
     */
    public boolean deleteBillInDB() {
        String query = "DELETE FROM hhm_bills WHERE BillID = :value";

        String packData = query +ServerStrings.PACK_KEY+ billID;

        String data = DataHolder.getInstance().getMember().encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.DEFAULT).get();
        } catch (Exception e) {
            Log.d("Bill save",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_4)) {
            ErrorHandler.handleHouseholdNoLongerExistsError();
            return false;
        } else if(!error.equals("none")){
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Bill deleted!");
            return true;
        }
        return false;
    }
}
