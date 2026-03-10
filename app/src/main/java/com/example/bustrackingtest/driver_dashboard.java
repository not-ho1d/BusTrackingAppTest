package com.example.bustrackingtest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.widget.PopupMenu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class driver_dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences driver_pref = getSharedPreferences("driver_pref",MODE_PRIVATE);
        SharedPreferences.Editor storage = driver_pref.edit();
        ImageView menuBtn = findViewById(R.id.menu_button);

        menuBtn.setOnClickListener(v -> {

            Context wrapper = new ContextThemeWrapper(this, R.style.PopupMenuStyle);

            PopupMenu popup = new PopupMenu(wrapper, menuBtn);
            popup.getMenuInflater().inflate(R.menu.menu_dashboard, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                if(item.getItemId() == R.id.logout){

                    storage.putBoolean("isLoggedIn", false);
                    storage.putString("phone_no", "");
                    storage.apply();

                    Intent i = new Intent(driver_dashboard.this, MainActivity.class);
                    startActivity(i);
                    finish();

                    return true;
                }
                return false;
            });

            popup.show();
        });


        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            finish();
        });
        ListView list = findViewById(R.id.bus_list);

        final ArrayList<Driver> drivers = new ArrayList<>();
        final DriverAdapter adapter = new DriverAdapter(this, drivers);

        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {

            Driver clickedDriver = drivers.get(position);

            Intent i = new Intent(driver_dashboard.this, DriverEditDashboard.class);
            i.putExtra("bus_name", clickedDriver.getBusName());
            i.putExtra("route_name", clickedDriver.getRouteName());

            startActivity(i);
        });

        String phone_no = driver_pref.getString("phone_no","");

        new Thread(() -> {
            try {

                JSONObject data = new JSONObject();
                data.put("action","get_driver_data");
                data.put("phone",phone_no);

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

                reader.close();

                JSONObject res = new JSONObject(response.toString());
                boolean status = res.getBoolean("driver_data_success");

                if(status){

                    JSONArray driverData = res.getJSONArray("driver_data");

                    runOnUiThread(() -> {

                        drivers.clear();

                        for(int i = 0; i < driverData.length(); i++){
                            try {

                                JSONObject obj = driverData.getJSONObject(i);

                                String busName = obj.getString("bus_name");
                                String routeName = obj.getString("route_name");

                                drivers.add(new Driver(busName, routeName, "06:00"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();
                    });
                }

            } catch(Exception e){
                e.printStackTrace();
            }

        }).start();


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent i = new Intent(driver_dashboard.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

}