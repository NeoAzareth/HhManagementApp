package com.IsraelSantiago.HhManagementApp.model;

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
 * Class that contains strings necessary for database interaction
 *
 * All the strings are purposely left blank.
 *
 * Name the file names accordingly
 *
 * fill in the root path where your scripts are located
 *
 * match the keys with those on the KeyStrings.php file
 * @author Israel Santiago
 */
class ServerStrings {

    /***
     * Server file names
     * LOGIN - login..
     * PW_RESET - reset the user password
     * VERIFY - used by the Utilities.isOnDB() method
     * SINGLE_RETRIEVE - retrieves only one row of data
     * MULTI_RETRIEVE - retrieves many rows of data
     * DEFAULT - handles insert, update and delete actions
     * REGISTER - handles new user registration
     * JOIN_HOUSEHOLD - join household
     * UPDATE_PW - update password
     * LEAVE_HOUSEHOLD - leave household
     * DELETE_HOUSEHOLD - delete household
     * CREATE_HOUSEHOLD - create the household
     * SPREADSHEET - used to create a spreadsheet to be emailed to each user of a given household
     *                  process happens at server level not in app!
     * SERVICE - used by the notifications
     */
    static final String LOGIN = "";
    static final String PW_RESET ="";
    static final String VERIFY = "";
    static final String SINGLE_RETRIEVE = "";
    static final String MULTI_RETRIEVE = "";
    static final String DEFAULT = "";
    static final String REGISTER = "";
    static final String JOIN_HOUSEHOLD = "";
    static final String UPDATE_PW = "";
    static final String LEAVE_HOUSEHOLD = "";
    static final String DELETE_HOUSEHOLD = "";
    static final String CREATE_HOUSEHOLD = "";
    static final String ALL_DONE = "";
    static final String SERVICE = "";

    //server root path where the files are located
    static final String ROOT_PATH = "";
    //expected by some scripts to allow actions without user email or password
    //such as the service, new user registration, reset password and data verification
    static final String KEY_CODE = "";

    //keys... post keys...
    static final String EMAIL_KEY = "";
    static final String USER_PW_KEY = "";
    static final String KEYCODE_KEY = "";
    static final String VERIFY_KEY = "";
    static final String PROCESS_KEY = "";
    static final String RETRIEVE_KEY = "";
    static final String SERVICE_KEY = "";

    //key to pack data - to be match with the KeyStrings::$UNPACK_KEY
    static final String PACK_KEY = "";
}
