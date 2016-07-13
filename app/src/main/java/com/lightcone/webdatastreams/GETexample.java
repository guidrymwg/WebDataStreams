package com.lightcone.webdatastreams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class GETexample extends Activity {

    private static final String TAG = "WEBSTREAM";

    // Following url address accesses a deprecated Google search API.  See
    // http://code.google.com/apis/websearch/docs/ for documentation and suggested
    // replacement.  We're just using it for illustration of the GET method here.

    // This returns random numbers
    //https://www.random.org/integers/?num=1&min=1&max=10&col=1&base=10&format=plain&rnd=new

    private String getURL = "https://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
    private String searchString = "butterfly";
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
            // will correspond to searchString.

            String GETResponseString = null;

            // Sample GET method. See
            // http://www.softwarepassion.com/android-series-get-post-and-multipart-post-requests/

            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(params[0] + params[1]);
                HttpResponse responseGet = client.execute(get);
                HttpEntity resEntityGet = responseGet.getEntity();
                GETResponseString = EntityUtils.toString(resEntityGet);
                if (resEntityGet != null) {
                    Log.i(TAG, "\nGET response:");
                    Log.i(TAG, GETResponseString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            // Have to catch following because they are thrown in method processResponse(s)
            try {
                wv.loadData(parseJSON(s), "text/html", "utf-8");
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
