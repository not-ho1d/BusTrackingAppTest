package com.example.bustrackingtest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

public class DriverEditDashboard extends AppCompatActivity {
    String fromStop;
    String toStop;
    String routeName;
    private boolean isTracking = false;
    private void sendTrackingStateToServer(String busName, boolean isTracking) {
        new Thread(() -> {
            try {
                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                String server_url = pref.getString("server_url", "http://192.168.1.5:8000/Api/");

                JSONObject data = new JSONObject();
                data.put("action", "driver_tracking_state");
                data.put("bus_name", busName);
                data.put("is_tracking", isTracking);

                HttpURLConnection conn = (HttpURLConnection) new URL(server_url).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes("UTF-8"));
                os.close();

                conn.getResponseCode(); // optional: read response if needed

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_edit_dashboard);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            finish();
        });


        EditText to1, to2, to3, to4, to5, to6;
        EditText rt1, rt2, rt3, rt4, rt5, rt6;
        to1 = findViewById(R.id.to1);
        to2 = findViewById(R.id.to2);
        to3 = findViewById(R.id.to3);
        to4 = findViewById(R.id.to4);
        to5 = findViewById(R.id.to5);
        to6 = findViewById(R.id.to6);

        rt1 = findViewById(R.id.rt1);
        rt2 = findViewById(R.id.rt2);
        rt3 = findViewById(R.id.rt3);
        rt4 = findViewById(R.id.rt4);
        rt5 = findViewById(R.id.rt5);
        rt6 = findViewById(R.id.rt6);



        Intent i = getIntent();

        String busName = i.getStringExtra("bus_name");


        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        new Thread(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("action", "search_bus");
                data.put("bus_name", busName);   // your input value

                String server_url = pref.getString(
                        "server_url",
                        "http://192.168.1.5:8000/Api/"
                );

                URL url = new URL(server_url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");

                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);

                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();

                InputStream is;
                if (responseCode >= 200 && responseCode < 300) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String responseText = response.toString();
                JSONObject res = new JSONObject(responseText);
                Log.d("RESS", res.toString());

                boolean status = res.getBoolean("search_success");

                runOnUiThread(() -> {

                    if (status) {
                        try {

                            fromStop = res.getString("from");
                            toStop = res.getString("to");
                            routeName = res.getString("route_name");

                            JSONArray takeoffs = res.getJSONArray("takeoffs");
                            JSONArray returns = res.getJSONArray("returns");

                            // INPUT ARRAYS
                            EditText[] takeOffInputs = {to1, to2, to3, to4, to5, to6};
                            EditText[] returnInputs = {rt1, rt2, rt3, rt4, rt5, rt6};

                            for (int j = 0; j < takeOffInputs.length; j++) {

                                if (j < takeoffs.length()) {
                                    takeOffInputs[j].setText(takeoffs.getString(j));
                                } else {
                                    takeOffInputs[j].setText("");
                                }

                                if (j < returns.length()) {
                                    returnInputs[j].setText(returns.getString(j));
                                } else {
                                    returnInputs[j].setText("");
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {

                        EditText[] takeOffInputs = {to1, to2, to3, to4, to5, to6};
                        EditText[] returnInputs = {rt1, rt2, rt3, rt4, rt5, rt6};

                        for (int j = 0; j < 6; j++) {
                            takeOffInputs[j].setText("");
                            returnInputs[j].setText("");
                        }
                    }

                });

            } catch (Exception e) {
                e.printStackTrace();

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Error fetching bus", Toast.LENGTH_SHORT).show();
                });
            }

        }).start();

        Button saveBtn = findViewById(R.id.save_btn);

        saveBtn.setOnClickListener(v -> {

            EditText[] takeOffInputs = {to1, to2, to3, to4, to5, to6};
            EditText[] returnInputs = {rt1, rt2, rt3, rt4, rt5, rt6};

            new Thread(() -> {
                try {
                    JSONObject busData = new JSONObject();

                    busData.put("bus_name", busName);
                    busData.put("route_name", routeName);
                    busData.put("from", fromStop);
                    busData.put("to", toStop);

                    busData.put("to1", to1.getText().toString());
                    busData.put("to2", to2.getText().toString());
                    busData.put("to3", to3.getText().toString());
                    busData.put("to4", to4.getText().toString());
                    busData.put("to5", to5.getText().toString());
                    busData.put("to6", to6.getText().toString());

                    busData.put("rt1", rt1.getText().toString());
                    busData.put("rt2", rt2.getText().toString());
                    busData.put("rt3", rt3.getText().toString());
                    busData.put("rt4", rt4.getText().toString());
                    busData.put("rt5", rt5.getText().toString());
                    busData.put("rt6", rt6.getText().toString());
                    JSONObject data = new JSONObject();
                    data.put("action", "update_bus_timings");
                    data.put("bus_data", busData.toString());



                    String server_url = pref.getString(
                            "server_url",
                            "http://192.168.1.5:8000/searchbus/"
                    );

                    URL url = new URL(server_url);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    OutputStream os = conn.getOutputStream();
                    os.write(data.toString().getBytes("UTF-8"));
                    os.close();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream())
                    );

                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject res = new JSONObject(response.toString());
                    Log.d("SAVE_RESPONSE", res.toString());

                    boolean success = res.optBoolean("update_success", false);

                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                success ? "Saved successfully " : "Save failed ",
                                Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
                    });
                }

            }).start();
        });
        Button clearBtn = findViewById(R.id.clear_btn);

        clearBtn.setOnClickListener(v -> {

            EditText[] takeOffInputs = {to1, to2, to3, to4, to5, to6};
            EditText[] returnInputs = {rt1, rt2, rt3, rt4, rt5, rt6};

            for (int j = 0; j < 6; j++) {
                takeOffInputs[j].setText("");
                returnInputs[j].setText("");
            }
        });

        Button ShareLocation = findViewById(R.id.live_loc);

        isTracking = pref.getBoolean("is_tracking", false);

        ShareLocation.setText(isTracking ? "Stop Sharing" : "Share Location");

        ShareLocation.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra("bus_name", busName);

            if (!isTracking) {
                // Start service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }

                isTracking = true;
                ShareLocation.setText("Stop Sharing");
                pref.edit().putBoolean("is_tracking", true).apply();

                Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show();

                sendTrackingStateToServer(busName, true);

            } else {
                stopService(serviceIntent);

                isTracking = false;
                ShareLocation.setText("Share Location");
                pref.edit().putBoolean("is_tracking", false).apply();

                Toast.makeText(this, "Tracking stopped", Toast.LENGTH_SHORT).show();

                sendTrackingStateToServer(busName, false);
            }
        });

    }
}