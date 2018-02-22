package com.IsraelSantiago.HhManagementApp.model;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Utilities.class
 *
 * Former Validation class.
 *
 * Java class that provides validation methods as well as other useful methods that are to be
 * accessed statically.
 *
 * @author Israel Santiago
 * @version 1.0
 */
public class Utilities {

    //pattern objects used by the isValidPattern
    //first name can be used for any single words
    private static final Pattern FIRST_NAME = Pattern.compile("[a-zA-z]+([a-zA-Z]+)*");
    //last name can be used to validate sentences that do not include special characters
    private static final Pattern LAST_NAME = Pattern.compile("[a-zA-z]+([ '-][a-zA-Z]+)*");
    //password is used to validate password strength
    //at least eight characters long
    //at least one upper case letter
    //at least one lower case letter
    //at least one number
    //no whitespace allowed
    private static final Pattern PASSWORD
            = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$");


    //currency format
    private static final NumberFormat currency = NumberFormat.getCurrencyInstance();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.US);

    /***
     * is valid pattern takes an enum code and a string as parameters to determine if the
     * given string matches a regular expression as part of validation
     * @param patternsEnum an  enum from Patterns
     * @param toMatch the string to be validated
     * @return boolean
     */
    public static boolean isValidPattern(Enum<Patterns> patternsEnum, String toMatch){
        Pattern p = null;
        if(patternsEnum.equals(Patterns.FIRST_NAME)){
            p = FIRST_NAME;
        } else if (patternsEnum.equals(Patterns.LAST_NAME)) {
            p = LAST_NAME;
        } else if (patternsEnum.equals(Patterns.PASSWORD)) {
            p = PASSWORD;
        }

        Matcher matcher = p.matcher(toMatch);

        return matcher.matches();
    }

    /***
     * checks if the email has a valid pattern
     * @return boolean
     */
    public static boolean isValidEmail(String email){
        Pattern pattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
        Matcher matcher = pattern.matcher(email.toUpperCase());
        return matcher.matches();
    }

    /**
     * helper function that checks if the passwords match
     * @param pass string to be compared
     * @param pass2 string to compare to
     * @return boolean
     */
    public static boolean passwordsMatch(String pass, String pass2){
        return pass.equals(pass2);
    }

    /***
     * helper function that checks if any field is missing
     * @param args takes any number of string parameters to be validated for null or empty values
     * @return boolean
     */
    public static boolean missingInfo(String... args){
        for (String arg: args) {
            if(isNullOrEmpty(arg)){
                return true;
            }
        }
        return false;
    }

    /**
     * helper function that checks if any given string is empty or null
     * @param field string
     * @return boolean
     */
    private static boolean isNullOrEmpty(String field){
        return field.equals(null) || field.equals("");
    }

    /**
     * isValidFloat check if a given string can be cast as a float
     *
     * @param decimalNumber string
     * @return boolean
     */
    public static boolean isValidFloat(String decimalNumber){
        try{
            float f = Float.parseFloat(decimalNumber);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /***
     * isValidAmount checks if the given string after parsed is below a given float amount
     * this function will crash if used before isValidFloat and the string can not be parsed
     * @param amount string. original amount
     * @param maxAmount float maximum amount
     * @return boolean
     */
    public static boolean isValidAmount(String amount,Float maxAmount){
        return Float.parseFloat(amount)<maxAmount;
    }

    /***
     * isNotZeroOrNegative check that a given string after parsed to float is not 0 or negative
     * this function will crash if used before isValidFloat and the string can not be parsed
     * @param s string
     * @return boolean
     */
    public static boolean isNotZeroOrNegative(String s){
        return Float.parseFloat(s)>0.0;
    }

    /***
     * isValidLength checks if a given string is shorter or equal than a given length
     * @param s string
     * @param length integer
     * @return boolean
     */
    public static boolean isValidLength(String s, int length){
        return s.length()<=length;
    }

    /**
     * hasInvalidScapeChars checks any given amount of strings for scape characters \n \t \r
     * @param params several strings
     * @return boolean
     */
    public static boolean hasInvalidScapeChars(String... params){
        String[] unwantedChars = {"\n","\t","\r"};
        for(String s:params){
            for (String c:unwantedChars){
                if(s.contains(c)){
                    return true;
                }
            }
        }
        return false;
    }

    /***
     * hasExtraWhiteSpaceBetweenWords checks any given amount of strings for extra
     * white space within words
     * @param params several strings
     * @return boolean
     */
    public static boolean hasExtraWhiteSpaceBetweenWords(String... params){

        for(String s:params){
            if(s.trim().contains("  ")){
                return true;
            }
        }
        return false;
    }


    /*
     * function that cleans a string for space characters such as \n \r \t
     * as well as any extra white space within the string and both beginning and end of the string
     * @param s string
     * @return boolean
     *
    public static String clearWhiteSpace(String s){
        //replaces all instances of \n \t and \r with nothing ("")
        String[] unwanted = {"\n","\t","\r"};
        for(String string:unwanted){
            s = s.replaceAll(string," ");
        }

        s = s.trim();

        //replaces all extra white space within the string to a single white space
        Boolean hasExtra = true;
        while(hasExtra){
            if(s.contains("  ")){
                s=s.replaceAll("  "," ");
            } else {
                hasExtra = false;
            }
        }
        return s;
    }*/

    /***
     * isNetworkAvailable()
     *
     * checks if the device has internet access before querying the database
     * @param activity Activity
     * @return boolean
     */
    static Boolean isNetworkAvailable(Activity activity){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /*
     * isTwoDigitAtMost checks if given string has at most two digits after decimal point,
     * so that the given string is a valid currency number
     * @param s an integer or float number
     * @return true if this number has maximum two digits after decimal point
     *
    public static boolean isTwoDigitAtMost(String s) {
        if(s.indexOf(".") == -1)
            return true;

        else if(s.length() - (s.indexOf(".") + 1) <= 2)
            return true;

        else
            return false;
    }*/

    /**
     * helper function that shows a toast for user feedback
     * @param message string
     */
    public static void showToast(String message){
        Activity activity = DataHolder.getInstance().getActivity();
        Toast.makeText(activity.getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }

    /***
     * encodeData()
     *
     * encodes data to be passed to a PHP script as the super global _POST
     *
     * @param id string, the identifier on the _POST
     * @param value string, the value associated with the identifier
     * @return string with the two previous fields encoded
     */
    static String encodeData(String id, String value){
        try {
            String encodedData = URLEncoder.encode(id,"UTF-8")+ "=" +
                    URLEncoder.encode(value,"UTF-8");
            Log.d("Utilities encoding", "encoded "+ value+ ":" +encodedData);
            return encodedData;
        } catch (Exception e){
            Log.d("Encoding "+ id + ": "+ value + " failed", e.toString());
        }
        return null;
    }

    /***
     * isOnDB()
     *
     * Helper function used to check if any given value exits on the database. The script that this
     * method queries will not return the actual id, only boolean values.
     *
     * @param value string, the value to search for e.g. an email: hello@hello.com
     * @param table string, the table to look into. Using the previous example: hhm_users
     * @param id string, an id tag. Retrieved as proof to see if the value exits
     * @param column string, the column of the table to look into. Following the example: Email
     * @return boolean
     */
    public static Boolean isOnDB(String value, String table, String id, String column){

        //id -v- table -v- column -v- value

        String query = "SELECT " + id + " FROM " + table +" WHERE " + column + " = :value";
        String tableData = query + ServerStrings.PACK_KEY + value;


        String data = encodeData(ServerStrings.VERIFY_KEY,tableData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.VERIFY).get();
            Log.d("Validate isOnDB result:",result);
        } catch(Exception e){
            Log.d("Utilities isOnDB", e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        return error.equals("none") && result.equals("true");
    }

    /***
     * capitalizeString()
     *
     * returns a capitalized string
     * @param string string to be capitalized
     * @return string.
     */
    public static String capitalizeString(String string) {
        return string.substring(0,1).toUpperCase() + string.substring(1);
    }

    /***
     * setProgressBar()
     *
     * a simple method that reduces the amount of lines by taking the context of an activity and
     * returning a progress bar widget ready for use
     * @param context context of the activity
     * @return progress bar completely initialized
     */
    public static ProgressBar setProgressBar(Context context) {
        ProgressBar progressBar = new ProgressBar(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(300,300);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        progressBar.setLayoutParams(params);
        progressBar.setVisibility(View.GONE);
        return progressBar;
    }

    /***
     * formatCurrency()
     *
     * takes a float value and returns an string formatted as currency
     *
     * e.g.
     * fl = 123.0
     * returns $123.00
     *
     * @param fl float
     * @return string
     */
    public static String formatCurrency(Float fl) {
        currency.setMaximumFractionDigits(2);
        currency.setMinimumFractionDigits(2);
        return currency.format(fl);
    }

    /***
     * getCurrentMonth()
     *
     * returns a current date string on the pattern yyyy-MM e.g. 2018-01
     *
     * @return string
     */
    static String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime());
    }
}
