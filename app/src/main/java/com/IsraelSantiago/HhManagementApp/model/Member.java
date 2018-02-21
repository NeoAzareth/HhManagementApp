package com.IsraelSantiago.HhManagementApp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.TableRow;
import android.widget.TextView;

import com.IsraelSantiago.HhManagementApp.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Member.class
 *
 * Member class extends the User class provides methods that allow bill management, report generation
 * adding or leaving a household and essential settings such as changing password
 *
 * @author Israel Santiago
 * @see User class, parent class
 * @see Admin class, child class
 * @version 2.0
 */
public class Member extends User {

    //instance fields
    private String userLevel, userStatus;
    private Integer userID , householdID;
    private ArrayList<Bill> bills;

    /***
     * Constructor with six parameters, used by the Household class to retrieve other users information
     * without the password
     *
     * @param userID self explanatory
     * @param lastName   "
     * @param firstName  "
     * @param email      "
     * @param userLevel  "
     * @param userStatus "
     */
    public Member(Integer userID, String lastName,String firstName,String email, String userLevel, String userStatus) {
        this.userID = userID;
        this.userLevel = userLevel;
        this.userStatus = userStatus;
        super.setFirstName(firstName);
        super.setLastName(lastName);
        super.setEmail(email);

    }

    /***
     * Constructor with 8 parameters, used by the User class for login.
     *
     * @param userID         Self explanatory
     * @param lastName              "
     * @param firstName             "
     * @param email                 "
     * @param password              "
     * @param userLevel             "
     * @param userStatus            "
     * @param householdId           "
     */
    public Member(Integer userID, String lastName, String firstName, String email, String password,
                  String userLevel, String userStatus,Integer householdId) {
        super(firstName,lastName,email,password);
        this.userID = userID;
        this.userLevel = userLevel;
        this.userStatus = userStatus;
        this.householdID = householdId;

    }

    //setters and getters
    public String getEmail() {return super.getEmail();}
    public Integer getUserID() {return userID;}
    public String getUserLevel() {return userLevel;}
    public String getUserStatus() {return userStatus;}
    public Integer getHouseholdID(){return householdID;}
    public ArrayList<Bill> getBills() {return bills;}

    /***
     * Setter for the user status, unlike the other setters, this method sets the user status
     * at the database level.
     *
     * @return true on success
     */
    public boolean setUserStatus() {
        String query = "UPDATE hhm_users SET UserStatus = 'done' "+
                "WHERE UserID = :value";

        String dataToProcess = query + ServerStrings.PACK_KEY + userID;

        String data = DataHolder.getInstance().getMember().encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,dataToProcess);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.DEFAULT).get();
        } catch (Exception e) {
            Log.d("status update failed",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_4)) {
            ErrorHandler.handleHouseholdNoLongerExistsError();
        } else if(!error.equals("none")){
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Status set to \"done\". \n" +
                    "Reloading...");
            return true;
        }
        return false;
    }

    /***
     * getFullName()
     *
     * methods that outputs the full user name capitalized.
     *
     * @return full user name capitalized.
     */
    public String getFullName() {
        return Utilities.capitalizeString(getFirstName())
                + " " + Utilities.capitalizeString(getLastName());
    }

    /***
     * refreshUserInfo()
     *
     * method that queries the database for the certain information. Mainly pertaining to actions
     * such as: leaving/deleting the household and change on user status.
     *
     * @return true on success
     */
    public boolean refreshUserInfo() {

        String query = "SELECT UserLevel, UserStatus, HouseholdID " +
                "FROM hhm_users " +
                "WHERE UserID = :value";

        String dataToRetrieve = query + ServerStrings.PACK_KEY + userID;
        String data = Utilities.encodeData(ServerStrings.RETRIEVE_KEY,dataToRetrieve);
        data += "&" + super.encodeCredentials();

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.SINGLE_RETRIEVE).get();
        } catch (Exception e) {
            Log.d("Member refresh failed",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals("none")) {
            String[] info = result.split("-c-");
            userLevel = info[0];
            userStatus = info[1];
            try {
                householdID = Integer.parseInt(info[2]);
            } catch (IndexOutOfBoundsException e) {
                householdID = 0;
            }
            return true;
        } else {
            Utilities.showToast(error);
        }
        return false;
    }

    /***
     * retrieveUserBills()
     *
     * queries the database for the user bills.
     * called for any and all actions that modify the user's bills
     *
     * @return true on success
     */
    public boolean retrieveUserBills() {
        Log.d("Retrieving "+ getFirstName()," bills");
        String currentMonthAndYear = Utilities.getCurrentMonth();

        String query = "SELECT BillID, BillAmount, BillDesc, BillCategory, BillDate " +
                "FROM hhm_bills WHERE UserID = :value" +
                " AND BillDate LIKE '" + currentMonthAndYear + "%'";


        String dataToRetrieve = query + ServerStrings.PACK_KEY + userID;

        String data = Utilities.encodeData(ServerStrings.RETRIEVE_KEY,dataToRetrieve);
        data += "&" + this.encodeCredentials();

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.MULTI_RETRIEVE).get();
            Log.d("bill retrieved"," updating views");
        } catch (Exception e) {
            Log.d("Member Retrieve bills",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals("none")) {

            bills = new ArrayList<>();
            //split result as rows
            String[] lines = result.split("-r-");
            for (String line:lines
                    ) {
                Bill bill;
                //split result as columns
                String[] info = line.split("-c-");
                //1st element is the bill id
                int billID = Integer.parseInt(info[0]);
                //2nd element is the bill amount
                float billAmount = Float.parseFloat(info[1]);
                //3rd element is the description
                String billDescription = info[2];
                //4th element is the category
                String billCategory = info[3];
                //5th element is the date
                String billDate = info[4];
                //bill is instantiated and added to a bill array
                bill = new Bill(billID,billAmount,billDescription,billCategory,billDate);
                bills.add(bill);
            }
            return true;
        } else if (error.equals(ErrorHandler.ERROR_CODE_2)){
            bills = null;
            return true;
        }
        return false;
    }

    /***
     * getUserStatusAsRow()
     *
     * getUserStatusAsRow constructs a table row with the user properties. Not the suggested method
     * by the "book" I cannot remember why I went this route, but it shows a similar method used on
     * PHP
     *
     * @param context application context
     * @return a table row containing the some use info ready to be added to a table
     */
    public TableRow getUserStatusAsRow(Context context){
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView fullNameTV = new TextView(context);
        fullNameTV.setText(this.getFullName());
        fullNameTV.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT,
                2f
        ));
        fullNameTV.setTextAppearance(R.style.style_table_elements);
        fullNameTV.setPadding(15,15,0,15);
        tr.addView(fullNameTV);

        TextView userLevelTV = new TextView(context);
        userLevelTV.setText(Utilities.capitalizeString(userLevel));
        userLevelTV.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT,
                1f
        ));
        userLevelTV.setTextAppearance(R.style.style_table_elements);
        userLevelTV.setPadding(15,15,0,15);
        tr.addView(userLevelTV);

        TextView userStatusTV = new TextView(context);
        userStatusTV.setText(userStatus);
        userStatusTV.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,1f
        ));
        userStatusTV.setTextAppearance(R.style.style_table_elements);
        userStatusTV.setPadding(15,15,0,15);
        tr.addView(userStatusTV);

        return tr;
    }

    /***
     * cancelJoinHouseholdRequest()
     *
     * Method that as the name suggests, cancels a request made by the user to join a household.
     *
     * @return true on success
     */
    public boolean cancelJoinHouseholdRequest() {
        String query = "UPDATE hhm_users "+
                "SET UserStatus = 'not in', "+
                "HouseholdID = NULL "+
                "WHERE UserID = :value";

        String packData = query + ServerStrings.PACK_KEY + userID;

        String data = Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);
        data+= "&" + super.encodeCredentials();

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.DEFAULT).get();
        } catch (Exception e) {
            Log.d("Member cancel join",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_4)) {
            ErrorHandler.handleHouseholdNoLongerExistsError();
        } else if (!error.equals("none")) {
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Success! \n Redirecting...");
            return true;
        }
        return false;
    }

    /***
     * joinHousehold()
     *
     * contrary to the previous method, this method sets a request to join a given household (by name)
     * note that the method assumes validation was done and the name given does exits in the
     * database
     *
     * @param householdName the name of the household to join
     * @return true on success
     * @see Utilities isOnDB() method to see how to check for existing data on the DB
     */
    public boolean joinHousehold(String householdName){


        String packedData = userID + ServerStrings.PACK_KEY + householdName;


        String data = Utilities.encodeData(ServerStrings.PROCESS_KEY,packedData);
        data += "&" + super.encodeCredentials();

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.JOIN_HOUSEHOLD).get();
        } catch (Exception e) {
            Log.d("Member join get HhID",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (!error.equals("none")) {
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Success! \n Pending approval.");
            return true;
        }
        return false;
    }

    /***
     * changePassword()
     *
     * unlike the resetPassword() from the User super class, this method changes the password to the
     * desired by the user, not a random one.
     *
     * @param currentPassword current user password
     * @param newPassword new password
     * @return true on success
     */
    public boolean changePassword(String currentPassword, String newPassword){

        String data = Utilities.encodeData(ServerStrings.EMAIL_KEY,super.getEmail());
        data += "&" + Utilities.encodeData(ServerStrings.USER_PW_KEY,currentPassword);
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,newPassword);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.UPDATE_PW).get();
        } catch (Exception e) {
            Log.d("Change password ",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }
        String error = ErrorHandler.handleResult(result);

        if (!error.equals("none")) {
            Utilities.showToast(error);
        } else {
            setPassword(newPassword);
            return true;
        }
        return false;
    }

    /***
     * leaveHousehold()
     *
     * this method allows a user to leave the current household.
     *
     * @return boolean true on success
     */
    public boolean leaveHousehold() {

        String query = "UPDATE hhm_users "+
                "SET UserStatus = 'not in', "+
                "HouseholdID = NULL "+
                "WHERE UserID = :userID";

        String packData = query + ServerStrings.PACK_KEY + userID;

        String data = super.encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.LEAVE_HOUSEHOLD).get();
        } catch (Exception e) {
            Log.d("Leave household ",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }
        String error = ErrorHandler.handleResult(result);
        if (!error.equals("none")) {
           Utilities.showToast(error);
        } else {
            return true;
        }

        return false;
    }

    /***
     * logout()
     *
     * unlike the name suggests this method destroys objects in a class dedicated to save the two
     * objects required to make the app function.
     *
     * @see DataHolder class
     */
    public void logout() {
        DataHolder.getInstance().setMember(null);
        DataHolder.getInstance().setHousehold(null);
        SharedPreferences sharedPreferences = DataHolder.getInstance().getActivity()
                .getSharedPreferences("SavedValues", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("autoLoginEmail");
        editor.remove("autoLoginPass");
        editor.apply();
    }

    /**
     * getUserInfoAsHashMap()
     *
     * returns a hash map containing the user info
     *
     * @return HashMap with user info
     */
    HashMap<String,String> getUserInfoAsHashMap(){
        HashMap<String,String> hashMap = new HashMap<>();

        hashMap.put("Name",getFullName());
        hashMap.put("Status", Utilities.capitalizeString(userStatus));
        hashMap.put("Email",super.getEmail());

        return hashMap;
    }

    /***
     * simple verify pass is used to leave or delete a household.
     * the name simple is used due to the fact that this does not query the database to validate the
     * password... instead it only compares it to the one saved inside the object
     *
     * @param password password provided by the user
     * @return true if the password matches with the one stored in the object
     */
    public boolean simpleVerifyPass(String password){
        return password.equals(super.getPassword());
    }

    /***
     * Method implemented so that this class can be passed into an array adapter. Mainly used
     * by the report category where the user has the option to customize the report through an user
     * name; however, if two users happen to have the same full name, there may be trouble deciding
     * who is who. Not only for the user but for the coding part as well.
     *
     * This method solves the user part for two or more users with the same full name, by, instead of
     * providing the user full name, the method provides the first name concatenated with the email.
     *
     * @return string.
     */
    public String toString() {
        if (getEmail().equals("All")) {
            return getEmail();
        } else {
            return getFirstName() + " " + getEmail();
        }
    }

    /********methods that only Admin has logic for********/
    public boolean changeRentAmount(String amount){return false;}

    public boolean manageHouseholdUser(Integer id, String action) {return false;}
}