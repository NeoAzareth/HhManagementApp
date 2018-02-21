package com.IsraelSantiago.HhManagementApp.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.util.Log;


/***
 * DBConnection.class
 *
 * This class is in charge of communicating with PHP scripts located at the ROOT_PATH provided
 * For security reasons some variables are left blank
 *
 * @author Israel Santiago
 * @version 2.0
 */
public class DBConnection extends AsyncTask<String,Void,String>{

    //empty constructor
    DBConnection(){}

    @Override
    protected void onPreExecute(){
        if(!Utilities.isNetworkAvailable(DataHolder.getInstance().getActivity())){
            this.cancel(true);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        return dbTransaction(params[1],params[0]);
    }

    @Override
    protected void onCancelled(){
        Utilities.showToast("There is no internet connection \n"+
        "Please check your settings and try again later...");
    }

    @Override
    protected void onPostExecute(String result){
        Log.d("DbTransaction result: ", result);
    }

    /**
     * dbTransaction handles all db communications
     * @param fileName string, one the file name constants on the ServerStrings
     * @param postData; the data that is passed to page through the $_POST super global
     * @return DB info or an error handling string
     */
    String dbTransaction(String fileName, String postData) {
        String fullURL = ServerStrings.ROOT_PATH + fileName;
        Log.d("DBTransaction ", "connecting to " + fullURL);

        //the method attempts DB connection
        try{
            //this line is used for registration only
            if (fileName.equals(ServerStrings.REGISTER) || fileName.equals(ServerStrings.SERVICE)
                    || fileName.equals(ServerStrings.PW_RESET) || fileName.equals(ServerStrings.VERIFY)) {
                //adds a keycode to the post data to be verified by the script and allow
                //registration
                postData += "&" + Utilities.encodeData(ServerStrings.KEYCODE_KEY,ServerStrings.KEY_CODE);
            }

            //the page where I took these lines do not explain what exactly they do
            //the link to the tutorial is in the project document
            //I have a vague idea of what is going on but I don't want to mislead
            URL url = new URL(fullURL);//set the link to communicate

            //open connection
            URLConnection conn = url.openConnection();

            //I think this sets a variable to true that means data is going to be past to through
            //the post into the PHP page/script
            conn.setDoOutput(true);

            //object to past the post data
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            //past the data
            wr.write(postData);

            //delete data
            wr.flush();

            //object that reads the data
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            //data is stored in a string
            String result = reader.readLine();

            Log.d("DBTransaction ", "Result "+ result);
            //result is returned
            return result;
        } catch (Exception e){
            //if any of the steps fails an exception is catch and the error is retrieved in the log
            Log.d("DBConnection",e.toString());
            return "Connection failed";
        }
    }
}
