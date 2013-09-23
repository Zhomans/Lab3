package com.mobileproto.lab3;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity {
    double prevLat, prevLong;
    double prevTime, curTime;
    double curLat, curLong;
    double gps_velocity;
    boolean kill;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);}catch (Exception e){}

        //Go to Second Activity: Map
        Button goMap = (Button) findViewById(R.id.map_button);
        goMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kill = true;
                Intent i = new Intent(getApplicationContext(), PulseActivity.class);
                startActivity(i);
            }
        });

        // explicitly enable GPS
        Intent enableGPS = new Intent(
                "android.location.GPS_ENABLED_CHANGE");
        enableGPS.putExtra("enabled", true);
        sendBroadcast(enableGPS);

        //Initialize GPS and grab views
        final GPS gps = new GPS(this);
        final TextView velocity = (TextView) findViewById(R.id.velocity_display);
        final TextView location = (TextView) findViewById(R.id.gps_display);

        //Grab Initial Data
        prevLat = gps.getLatitude();
        prevLong = gps.getLongitude();
        prevTime = SystemClock.uptimeMillis();

        //Thread
        Thread vel = new Thread(){
            public void run(){
                try {
                    while(kill){
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                curLat = gps.getLatitude();
                                curLong = gps.getLongitude();
                                curTime = SystemClock.uptimeMillis();


                                gps_velocity = Math.sqrt((curLat-prevLat)*(curLat-prevLat)+(curLong-prevLong)*(curLong-prevLong))*6371000.0/((curTime-prevTime)/1000.0);

                                location.setText("Lat:" + String.valueOf(curLat) + "\n Long:" + String.valueOf(curLong));
                                Log.e("Latitude",  String.valueOf(curLat));
                                Log.e("Longitude", String.valueOf(curLong));
                                Log.e("Velocity",  String.valueOf(gps_velocity));

                                velocity.setText(String.valueOf(gps_velocity) + " m/s");
                            }});Thread.sleep(100);

                            //Update website with information here
                            TelephonyManager tMgr =(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            String phoneName = tMgr.getDeviceId();

                            try {
                                URL url = new URL("http://10.41.88.218:5000/post?data" + phoneName + "-" + String.valueOf(curLat) + "-" + String.valueOf(curLong) + "-" + String.valueOf(gps_velocity));
                                url.openConnection();
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }catch (InterruptedException e){Log.v("ServerThread","Stopped");}
                }};vel.start();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
