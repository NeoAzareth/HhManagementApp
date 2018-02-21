package com.IsraelSantiago.HhManagementApp.controller;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.IsraelSantiago.HhManagementApp.R;
import com.IsraelSantiago.HhManagementApp.model.DataHolder;
import com.IsraelSantiago.HhManagementApp.model.Member;

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
 * MenuActivity.class
 *
 * Simple menu activity used to reduce the need to add this code to each activity.
 *
 * Any activity that needs access to the menu should extend this activity.
 *
 * e.g.
 * The overview activity requires access to the menu so instead of
 *
 * public class OverviewActivity extends Activity {
 *     etc, etc...
 * }
 *
 * we extend this class:
 *
 * public class OverviewActivity extends MenuActivity {
 *     etc, etc...
 * }
 *
 * any class that extends this class will have access to the menu inflater
 *
 * @author Israel Santiago
 * @version 1.0
 */
public abstract class MenuActivity extends Activity {

    Intent intent;

    // This method create menu
    // Depends on user's role (admin or general member), show corresponding menu
    // The main difference of two menus is general member won't see admin option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Member currentUser = DataHolder.getInstance().getMember();
        if(currentUser.getUserLevel().equals("admin")){
            getMenuInflater().inflate(R.menu.admin_menu, menu);
        } else if (currentUser.getUserStatus().equals("not in") ||
                currentUser.getUserStatus().equals("pending")) {
            getMenuInflater().inflate(R.menu.pending_member_menu,menu);
        } else
            getMenuInflater().inflate(R.menu.member_menu, menu);

        return true;
    }

    // This method handle user's click on a specific item
    // Redirect to corresponding activity once one item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.overview_menu_item:
                intent = new Intent(getApplicationContext(),OverviewActivity.class);
                startActivity(intent);
                break;
            case R.id.no_household_item:
                intent = new Intent(getApplicationContext(),NoHouseholdActivity.class);
                startActivity(intent);
                break;
            case R.id.manage_bills_menu_item:
                intent = new Intent(getApplicationContext(),ManageBillsActivity.class);
                startActivity(intent);
                break;
            case R.id.report_menu_item:
                intent = new Intent(getApplicationContext(),ReportActivity.class);
                startActivity(intent);
                break;
            case R.id.settings_menu_item:
                intent = new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.admin_menu_item:
                intent = new Intent(getApplicationContext(),AdminActivity.class);
                startActivity(intent);
                break;
            case R.id.log_out_menu_item:
                DataHolder.getInstance().getMember().logout();
                intent = new Intent(getApplicationContext(),LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
