package com.lightcone.webdatastreams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class GETexample extends AppCompatActivity {

    private static final String TAG = "WEBSTREAM";

    // Range for random integer generator
    private static final int num = 10;       // Number of random integers
    private static final int lower = 0;     // Lower limit of interval
    private static final int upper = 100;   // Upper limit of interval


    // Following url address accesses a deprecated Google search API.  See
    // http://code.google.com/apis/websearch/docs/ for documentation and suggested
    // replacement.  We're just using it for illustration of the GET method here.

    // This returns random numbers
    //https://www.random.org/integers/?num=1&min=1&max=10&col=1&base=10&format=plain&rnd=new
    //https://developer.android.com/reference/packages.html#q=runtime%20permissions
    // http://simbad.u-strasbg.fr/simbad/sim-basic?Ident=large+magellanic+cloud&submit=SIMBAD+search

    //private String getURL = "http://simbad.u-strasbg.fr/simbad/sim-basic?Ident=Large+Magellanic+Cloud&submit=SIMBAD+search";
    String url = "https://www.random.org/integers/?num="+num+"&min="+lower+"&min=1&max="
            +upper+"&col=1&base=10&format=plain&rnd=new";
    private String getURL=url;
    //private String getURL = "https://www.random.org/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new";
    private String searchString = "";
    //private String getURL = "https://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
    //private String searchString = "butterfly";
    ProgressBar progressBar;


    Context context = this;   // Store this context for webview invoked later from inner class

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.getexample);

        // Execute the GET request on a background thread
        progressBar = (ProgressBar) findViewById(R.id.GET_bar);
        try {
            new BackgroundLoad().execute(getURL, URLEncoder.encode(searchString, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // JSON parser, returning HTML formatted string. Adapted from The Android Developer's
    // Cookbook, J. Steele and N. To, p. 208.

    public String parseJSON (String resp) throws IllegalStateException,
            IOException, JSONException, NoSuchAlgorithmException {

        StringBuilder stringBuilder = new StringBuilder();
        JSONObject response = new JSONObject(resp).getJSONObject("responseData");
        JSONArray array = response.getJSONArray("results");
        for(int i=0; i<array.length(); i++){
            String title = array.getJSONObject(i).getString("title");
            String url = array.getJSONObject(i).getString("url");
            String visibleUrl = array.getJSONObject(i).getString("visibleUrl");
            stringBuilder.append("<p>"+title+"\n");
            stringBuilder.append(" <a href=\""+url+"\">");
            stringBuilder.append("<em>"+visibleUrl+"</em></a></p>");
        }
        Log.i(TAG,"JSON="+stringBuilder.toString());
        return stringBuilder.toString();
    }

    // Example of using HttpURLConnection for a GET request.  The string getURL
    // is assumed to give the full url with the appended data payload (with the data
    // entries URLEncoded where necessary).  For example,
    //   https://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=butterfly
    // The result of the web request is returned as a string by this method.

    public String getRequest(String getURL){
        URL url = null;
        String result = null;
        try {
            url = new URL(getURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream in = null;
            try {
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            result = readStream(in);
        } finally {
            // Disconnecting releases resources held by connection so they can be closed or reused.
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return result;
    }


    // Reader for the GET response stream
    private String readStream(InputStream is){

        // Begin reading the GET input stream line by line
        Log.i(TAG, "\n\nBegin reading GET input stream");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size
        String total = "";

        try {
            String test;
            while (true){
                test = br.readLine();
                if(test == null) break;    // readLine() returns null if no more lines
                Log.i(TAG, test);
                total += test+"&nbsp;";
            }
            isr.close();
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "\n\nThat is all" );
        Log.i(TAG, "\n test="+total);
        return total;
    }


    // Use AsyncTask to perform the web download on a background thread.  The three
    // argument types inside the < > are (1) a type for the input parameters (Strings in this case),
    // (2) a type for any published progress during the background task (Void in this case,  because
    // we aren't going to publish progress since the task should be very short), and (3) a type
    // for the object returned from the background task (in this case it is type String).

    private class BackgroundLoad extends AsyncTask <String, Void, String>{

        // Executes the task on a background thread
        @Override
        protected String doInBackground(String... params) {

            // The notation String... params means that the input parameters are an array of
            // strings.  In new BackgroundLoad().execute(getURL, searchString) above we are
            // passing two arguments, so params[0] will correspond to getURL and and params[1]
            // will correspond to the search string.

            String GETResponseString = null;

            String address = null;
            try {
                address = params[0] + URLEncoder.encode(params[1], "ASCII");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Search ="+params[1]);
            Log.i(TAG, "Address = "+address);
            GETResponseString = getRequest(address);
            return GETResponseString;
        }

        // Executes before the thread run by doInBackground

        protected void onPreExecute () {

        }

        // Executed after the thread run by doInBackground has returned. The variable s
        // passed is the string value returned by doInBackground.

        @Override
        protected void onPostExecute(String s){

            // Stop the progress dialog

            progressBar.setVisibility(View.GONE);

            Log.i(TAG, "Thread finished.  Displaying content as webview.");
            // Display the response in a webview
            WebView wv = new WebView(context);
            setContentView(wv);

            // Display the result in an html format

            s="<p>"+num+" integers chosen randomly between 0 and 100:</p>" +
                    "<center><h3><pre>"+s+"</pre></h3></center>";
            s+="Source: <em>https://www.random.org</em>";
            wv.loadData(s, "text/html", "utf-8");

        }
    }
}
