package com.IsraelSantiago.HhManagementApp.model;


import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;

/***
 * Household.class
 *
 * Class used to instantiate Household Objects. It provides methods to create/save it self into
 * the database, to display its properties and to retrieve reports from the database.
 *
 * @author Israel Santiago
 * @version 1.1
 */
public class Household {

    //instance fields
    private String householdName;
    private int householdID;
    private Float householdRent;
    private ArrayList<Member> members;
    private ArrayList<String> availableMonthsList;

    /***
     * Constructor with one parameter. Used to retrieve a household info from the database
     *
     * @param householdID integer, id belonging to the household at the database level
     */
    public Household(Integer householdID) {
        this.householdID = householdID;
    }

    /***
     * Constructor with two parameters. Use to create a new household.
     *
     * @param householdName string, household name
     * @param householdRent float, household rent
     */
    public Household(String householdName,Float householdRent) {
        this.householdName = householdName;
        this.householdRent = householdRent;
    }

    //getters and setters
    public Integer getHouseholdID() {return householdID;}

    public String getHouseholdName(){
        return householdName;
    }

    public ArrayList<String> getAvailableMonthsList() { return availableMonthsList;}

    public float getHouseHoldRent(){
        return householdRent;
    }
    public void setHouseholdRent(String householdRent) {
        this.householdRent = Float.parseFloat(householdRent);
    }

    public ArrayList<Member> getMembers(){
        return this.members;
    }

    /***
     * retrieveHouseholdInfo()
     *
     * As the name suggests, this methods retrieves the household info (name, rent) from the database.
     * In order to do so, the household must have been instantiated previously with the household id.
     *
     * @return true on success.
     */
    public boolean retrieveHouseholdInfo() {

        String query = "SELECT HouseholdName, HhRentAmount " +
                "FROM hhm_households WHERE HouseholdID = :value";

        String packData = query + ServerStrings.PACK_KEY + householdID;

        String data = Utilities.encodeData(ServerStrings.RETRIEVE_KEY,packData);
        data +="&" + DataHolder.getInstance().getMember().encodeCredentials();

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.SINGLE_RETRIEVE).get();
        } catch (Exception e) {
            Log.d("retrieveHHInfo",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals("none")) {
            //the line is separated into columns
            String[] info = result.split("-c-");
            //first element is the household name
            householdName = info[0];
            //second element is the household rent
            householdRent = Float.parseFloat(info[1]);
            return true;
        } else {
            Utilities.showToast(error);
        }
        return false;
    }

    /***
     * retrieveHouseholdMembers()
     *
     * Retrieves the users associated with this household an stores them in the members array property.
     *
     * @return boolean true on success.
     */
    public boolean retrieveHouseholdMembers() {

        String query = "SELECT UserID, LastName, FirstName, Email, UserLevel, UserStatus " +
                "FROM hhm_users WHERE HouseholdID = :value";

        String packData = query + ServerStrings.PACK_KEY +householdID;

        String data = Utilities.encodeData(ServerStrings.RETRIEVE_KEY,packData);
        data += "&" + DataHolder.getInstance().getMember().encodeCredentials();

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.MULTI_RETRIEVE).get();
        } catch (Exception e) {
            Log.d("retrieveHHinfo",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals("none")) {
            members = new ArrayList<>();
            //the result is separated in a new array of string in which each element is a row
            String[] lines = result.split("-r-");
            //loop through each element to create Member objects
            for (String line:lines) {
                String[] info = line.split("-c-");

                Integer id = Integer.parseInt(info[0]);
                String lastName = info[1];
                String firstName = info[2];
                String email = info[3];
                String userLevel = info[4];
                String userStatus = info[5];

                Member member;

                if (userLevel.equals("admin")) {
                    member = new Admin(id,lastName,firstName,email,userLevel,userStatus);
                } else {
                    member = new Member(id,lastName,firstName,email,userLevel,userStatus);
                }
                members.add(member);
            }
            return true;
        } else {
            Utilities.showToast(error);
        }
        return false;
    }

    /***
     * retrieveListOfMonthDates()
     *
     * method that queries the database for unique dates related with the household. Specifically, it
     * will look at the BillDate column on the hhm_bills for a specific uniqueness on the first 7
     * characters of the BillDate.
     * E.g.
     *     dates in the database are formatted like this:  2018-01-01 12:12:12
     *     this method will only look at the first 7 characters from the date above as this: 2018-01
     *     and retrieve a non repeating list of dates.
     *
     * Such method is used for the report feature; thanks to this method, the user will only be able to
     * select from months that actually have bills associated with them.
     */
    public void retrieveListOfMonthDates() {
         String query = "SELECT DISTINCT LEFT(BillDate, LOCATE('-',BillDate,6)-1) FROM hhm_bills " +
                 "WHERE HouseholdID = :value";

         String packData = query + ServerStrings.PACK_KEY + householdID;

         String data = Utilities.encodeData(ServerStrings.RETRIEVE_KEY,packData);
         data += "&" + DataHolder.getInstance().getMember().encodeCredentials();

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.MULTI_RETRIEVE).get();
        } catch (Exception e) {
            Log.d("Household class"," failed to retrieve dates list");
            Utilities.showToast("Unknown problem occurred...");
        }

        Log.d("Household class","retrieve dates list result: "+ result);

        availableMonthsList = new ArrayList<>();

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_2)) {
            availableMonthsList.add(Utilities.getCurrentMonth());
        } else if (!error.equals("none")) {
            getMonthList();
        } else {
            result = result.replaceAll("-c-","");

            String[] dates = result.split("-r-");

            availableMonthsList = new ArrayList<>(Arrays.asList(dates));

            Collections.reverse(availableMonthsList);

            String currentMonth = Utilities.getCurrentMonth();
            if (!availableMonthsList.contains(currentMonth)) {
                availableMonthsList.add(0,Utilities.getCurrentMonth());
            }
        }
    }

    /***
     * getMonthList()
     *
     * method that sets the availableMonthsList with 12 elements that represent the current
     * month and 11 prior months to the current. Left in to the program so that if for some reason the
     * retrieveMonthsList
     */
    private void getMonthList() {
        Calendar calendar = Calendar.getInstance();
        ArrayList<String> monthStringList = new ArrayList<>();
        java.util.Date tasktime;
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM", Locale.US);

        for(int i = 0; i < 12; i++) {
            tasktime = calendar.getTime();
            monthStringList.add(sDateFormat.format(tasktime));
            calendar.add(Calendar.MONTH, -1);
        }
        this.availableMonthsList = monthStringList;
    }

    /***
     * retrieveReport()
     *
     * retrieves a report from the database. Given the constrains member, category and date.
     * This method will only retrieve the data. If no data is found matching the constrains, this
     * method return a "no records" string. On the opposite, if there are matches the data will be
     * separated by simple regular expression markers to delimit columns "-c-" and rows "-r-".
     *
     * @param email string
     * @param category string
     * @param date string
     * @return a string containing the report
     */
    public String retrieveReport(String email, String category, String date){

        String constrainMember = "";
        String constrainCategory = "";

        if (!email.equals("All")) {
            Integer id = getMemberID(email);
            constrainMember = " AND u.UserID = "+ id +" ";
        }
        if (!category.equals("All")) {
            constrainCategory = " AND BillCategory = '"+category+"' ";
        }

        String query = "SELECT FirstName, LastName, BillDesc, BillAmount, BillCategory, BillDate " +
                "FROM hhm_bills b " +
                "INNER JOIN hhm_users u " +
                "ON u.UserID = b.UserID " +
                "WHERE b.HouseholdID = :value " +
                "AND b.BillDate LIKE '" + date + "%' " +
                constrainMember +
                constrainCategory;
        String packData = query + ServerStrings.PACK_KEY + householdID;


        String data = DataHolder.getInstance().getMember().encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.RETRIEVE_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.MULTI_RETRIEVE).get();
        } catch (Exception e) {
            Log.d("retrieveReport",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }
        return result;
    }

    /***
     * formatReportForList()
     *
     * Method that formats a report string generated by the Household.retrieveReport() method and
     * returns it as a List of HashMaps. In order to recycle a loop this method also sums the
     * value of the bills of the report and saves it as the first value of the HashMap.
     *
     * @param report string generated by the Household.retrieveReport() method
     * @see this.retrieveReport()
     * @return List of HashMaps String, String - key, value
     */
    public static List<HashMap<String, String>> formatReportForList(String report) {

        List<HashMap<String, String>> reportAsHashMap = new ArrayList<>();

        SimpleDateFormat formatDate = new SimpleDateFormat("MMM d, yyyy", Locale.US);

        Float total = 0.0f;

        String[] rows = report.split("-r-");

        for (String row: rows
             ) {
            String[] fields = row.split("-c-");
            HashMap<String, String> rowMap = new HashMap<>();

            rowMap.put("Name", "By "+fields[0] + " " + fields[1]);

            rowMap.put("Description", Utilities.capitalizeString(fields[2]));

            Float billAmount = Float.parseFloat(fields[3]);
            rowMap.put("Amount", "$" + String.format(Locale.US,"%.2f",billAmount));
            total += billAmount;

            Date date = Date.valueOf(fields[5].substring(0,10));
            rowMap.put("Date", "On "+ formatDate.format(date));

            rowMap.put("Category", Utilities.capitalizeString(fields[4]));

            reportAsHashMap.add(rowMap);

        }

        HashMap<String, String> rowMap = new HashMap<>();

        rowMap.put("Total",String.format(Locale.US,"%.2f",total));

        reportAsHashMap.add(0,rowMap);

        return reportAsHashMap;
    }

    /***
     * getMemberID()
     *
     * returns a member id from the members array given the user full name. used to constrain the
     * report by member.
     *
     * @param email string
     * @see this.retrieveReport()
     * @return integer, the id related to the given user full name.
     */
    public Integer getMemberID(String email) {
        Integer id = 0;
        for (Member member : members
             ) {
            if (member.getEmail().equals(email)) {
                id = member.getUserID();
                break;
            }
        }
        return id;
    }

    /***
     * createHousehold()
     *
     * unlike the name suggests this method saves an already instantiated Household into the database.
     * The household must already have a name and rent value.
     *
     * @return boolean true on success
     */
    public boolean createHousehold() {
        householdName = householdName.replaceAll("'","\'");

        String packData = householdName + ServerStrings.PACK_KEY +householdRent;

        String data = DataHolder.getInstance().getMember().encodeCredentials();
        data += "&" + Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.CREATE_HOUSEHOLD).get();
        } catch (Exception e) {
            Log.d("Household create",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (!error.equals("none")) {
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Household successfully created! \n" +
                    "Redirecting to Overview...");
            return true;
        }
        return false;
    }

    /***
     * getMembersAsHashMapList()
     *
     * returns a list of HashMaps with the household member's info.
     *
     * @return List of HashMaps with member's info
     */
    public List<HashMap<String, String>> getMembersAsHashMapList(){

        List<HashMap<String, String>> hashMapList = new ArrayList<>();

        for (Member member:members
                ) {
            hashMapList.add(member.getUserInfoAsHashMap());
        }
        return hashMapList;
    }

    /***
     * isHouseholdAdmin()
     *
     * this methods checks if the given email belongs to be the household Admin. Used to avoid giving
     * the option to the admin of removing himself from a household. Previous versions used the
     * full name instead but in the very unlikely event that two users happen to have the same
     * full name, the method fails... so it was changed to email instead.
     *
     * @param email string.
     * @return true if the given name happens to be the household admin
     */
    public boolean isHouseholdAdmin(String email) {
        for (Member member :members
                ) {
            if (member.getEmail().equals(email)) {
                String level = member.getUserLevel();
                return  level.equals("admin");
            }
        }
        return false;
    }

    /***
     * returns an array adapter for the report activity member spinner
     *
     * @param context application context
     * @return ArrayAdapter with Member objects
     */
    public ArrayAdapter<Member> getMembersArrayAdapter(Context context){
        ArrayList<Member> memberArrayList = new ArrayList<>();

        memberArrayList.addAll(members);

        memberArrayList.add(0,
                new Member(0,"","","All","",""));
        ArrayAdapter<Member> adapter =
                new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,memberArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    /***
     * Method that upon setting the user status to done calls a script that checks if all users
     * are also done, handles the response from the script by notifying the user if other
     * users are still not done, if the script was successful in emailing the report, if there is
     * no need for report and normal error handling
     */
    public void areAllUsersDone(){
        String data = Utilities.encodeData(ServerStrings.KEYCODE_KEY,ServerStrings.KEY_CODE);
        data += "&" +Utilities.encodeData(ServerStrings.VERIFY_KEY,String.valueOf(householdID));

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.ALL_DONE).get();
        } catch (Exception e) {
            Utilities.showToast("Unknown error occurred... \n" +
                    "please try again later.");
        }

        String error = ErrorHandler.handleResult(result);

        switch (error) {
            case ErrorHandler.ERROR_CODE_5:
                Utilities.showToast("Failed to email report...");
                break;
            case "none":
                Utilities.showToast("All users are done! \n" +
                        "You should receive the report via email soon.");
                break;
            default:
                Utilities.showToast(error);
                break;
        }
    }
}
