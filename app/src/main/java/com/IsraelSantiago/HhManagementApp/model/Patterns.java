package com.IsraelSantiago.HhManagementApp.model;

/***
 * Enum Patters
 *
 * Enum for regular expressions pattern names
 * @author Israel Santiago... lol.
 */
public enum Patterns {
    /****
     * FIRST_NAME is used for the user first name does not allow any special characters, white space
     * or numbers
     * LAST_NAME is used for last name does not allow white space or numbers but it allows some
     * special characters apostrophes, single white space and single dash
     * PASSWORD is use for password strength see Validator class for explanation
     */
    FIRST_NAME,LAST_NAME,PASSWORD
}
