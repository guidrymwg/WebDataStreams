package com.lightcone.webdatastreams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class POSTexample extends Activity {

    // Set address for the questioner script on the server

    public static final String host_url =
            "http://csep10.phys.utk.edu/cgi-bin/quizforms/course1/questioner2.pl";  // Question script
    public static final String TAG = "WEBSTREAM";

    // Questioner data holders
    private String qnum;
    private String question;
    private String answer[] = new String[5];
    private String chapter;
    private TextView tv;
    private Bundle postData;
    ProgressBar progressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postexample);

        // Set up a TextView for screen output
        tv = (TextView) findViewById(R.id.TextView02);

        tv.setText(R.string.poststatus);

        // Set up the name-value data pairs that will be transmitted as part of the POST request
        // as elements of a Bundle. In this simple example there will be only one name-value
        // pair but the putString(label, value) can be repeated to put multiple name-value pairs
        // into the Bundle.

        postData = new Bundle();
        postData.putString("chapter", "4");

        progressBar = (ProgressBar) findViewById(R.id.POST_bar);

        // Execute the POST request on a background thread
        new BackgroundLoad().execute(host_url);
    }


    // Implement query by POST method and return the response as a string.

    private String doPOST(String host_url, Bundle data){

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(host_url);
        HttpResponse response = null;

        // Transfer the name-value pairs in the Bundle data to name-value pairs for
        // the POST access.  In our simple example we actually have only one name-value
        // pair, but the following loop is set up to handle an arbitrary number.

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        Object [] keyset = null;
        keyset = data.keySet().toArray();
        for(int i=0; i<keyset.length; i++){
            String key = keyset[i].toString();
            Log.i(TAG, "  data key = "+key);
            pairs.add(new BasicNameValuePair(key, data.getString(key) ));
        }

        // See http://www.softwarepassion.com/android-series-get-post-and-multipart-post-requests/

        UrlEncodedFormEntity ent = null;
        try {
            ent = new UrlEncodedFormEntity(pairs,HTTP.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        post.setEntity(ent);

        String postResponseString = null;
        int responseStatus = 0;
        try {
            response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            if(resEntity != null){
                // Extract the response status and the headers
                responseStatus = response.getStatusLine().getStatusCode();
                postResponseString = EntityUtils.toString(resEntity);
                Log.i(TAG, "\nPOST response and headers:" );
                Log.i(TAG, "\nResponse="+response.getStatusLine());
                Log.i(TAG, "Response code = "+responseStatus);
                Header [] hd = response.getAllHeaders();
                for(int i=0; i<hd.length; i++){
                    Log.i(TAG, "header="+hd[i].getName()+" value="+hd[i].getValue());
                }
                Log.i(TAG, "\nString returned from POST request:\n\n");
                Log.i(TAG, postResponseString);
            }
        } catch (ClientProtocolException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return postResponseString;
    }


    /** Method to process the string returned by the POST request to questioner2.pl.
     The response from the server is an ascii string of the form
     qnum=20
     question=An ellipse is a member of a general class of geometrical figures called
     answerA= ovals
     answerB= hyperbolas
     answerC= conic sections
     answerD= parabolas
     answerE= circles
     chapter=4
     coran= C
     amp= Amplifying remarks
     where the first line defines the question number, the second line defines the question, the
     next 5 lines define possible answers, the next line defines the chapter (of a book)
     that the question is associated with, the next line defines the correct answer, and the final
     line defines an (optional) amplifying remark on the correct answer.
     */

    private void postParse(String s){
        StringTokenizer st = new StringTokenizer(s,"\n");
        String ts;
        Log.i(TAG,"\nFrom Tokenizer:\n");
        qnum = st.nextToken();

        qnum = qnum.substring(qnum.indexOf("=")+1).trim();
        int iqnum = Integer.parseInt(qnum);
        question = st.nextToken();
        question = question.substring(question.indexOf("=")+1).trim();
        Log.i(TAG, "qnum="+iqnum);
        tv.append("\n\nqnum="+iqnum);
        Log.i(TAG, "question="+question);
        tv.append("\nquestion="+question);
        for(int i=0; i<5; i++){
            ts = st.nextToken();
            answer[i] = ts.substring(ts.indexOf("=")+1).trim();
            Log.i(TAG, "Answer["+i+"]: "+ answer[i]);
            tv.append("\nAnswer["+i+"]: "+ answer[i]);
        }
        chapter = st.nextToken();
        chapter = chapter.substring(chapter.indexOf("=")+1).trim();
        Log.i(TAG, "chapter="+chapter);
        tv.append("\nchapter="+chapter);
    }

    // Use AsyncTask to perform the web download on a background thread.  The three
    // argument types inside the < > are a type for the input parameters (Strings in this case),
    // a type for any published progress during the background task (Void in this case,  because
    // we aren't going to publish progress since the task should be very short), and a type
    // for the object returned from the background task (in this case it is type String).

    private class BackgroundLoad extends AsyncTask <String, Void, String>{

        // Executes the task on a background thread
        @Override
        protected String doInBackground(String... params) {

            // The notation String... params means that the input parameters are an array of
            // strings.  In new BackgroundLoad().execute(host_url) above we are
            // passing just one argument, so params[0] will correspond to host_url.

            return doPOST(params[0], postData);

        }

        // Executes before the thread run by doInBackground
        protected void onPreExecute () {

        }

        // Executes after the thread run by doInBackground has returned. The variable s
        // passed is the string value returned by doInBackground.

        @Override
        protected void onPostExecute(String s){

            // Stop the progress dialog
            progressBar.setVisibility(View.GONE);

            // Process the response
            postParse(s);
        }
    }
}
