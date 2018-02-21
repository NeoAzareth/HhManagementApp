# HouseholdManagement / BillTrack

A more organized version of the original work by Seattle Central College students -2016

Original contributors:
<ul>
Colin Lin, colinhx@gmail.com
Sicheng Zhu, szhu0007@seattlecentral.edu
</ul>
Israel Santiago, neoazareth@gmail.com

Rework contributors:
<li>
Sicheng Zhu, szhu0007@seattlecentral.edu
<li>
Israel Santiago, neoazareth@gmail.com

GitHub link to previous version:
https://github.com/sicheng-zhu/HouseholdManagement

Android App translation of “original” project by Israel Santiago's Household Management Web App -2015
Link to deployed application https://neoazareth.com/HHManageWebApp/index.php
GitHub repository coming soon...

As the previous version README.md especifies, the intention of the app is to allow user to manage their expenses 
and produce a spreadsheet with all the bills associated with the current month.

Key features:

1. User registration - only available through the Android app.
2. Create a household -creator becomes the Admin of such household; or Join an existing household -user becomes a member.
3. Provides an overview of the household status -current bills and other user’s status.
4. Allows users to add, edit or delete current month bills.
5. Create customized reports based on user, category and date (this version provides a list of only dates which have 
bills associated with them).
6. Allows users to change their password within the app and leave/delete their current household.
7. Admin of household has the authority to manage other members or update the household rent.
8. Password retrieval through user’s email. 
9. Notifies the user when a new months has begun and prompts them to add their bills
10. Creates and emails all the users of a household a spreadsheet with the household report once all the users are done 
adding bills.

Neither the Web App nor the Android app longer supports mobile numbers. The rework does not allow for landscape 
views.

This app is available on Google Play(https://play.google.com/store/apps/details?id=com.householdmanagement&hl=en). 
We have a web version (www.neoazareth.com/HHManageWebApp).

Here is a test account if you want to try: Username: zsc@uw.edu Password: Sicheng6625

Final notes:

This rework was intentionally left with various ways of coding and producing the views; it was a way to provide a 
perspective to the approach of each contributor to complete a certain aspect of the application. 

Originally the intent was solve an issue that I (Israel) had within my household. Every month I had to request my 
family members to provide me with their monthly expenses so that I could balance the month and distribute an spreadsheet 
that contains everyone's bills and the fair amount that corresponds to all of us. 

During the reworking of the application, many inherent flaws of the original idea surfaced; however, only time will tell 
if they are worth addressing. One of such flaws is the spreadsheet report mailed to the users, or the way the application 
auto resets to allow user to add expenses to the next period. That being said, the app was intended as a way to practice 
collaboration as well as dealing with issues that may arise from such collaboration. All aspects of the application are 
fully functional regardless of how useful they really are.

Additional Credits to implement the notifications:

Rakesh Cusat(I believe he is the Author, if not I apologize...). <
https://www.javacodegeeks.com/2012/09/android-alarmmanager-tutorial.html

As well as Jonathan Hasenzahl, James Celona, Dhimitraq Jorgji. \n
https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/

PHPMailer and all their contributors. \n
https://github.com/PHPMailer/PHPMailer

PHPExcel and all their contributors. \n
https://github.com/PHPOffice/PHPExcel

And SimpleTest. \n
http://www.simpletest.org/

Additional thanks to all of those that help the community, we (or at least me) used search engines (google) to search 
and solve many issues as they arose. 
