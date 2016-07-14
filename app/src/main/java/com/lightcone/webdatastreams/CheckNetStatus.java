package com.lightcone.webdatastreams;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CheckNetStatus extends AppCompatActivity {

    private static final String TAG = "WEBSTREAM";
    private static final String URL ="";
    private static TextView tv;
    public Bundle netstat;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checknetstatus);

        tv = (TextView) findViewById(R.id.TextView1);

        // We will do the network accesses for the check on a background thread by subclassing
        // AsyncTask.

        // Create a progress spinner
        progressBar = (ProgressBar) findViewById(R.id.network_bar);

        // Execute on a background thread
        new BackgroundLoad().execute(URL);
    }

    // Method to return network and server connections status

    public Bundle networkStatus(){

        Bundle netStatus = new Bundle();
        Boolean wifiConnected = null;
        Boolean phoneConnected = null;
        String ipNumber;
        NetworkInfo netInfo;

        // Get a connectivity manager instance
        ConnectivityManager conman = (ConnectivityManager) getSystemService (
                Context.CONNECTIVITY_SERVICE);

        netInfo = conman.getActiveNetworkInfo();

        // Check wifi status
        if(netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            wifiConnected = netInfo.isConnected();
            Log.i(TAG,"Network Type:  "+netInfo.getTypeName());
        }

        // Check telephony status
        if(netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            phoneConnected = netInfo.isConnected();
        }

        ipNumber = getLocalIpAddress();

        // Add network status variable values to the Bundle netStatus

        if(wifiConnected != null) netStatus.putBoolean("wifiConnected", wifiConnected);
        if(phoneConnected != null)netStatus.putBoolean("phoneConnected", phoneConnected);
        if(ipNumber != null)netStatus.putString("ipNumber", ipNumber);

        return netStatus;
    }

    // Method to find local ip address of the device on wifi or mobile.
    // See http://chandan-tech.blogspot.com/2010/12/finding-ip-address-of-your-android.html
    // and http://stackoverflow.com/questions/32141785/android-api-23-inetaddressutils-replacement

    public static String getLocalIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }

        } catch (Exception ex) {

        }
        return null;
    }


    /**  Use AsyncTask to perform the network checks on a background thread.  The three
     argument types inside < > are (1) a type for the input parameters (String in this case),
     (2) a type for published progress during the background task (String in this case,
     since we will illustrate by publishing a short string), and (3) a type
     for the object returned from the background task (in this case it is a Bundle). If one of
     the argument types is not needed, specify it by Void.  For example, if our task used one or
     more String input parameters but did not publish progress and did not return anything
     the pattern would be <String, Void, Void>, where String implies an array of input strings.
     It is convenient to implement AsyncTask as an inner class (as we do here) because this
     gives it access to the fields and methods of the enclosing class.  */

    private class BackgroundLoad extends AsyncTask <String, String,  Bundle> {

        // Executes the task on a background thread.  Note: since this is a background
        // thread, we are strictly forbidden to touch any views on the main UI thread from this
        // method.

        @Override
        protected Bundle doInBackground(String... url) {

            Log.i(TAG, "---doInBackground---");

            // The notation String... url means that the input parameters are an array of
            // strings.  However, we are only passing one argument in the
            // new BackgroundLoad().execute(URL) statement above, so we use only
            // url[0] in the following.

            // Publish progress.  This will cause onProgressUpdate to run on the main UI thread,
            // with the argument of publishProgress passed to it.

            publishProgress("\n\nStarting background thread\n");

            return networkStatus() ;
        }

        // Executes on the main UI thread before the thread run by doInBackground.  Since
        // it executes on the main UI thread we are free to interact with views on the main
        // thread from this method.

        protected void onPreExecute () {
            Log.i(TAG, "\n---onPreExecute---");
        }

        // Override onProgressUpdate to publish progress while the background thread is running.
        // This runs on the main UI thread after the publishProgress method is invoked in
        // doInBackground.  Here we do something fairly trivial by sending to the screen a
        // message indicating that we have started processing on the background
        // thread.  A more common real-life application would be to use say
        // onProgressUpdate(Integer ... prog) to update a progress bar with the fraction of
        // the job completed.  Note that since this method runs on the main UI thread it can
        // interact directly with the views there.

        protected void onProgressUpdate(String... progress) {
            Log.i(TAG, "---onProgressUpdate---");
            tv.append(progress[0]);
        }


        // Executed after the thread run by doInBackground has returned. The Bundle data
        // passed are the Bundle returned by doInBackground. This method executes on
        // the main UI thread so we are free to interact with views on the main thread from
        // here.

        @Override
        protected void onPostExecute(Bundle data){

            // Stop the progress dialog

            progressBar.setVisibility(View.GONE);

            Log.i(TAG, "---onPostExecute---");

            // Process and display results in the Bundle returned from the background thread,
            // extracting information from the Bundle as a string displayed to screen.

            netstat = data;
            String net1 = " phone connected = "+netstat.getBoolean("phoneConnected");
            String net2 = " wifi connected = "+netstat.getBoolean("wifiConnected");
            String net3 = " IP = "+netstat.getString("ipNumber");
            String netString = net1+"\n"+net2+"\n"+net3;
            Log.i(TAG, netString);
            tv.append("\n"+netString);
        }
    }
}
