package com.lightcone.webdatastreams;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class CheckNetStatus extends AppCompatActivity {

    private static final String TAG = "WEBSTREAM";
    // Examples: "127.0.0.1"(= "localhost"); "csep10.phys.utk.edu"; "74.125.47.103"; "google.com";
    private static final String URL ="google.com";
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
    // See Darcey and Condor, 2nd ed. p. 297


    public Bundle networkStatus(String serverURL){

        Bundle netStatus = new Bundle();
        Boolean wifiAvailable = null;
        Boolean wifiConnected = null;
        Boolean phoneAvailable = null;
        Boolean phoneConnected = null;
        String ipNumber = null;
        int ipInteger = 0;
        NetworkInfo netInfo = null;
        InetAddress inetAddress = null;

/*        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Toast.makeText(this, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                Toast.makeText(this, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // not connected to the internet
        }*/

        // Get a connectivity manager instance
        ConnectivityManager conman = (ConnectivityManager) getSystemService (
                Context.CONNECTIVITY_SERVICE);

        netInfo = conman.getActiveNetworkInfo();

        // Check wifi status
        //netInfo = conman.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            wifiAvailable = netInfo.isAvailable();
            wifiConnected = netInfo.isConnected();
            Log.i(TAG,"Network Type:  "+netInfo.getTypeName());
        }

        // Check telephony status
        //netInfo = conman.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            phoneAvailable = netInfo.isAvailable();
            phoneConnected = netInfo.isConnected();
        }
        try {
            inetAddress = InetAddress.getByName(serverURL);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ipNumber = inetAddress.getHostAddress();
        //Log.i(TAG, "ipNumber="+ipNumber);

        ipInteger = lookupHost(serverURL);
        //Log.i(TAG, "ipInteger="+ipInteger);

        // Add status booleans to the Bundle netStatus

        if(wifiAvailable != null) netStatus.putBoolean("wifiAvailable", wifiAvailable);
        if(wifiConnected != null) netStatus.putBoolean("wifiConnected", wifiConnected);
        if(phoneAvailable != null)netStatus.putBoolean("phoneAvailable", phoneAvailable);
        if(phoneConnected != null)netStatus.putBoolean("phoneConnected", phoneConnected);
        if(ipNumber != null)netStatus.putString("ipNumber", ipNumber);
        netStatus.putInt("ipInteger", ipInteger);

        // Works for wifi but not needed since getLocalIpAddress will work for wifi or mobile.
        /*WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        Log.i(TAG,"WIFI IP="+ipAddress);*/

        Log.i(TAG, "inetAddress="+getLocalIpAddress());

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
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }

        } catch (Exception ex) {

        }
        return null;
    }


    /**  Method to look up an ip address and convert it to an integer.
     Following seems to have a basic problem?  It returns integers but an IP address
     aaa.bbb.ccc.ddd with aaa larger than 127 tends to exceed the maximum value
     for an integer (2^31 - 1 = 2147483647).  But methods like requestRouteToHost
     (int networkType, int hostAddress) from the ConnectivityManager class
     take an int argument for the ip hostAddress.  Something doesn't add up. Seems like
     it should be a long int,  but the API doesn't support that.

     The problem is related to all integers in Java being signed.  See

     http://stackoverflow.com/questions/11088/what-is-the-best-way-to-work
     -around-the-fact-that-all-java-bytes-are-signed

     http://www.darksleep.com/player/JavaAndUnsignedTypes.html

     http://www.jguru.com/faq/view.jsp?EID=13647

     I would assume that the solution to the problem might lie in some manipulation of the
     sort described in these links to convert the negative integers that you get if say  the ip
     address aaa.bbb.ccc.ddd has aaa greater than 127 to the correct positive value that
     you would get if you did the conversion on a calculator.  But I'm still not sure how that
     solves the specific problem, because the method requestRouteToHost wants an
     int argument, and the correct positive value for the above example will be flagged as
     beyond the range for ints.  As a consequence, I don't use requestRouteToHost in the
     following.  */

    public static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
            Log.i(TAG, "inetAddress="+inetAddress);
            String ipNumber = inetAddress.getHostAddress();
            Log.i(TAG, "ipNumber="+ipNumber);
            try {
                Log.i(TAG, "isReachable="+inetAddress.isReachable(5000));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            return -1;
        }

        // Convert IP address to Byte array
        byte [] addBytes;
        int add;
        //Log.i(TAG, "***** inetAddress ="+inetAddress);
        addBytes = inetAddress.getAddress();

        for(int i=0; i<addBytes.length; i++){
            Log.i(TAG, "  Byte "+i+"="+addBytes[i]);
        }
        //Log.i(TAG, "\ninetAddress: "+inetAddress.getHostAddress());

        // Convert ip dot-form address to ip integer. Copied from
        // http://stackoverflow.com/questions/2295998/requestroutetohost-ip-argument
        // but with the byte order reversed.

        add = ((addBytes[0] & 0xff) << 24)
                | ((addBytes[1] & 0xff) << 16)
                | ((addBytes[2] & 0xff) << 8)
                |  (addBytes[3] & 0xff);

        Log.i(TAG, "IPinteger: "+add);

        // To check values, see http://www.aboutmyip.com/AboutMyXApp/IP2Integer.jsp.
        // Above is equivalent to the more pedestrian
        //     addr=16777216*addBytes[0]+65536*addBytes[1]+256*addBytes[2]+addBytes[3];
        // where 16777216=256^3 and 65536=256^2

        return add;
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

            return networkStatus(url[0]) ;
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
            String net1 = " phone available = "+netstat.getBoolean("phoneAvailable");
            String net2 = " phone connected = "+netstat.getBoolean("phoneConnected");
            String net3 =  " wifi available = "+netstat.getBoolean("wifiAvailable");
            String net4 = " wifi connected = "+netstat.getBoolean("wifiConnected");
            String net5 = " IP = "+netstat.getString("ipNumber");
            String net6 = " IPinteger = "+netstat.getInt("ipInteger");
            String netString = net1+"\n"+net2+"\n"+net3+"\n"+net4+"\n"+net5+"\n"+net6;
            Log.i(TAG, netString);
            tv.append("\n"+netString);
        }
    }
}
