package com.mobileproto.lab3;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendPulse = (Button) findViewById(R.id.pulse_button);

        // explicitly enable GPS
        Intent enableGPS = new Intent(
                "android.location.GPS_ENABLED_CHANGE");
        enableGPS.putExtra("enabled", true);
        sendBroadcast(enableGPS);
        
        final GPS gps = new GPS(this);

        final TextView velocity = (TextView) findViewById(R.id.velocity_display);
        final TextView location = (TextView) findViewById(R.id.gps_display);

        Thread vel = new Thread(){
        public void run(){
            try{
                while(!isInterrupted()){
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            location.setText("Lat:" + String.valueOf(gps.getLatitude()) + "\n Long:" + String.valueOf(gps.getLongitude()));
                            Log.e("Latitude",String.valueOf(gps.getLatitude()));
                            velocity.setText(Double.toString(gps.getSpeeed()) + " deg/s");
                        }
                    });
                    Thread.sleep(200);
                }
            } catch (InterruptedException e){}
        }
        };vel.start();

        sendPulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gps.canGetLocation()){
                    location.setText("Lat:" + String.valueOf(gps.getLatitude()) + "\n Long:" + String.valueOf(gps.getLongitude()));}
            };
        });

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
