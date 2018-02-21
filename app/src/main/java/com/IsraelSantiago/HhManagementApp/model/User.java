package com.IsraelSantiago.HhManagementApp.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

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
 * User Class
 *
 * This is the most basic as well as the parent class for the app users.
 *
 * User, Member and Admin
 *
 * It provides methods that allow users to login, register and reset password as well as some
 * helpful methods.
 *
 * @author Israel Santiago
 * @see DBConnection class
 * @version 2.0
 * */
public class User {

    //instance fields
    private String firstName, lastName, email, password;

    /***
     * Constructor with no parameters, required for subclasses
     * */
    public User() {}

    /***
     * Constructor with one parameter.
     *
     * Used for the reset password feature
     * @param email user email
     */
    public User(String email){
        this.email = email;
    }

    /***
     * Constructor with two parameters.
     *
     * Used for the login feature
     *
     * @param email user email
     * @param password user password
     */
    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    /***
     * Constructor with four parameters.
     *
     * Used for the registration feature
     *
     * @param firstName user first name
     * @param lastName user last name
     * @param email user email
     * @param password user password
     */
    public User (String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    //------------------------- Setters and Getters --------------------------------
    protected void setEmail (String email) {this.email = email;}
    protected String getEmail () {return email;}

    protected void setPassword (String password) {this.password = password;}
    protected String getPassword () {return password;}

    public void setFirstName(String firstName) {this.firstName = firstName;}
    public String getFirstName() {return firstName;}

    public void setLastName(String lastName) {this.lastName = lastName;}
    public String getLastName() {return lastName;}

    /***
     * Login Method
     *
     * As the name implies, this method uses the DBConnection class to validate the user Email
     * address and password.
     *
     * In order to reduce redundancy (or at least that is what I think) a successful login is
     * achieved by simply attempting to retrieve the necessary user information. An script at the
     * server side will return negative feedback if the credentials do not match. Such negative
     * feedback will be treated as an incorrect email/password message; otherwise, the method will
     * assume the given output is valid and will attempt to create a Member sub class object.
     *
     * @return true if successful
     */
    public boolean login(){

        Log.d("User","call to login");

        String result = "";
        try {
            result = new DBConnection().execute(encodeCredentials(), ServerStrings.LOGIN).get();
            Log.d("User","login result: " + result);
        } catch (Exception e){
            Log.d("Login failed",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals("none")) {
            Utilities.showToast("Login successful.");

            String[] info = result.split("-c-");
            int userID = Integer.parseInt(info[0]);
            lastName = info[1];
            firstName = info[2];
            String userLevel = info[3];
            String userStatus = info[4];
            int householdID;

            try{
                householdID = Integer.parseInt(info[5]);
            } catch (Exception ArrayIndexOutOfBoundException){
                householdID = 0;
            }

            Member member;
            if(userLevel.equals("admin")){
                member = new Admin(userID,lastName,firstName,email,password,
                        userLevel,userStatus,householdID);
            } else {
                member = new Member(userID,lastName,firstName,email,password,
                        userLevel,userStatus,householdID);
            }
            SharedPreferences sharedPreferences = DataHolder.getInstance().getActivity()
                    .getSharedPreferences("SavedValues", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("userID",member.getUserID());
            editor.putString("autoLoginEmail",email);
            editor.putString("autoLoginPass",password);
            editor.apply();

            DataHolder.getInstance().setMember(member);
            return true;
        } else {
            Utilities.showToast(error);
        }
        return false;
    }

    /***
     * encodeCredentials()
     *
     * A helper class used to encode the user email and password. Necessary for the PHP script
     * _POST data. Not essential but since these are accessed so many times, it reduces amount of
     * lines
     *
     * @return a string with the user password
     * @see Utilities static method encodeData()
     */
    protected String encodeCredentials(){
        String credentials = Utilities.encodeData(ServerStrings.EMAIL_KEY,email);
        credentials += "&"+ Utilities.encodeData(ServerStrings.USER_PW_KEY,password);
        return credentials;
    }

    /***
     * resetPassword()
     *
     * A method that handles the resetting of the user password.
     *
     * @see this.randomPass()
     */
    public void resetPassword(){
        String newPass = randomPass();

        String data = Utilities.encodeData(ServerStrings.USER_PW_KEY,newPass);
        data += "&" + Utilities.encodeData(ServerStrings.EMAIL_KEY, email);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.PW_RESET).get();
        } catch (Exception e){
            Log.d("User Reset pass failed",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if (error.equals(ErrorHandler.ERROR_CODE_5)) {
            Utilities.showToast("Failed to send email with new password, password remains unchanged" +
                    "Please try again later...");
        } else if (error.equals(ErrorHandler.ERROR_CODE_3)) {
            Utilities.showToast("Failed to update password... disregard the email sent to you..." +
                    "Please try again later...");
        } else if (!error.equals("none")){
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Success! \n " +
                    "you should soon receive an email with your new password!");
        }
    }

    /***
     * register()
     *
     * A method that registers the a new user into the server database
     * @return true on success
     */
    public boolean register() {

        String packData = lastName + ServerStrings.PACK_KEY + firstName + ServerStrings.PACK_KEY +
                email + ServerStrings.PACK_KEY + password;

        String data = Utilities.encodeData(ServerStrings.PROCESS_KEY,packData);

        String result = "";

        try {
            result = new DBConnection().execute(data,ServerStrings.REGISTER).get();
        } catch (Exception e){
            Log.d("Registration failed",e.toString());
            Utilities.showToast("Unknown problem occurred...");
        }

        String error = ErrorHandler.handleResult(result);

        if(!error.equals("none")){
            Utilities.showToast(error);
        } else {
            Utilities.showToast("Registration Complete \n" +
                    "Attempting Login...");
            return true;
        }
        return false;
    }

    /***
     * randomPass()
     *
     * A helper method that creates a 12 characters long random password.
     * The password has:
     * 4 upper case letter
     * 4 lower case letter
     * 4 numbers
     *
     * @return a random 12 character password
     */
    private String randomPass(){
        //array to store random generated chars
        ArrayList<Character> passwordChars = new ArrayList<>();
        //random object
        Random rand = new Random();
        //randomize chars 4 times
        for(int x = 1;x<=4;x++) {
            //4 upper case letters
            int randomChar = 65 + rand.nextInt(26);
            passwordChars.add((char)randomChar);
            //4 numbers
            randomChar = 48 + rand.nextInt(10);
            passwordChars.add((char)randomChar);
            //4 lower case letters
            randomChar = 97 + rand.nextInt(26);
            passwordChars.add((char)randomChar);
        }
        //initialize string to be returned
        String randomPass = "";
        //shuffle the chars and append to the string to be returned
        while(passwordChars.size() != 0){
            int randomChar = 0 + rand.nextInt(passwordChars.size());
            randomPass += passwordChars.get(randomChar);
            passwordChars.remove(randomChar);
        }

        return randomPass;
    }

}