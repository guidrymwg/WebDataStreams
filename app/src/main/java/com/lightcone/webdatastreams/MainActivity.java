package com.lightcone.webdatastreams;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements  android.view.View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Click listeners for all buttons
        View checkButton = findViewById(R.id.check_button);
        checkButton.setOnClickListener(this);
        View getButton = findViewById(R.id.GET_button);
        getButton.setOnClickListener(this);
/*        View postButton = findViewById(R.id.POST_button);
        postButton.setOnClickListener(this);*/
        View bitmapButton = findViewById(R.id.bitmap_button);
        bitmapButton.setOnClickListener(this);
    }

    // Process the button clicks
    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.check_button:
                Intent i = new Intent(this, CheckNetStatus.class);
                startActivity(i);
                break;

            case R.id.GET_button:
                Intent j = new Intent(this, GETexample.class);
                startActivity(j);
                break;

/*            case R.id.POST_button:
                //Intent k = new Intent(this, POSTexample.class);
                //startActivity(k);
                break;*/

            case R.id.bitmap_button:
                Intent m = new Intent(this, BitmapExample.class);
                startActivity(m);
                break;

        }
    }
}
