package com.IsraelSantiago.HhManagementApp.model;

import android.util.Log;

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
 * Admin.class
 *
 * Admin class extends the Member class. Gives the App users methods that allowed them to delete a
 * household manage other users within their household and update the rent amount of the same household
 *
 * @author Israel Santiago
 * @see Member parent class
 * @version 2.0
 */
public class Admin extends Member {

    /***
     * Constructor with six parameters. Used by the Household.class to retrieve users information
     * without the password
     *
     * @param userID string
     * @param lastName string
     * @param firstName string
     * @param email string
     * @param userLevel string
     * @param userStatus string
     */
    Admin(Integer userID, String lastName,String firstName,String email, String userLevel, String userStatus) {
        super(userID,lastName,firstName,email,userLevel,userStatus);
    }

    /***
     * Constructor with eight parameters. Used by the User.class for login.
     *
     * @param userID string
     * @param lastName string
     * @param firstName string
     * @param email string
     * @param password string
     * @param userLevel string
     * @param userStatus string
     * @param householdId string
     */
    Admin(Integer userID, String lastName, String firstName, String email, String password,
                  String userLevel, String userStatus,Integer householdId) {
        super(userID, lastName, firstName, email, password, userLevel, userStatus, householdId);


    }

    /***
     * leaveHousehold()
     *
     * method that overrides Member.leaveHousehold() method. Unlike it's parent class, this method
     * deletes the household instead of just leaving it.
     *
     * @return true if the household is successfully deleted.
     */
    @Override
    public boolean leaveHousehold() {

        String query = "UPDATE hhm_users "+
                "SET UserStatus = 'not in', "+
                "HouseholdID = NULL, UserLevel = 'member' "+
                "WHERE HouseholdID = :householdID";

        String packData = query + ServerStrings.PACK_KEY + getHouseholdID();


        String data = super.encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.DELETE_HOUSEHOLD).get();
        } catch (Exception e) {
            Log.d("Delete household ",e.toString());
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
     * changeRentAmount()
     *
     * Method that overrides the  Member.changeRentAmount() that happens to be a non implemented method
     * at the Member level. This method allows the admin to update the rent of the current household
     *
     * @param rentAmount string
     * @return boolean true if successful
     */
    @Override
    public boolean changeRentAmount(String rentAmount){
        Float rent = Float.parseFloat(rentAmount);

        String query = "UPDATE hhm_households SET HhRentAmount = " + rent +
                " WHERE HouseholdID = :value";

        String packData = query + ServerStrings.PACK_KEY + getHouseholdID();
        String data = super.encodeCredentials();

        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.DEFAULT).get();
        } catch (Exception e) {
            Log.d("Household create",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_4)) {
            ErrorHandler.handleHouseholdNoLongerExistsError();
            return false;
        } else if (!error.equals("none")) {
            Utilities.showToast(error);
        } else {
            return true;
        }
        return false;
    }

    /***
     * manageHouseholdUser()
     *
     * Method that overrides the  Member.manageHouseholdUser() that happens to be a non implemented
     * method at the Member level. This method allows the admin to manage other household members.
     * The method takes an id belonging to the current user to manage. The action could be as of
     * right now Update or Delete but it could be extended if needed. Update action can be used to
     * reset the user status to not done which allows the user to keep adding bills but it is also
     * used to allow a pending user to the household. Delete action it's used to delete/remove users
     * from the household.
     *
     * @param id integer, the id of the subject of an action
     * @param action string, action to be exerted upon the user to which the id belongs
     * @return boolean true if the action its successful
     */
    @Override
    public boolean manageHouseholdUser(Integer id, String action) {

        Integer householdID = DataHolder.getInstance().getHousehold().getHouseholdID();

        String query = "";

        if (action.equals("update")) {
            query = "UPDATE hhm_users SET UserStatus = 'not done' " +
                    "WHERE UserID = " + id + " AND HouseholdID = :value";
        } else if (action.equals("delete")) {
            query = "UPDATE hhm_users SET UserStatus = 'not in', " +
                    "HouseholdID = NULL " +
                    "WHERE UserID = " + id + " AND HouseholdID = :value";
        }

        String packData = query + ServerStrings.PACK_KEY + householdID;

        String data = super.encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY, packData);

        String result = "";

        try {
            result = new DBConnection().execute(data, ServerStrings.DEFAULT).get();
        } catch (Exception e) {
            Log.d("Change user status ", e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_4)) {
            ErrorHandler.handleHouseholdNoLongerExistsError();
            return false;
        } else if (!error.equals("none")) {
            Utilities.showToast(error);
        } else {
            return true;
        }
        return false;
    }
}