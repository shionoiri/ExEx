package com.example.exex;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class HRReadingActivity extends WearableActivity implements SensorEventListener {
    private TextView mTextView;
    private TextView rTextView;
    private TextView lTextView;
    SensorManager mSensorManager;
    Sensor mHRSensor;
    float hr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.heartRateText);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHRSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rTextView = (TextView) findViewById(R.id.rssi);
        lTextView = (TextView) findViewById(R.id.speed);
        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();

        int rssi = info.getRssi();
        rTextView.setText("RSSI=" + String.valueOf(rssi));
        Log.d("RSSI", String.valueOf(rssi));

        int linkspeed = info.getLinkSpeed();
        lTextView.setText("Speed=" + String.valueOf(linkspeed));
        Log.d("Speed", String.valueOf(linkspeed));

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_HEART_RATE);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {

            float hr = event.values[0];

            int mHeartRate = Math.round(hr);

            String str = String.valueOf(mHeartRate);

            mTextView.setText("HR=" + str);

            connect(str);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void connect(final String str) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                try {

                    Socket socket = new Socket("192.168.49.1", 8080);
                    OutputStream os = socket.getOutputStream();

                    BufferedWriter bufwritter = new BufferedWriter(new OutputStreamWriter(os));
                    bufwritter.write(str);
                    //bufwritterをcloseしないと値がサーバー側で反映されなかった．

                    bufwritter.close();
                    socket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
        Log.v("socketinfo", "Data was sent");
    }
}