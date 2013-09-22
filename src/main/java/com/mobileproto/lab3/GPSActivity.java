package com.mobileproto.lab3;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by evan on 9/15/13.
 */
public class GPSActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        final GPS gps = new GPS(this);

        final Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gps.canGetLocation()){
                    TextView lat = (TextView) findViewById(R.id.lat);
                    TextView lon = (TextView) findViewById(R.id.lon);
                    lat.setText("Lattitude: " + String.valueOf(gps.getLatitude()));
                    lon.setText("Longitude: " + String.valueOf(gps.getLongitude()));
                    Log.v("Lattitude", String.valueOf(gps.getLatitude()));
                    new RemoteGeocode(){
                        @Override
                        public void onPostExecute(String result){
                            TextView xml = (TextView) findViewById(R.id.textView);
                            xml.setText(result);
                        }
                    }.execute(RemoteGeocode.generateUri(gps.getLatitude(), gps.getLongitude()));
                }
            button.setText("Refresh");
            }
        });


    }

}
