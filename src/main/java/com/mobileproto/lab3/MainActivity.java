package com.mobileproto.lab3;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.StrictMode;
import android.os.SystemClock;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try{
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        }catch (Exception e){}

        Button sendPulse = (Button) findViewById(R.id.pulse_button);

        // explicitly enable GPS
        Intent enableGPS = new Intent(
                "android.location.GPS_ENABLED_CHANGE");
        enableGPS.putExtra("enabled", true);
        sendBroadcast(enableGPS);

        final GPS gps = new GPS(this);
        final TextView velocity = (TextView) findViewById(R.id.velocity_display);
        final TextView location = (TextView) findViewById(R.id.gps_display);

        prevLat = gps.getLatitude();
        prevLong = gps.getLongitude();
        prevTime = SystemClock.uptimeMillis();



        sendPulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gps.canGetLocation()){
                    location.setText("Lat:" + String.valueOf(gps.getLatitude()) + "\n Long:" + String.valueOf(gps.getLongitude()));}
            };
        });



        Thread vel = new Thread(){
            public void run(){
                try {
                    while(!isInterrupted()){
                        curLat = gps.getLatitude();
                        curLong = gps.getLongitude();
                        curTime = SystemClock.uptimeMillis();
                        gps_velocity = Math.sqrt(((curLat - prevLat) / (curTime - prevTime)) * ((curLat - prevLat) / (curTime - prevTime)) + ((curLong - prevLong) / (curTime - prevTime)) * ((curLong - prevLong) / (curTime - prevTime)));
                        location.setText("Lat:" + String.valueOf(curLat) + "\n Long:" + String.valueOf(curLong));
                        Log.e("Latitude",  String.valueOf(curLat));
                        Log.e("Longitude", String.valueOf(curLong));
                        Log.e("Velocity",  String.valueOf(gps_velocity));
                        velocity.setText(String.valueOf(gps_velocity) + " deg/s");
                        Thread.sleep(100);
                    }
                }catch (InterruptedException e){};
            }};vel.start();

        Thread server = new Thread(){
            public void run(){
                try {
                    URL url = new URL("http://www.vogella.com");
                    final HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    while (!isInterrupted())
                       {readData(con.getInputStream());}
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }};if(isNetworkAvailable()){server.start();}
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public ArrayList<Double> readData(InputStream in){
        BufferedReader reader = null;
        int prev = 0;
        ArrayList<Double> data = new ArrayList<Double>();
        try{
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine())!=null){
                for (int i = 0; i < line.length(); i++){
                    if (line.charAt(i) == '-'  || line.charAt(i) == '|'){
                        data.add(Double.parseDouble(line.substring(prev,i)));prev = i;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return data;
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
