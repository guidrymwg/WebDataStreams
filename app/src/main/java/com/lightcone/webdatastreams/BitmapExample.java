package com.lightcone.webdatastreams;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class BitmapExample extends Activity {

    public static final String TAG = "WEBSTREAM";
    ImageView imageview;
    private static final String URL =
            //"http://heritage.stsci.edu/2001/24/mars/0124b.jpg";  // Mars
    "http://heritage.stsci.edu/1999/01/images/9901b.jpg";  // Ring Nebula

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bitmapexample);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        // Execute on a background thread using AsyncTask
        new BackgroundLoad().execute(URL);
    }

    // Method to load bitmap from web
    private Bitmap loadBitmap (String url){
        Bitmap image = null;
        InputStream inStream = null;
        try {
            inStream = openHttpConnection(url);
            image = BitmapFactory.decodeStream(inStream);
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // Method adapted from example in "Beginning Android Application Development", W-M. Lee,
    // pp. 284-287.

    private InputStream openHttpConnection(String urlString)  throws IOException {
        int responseCode = -1;
        InputStream inStream = null;
        URL url = new URL(urlString);
        URLConnection uconn = url.openConnection();
        if(!(uconn instanceof HttpURLConnection)){
            throw new IOException("Not a valid HTTP connection");
        }
        try{
            HttpURLConnection httpConn = (HttpURLConnection) uconn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            responseCode = httpConn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                inStream = httpConn.getInputStream();
            }
        } catch( Exception e) {
            throw new IOException("Connection Error");
        }
        return inStream;
    }


    /** Subclass AsyncTask to download image on a background thread.  The three argument types
     <Params, Progress, Result> are (1) a type for the input parameters (String in this case),  (2) a
     type for any published progress during the background task (String in this case), and (3) a type
     for the objects returned from the background task (if any; in this case it is a Bitmap).  Each of
     these three arguments stands for an array of the corresponding type, so it is possible
     to pass multiple arguments of each kind. If one of the argument types is not needed, specify
     it by Void.  For example, if our task used one or more String input parameters but did not
     publish progress and did not return anything the pattern would be <String, Void, Void>, where
     String implies an array of input strings. It is convenient to implement AsyncTask as an inner
     class (as we do here) because this gives it access to the fields and methods of the
     enclosing class. Must be launched from the UI thread and may only be invoked once.  Use
     new BackgroundLoad().execute(arg);
     to launch it, where arg is the String argument passed to AsyncTask. This constructor returns
     itself (this), so you could also retain a reference to the AsyncTask object by invoking instead
     AsyncTask<String, String, Bitmap> at = new BackgroundLoad().execute(arg);
     for the present example. */


    private class BackgroundLoad extends AsyncTask <String, String, Bitmap>{

        // Executes a task on a background thread that is managed automatically by the system.
        // Note: since this is a background  thread, we are strictly forbidden to touch any views on
        // the main UI thread directly from this method.

        @Override
        protected Bitmap doInBackground(String... url) {

            // The notation String... url means that the input parameters are an array of
            // strings (in principle), but we are only passing one argument in the
            // new BackgroundLoad().execute(URL) statement above, so we use only
            // url[0] in the following.

            // Optional: publish progress. This will cause onProgressUpdate to run on the main UI thread,
            // with the argument of publishProgress passed to it.

            publishProgress("Starting image load on background thread\nURL="+url[0]);

            // Retrieve the bitmap from the specified url
            Bitmap bitmap = loadBitmap(url[0]);
            return bitmap;
        }

        // Executes on the main UI thread before the thread run by doInBackground does.  Since
        // it executes on the main UI thread we are free to interact with views on the main
        // thread from this method. This is the place to do any setup required before the task on
        // the background thread runs.  In this example, we use it to launch a progress dialog
        // that will indicate to the user that work is being done while the background task is running.


        @Override
        protected void onPreExecute () {

        }

        // Override onProgressUpdate to publish progress while the background thread is still running.
        // This runs on main UI thread after publishProgress method invoked on background thread in
        // doInBackground.  Here we do something fairly trivial by sending to the logcat stream a
        // message indicating that we have started processing on the background thread and
        // the url it is using.  A more common real-life application of this method would be to use say
        // onProgressUpdate(Integer ... prog) to update a progress bar with the fraction of
        // the job completed. (Then you would have to change the AsyncTask parameter pattern for this
        // example to <String, Integer, Bitmap> and change the publishProgress method in
        // doInBackground accordingly.)  Note that this method runs on the main UI thread so it is
        // permitted to interact directly with the views there.

        @Override
        protected void onProgressUpdate(String... progress) {
            Log.i(TAG, "---onProgressUpdate---");
            Log.i(TAG, progress[0]);
        }

        // Executed after the thread run by doInBackground has returned. The variable bitmap
        // passed is the bitmap returned by doInBackground.  This method executes on
        // the main UI thread, so we are free to interact with views on the main thread from
        // here.

        @Override
        protected void onPostExecute(Bitmap bitmap){

            // Stop the progress dialog

            progressBar.setVisibility(View.GONE);

            Log.i(TAG, "Thread finished.  Displaying image.");
            // Display the retrieved image as an ImageView
            imageview = (ImageView) findViewById(R.id.image);
            imageview.setImageBitmap(bitmap);
        }
    }
}
