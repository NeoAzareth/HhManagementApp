package com.IsraelSantiago.HhManagementApp.model;

import android.app.Activity;

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
 * DataHolder.class
 *
 * simple class that serves to store application data
 *
 * @author Israel Santiago
 * @version 1.1
 */
public class DataHolder {

    //store data on this application
    private Member member;
    private Household household;
    private Activity activity;

    //getters and setters
    public Member getMember() {return member;}
    public void setMember(Member member) {this.member = member;}

    public Household getHousehold() {return household;}
    public void setHousehold(Household household) {this.household = household;}

    public Activity getActivity(){return activity;}
    public void setActivity(Activity activity){this.activity = activity;}

    private static final DataHolder holder = new DataHolder();
    public static DataHolder getInstance() {return holder;}
}
