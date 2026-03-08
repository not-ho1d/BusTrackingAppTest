package com.example.bustrackingtest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView fromStand = findViewById(R.id.fromStand);
        AutoCompleteTextView toStand = findViewById(R.id.toStand);
        CardView find_bus_button = findViewById(R.id.find_bus_button);
        CardView view_map_button = findViewById(R.id.view_map_button);
        CardView driver_access = findViewById(R.id.driver_login_button);
        ImageView settings = findViewById(R.id.settings);

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor storage = pref.edit();

        String[] stands = {"dwaraka","mananthavady","4th-mile","thonichal","nadakkal","tharuvana","changadakkadavu","nadakkal","tharuvana",
        "vellamunda","kanhirangad","korome","niravilpuzha"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown,stands);
        fromStand.setAdapter(adapter);
        toStand.setAdapter(adapter);

        find_bus_button.setOnClickListener(v->{
            //gets data from editText
            String from = fromStand.getText().toString().toLowerCase();
            String to = toStand.getText().toString().toLowerCase();

            //if both input fields are not empty move to FindBus activity
            if(!from.isEmpty() && !to.isEmpty()){
                Bundle searchInfo = new Bundle();
                searchInfo.putString("from",from);
                searchInfo.putString("to",to);

                //http req
                new Thread(()->{
                    try {
                        JSONObject data = new JSONObject();
                        data.put("action","find_bus_search");
                        data.put("from",from);
                        data.put("to",to);
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
                            is = conn.getInputStream();     // success response
                        } else {
                            is = conn.getErrorStream();     // error response
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
                        boolean status = res.getBoolean("search_success");
                        if(status){
                            Log.d("search_success",res.getJSONArray("data").toString());
                            JSONArray bus_data_array = res.getJSONArray("data");
//                            for(int i=0;i < bus_data_array.length() ; i++){
//                                JSONObject bus_data = bus_data_array.getJSONObject(i);
//                                Log.d("BUS_DATA: ", bus_data.getJSONObject("bus_timetable").toString());
//                            }
                            searchInfo.putString("search_result",bus_data_array.toString());
                            searchInfo.putString("search_status","success");
                            runOnUiThread(()->{
                                Intent i = new Intent(MainActivity.this,FindBus.class);
                                i.putExtra("searchInfo",searchInfo);
                                startActivity(i);
                            });
                        }else{
                            runOnUiThread(()->{
                                Intent i = new Intent(MainActivity.this,FindBus.class);
                                searchInfo.putString("search_status","fail");
                                startActivity(i);
                            });
                        }

                    }catch(Exception e){
                        runOnUiThread(()->{
                            Intent i = new Intent(MainActivity.this,FindBus.class);
                            searchInfo.putString("search_status","error");
                            startActivity(i);
                        });
                    }
                }).start();
            }
        });

        view_map_button.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this,ViewInMap.class);
            startActivity(i);
        });

        settings.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this,Settings.class);
            startActivity(i);
        });

        driver_access.setOnClickListener(v->{
            Boolean isLoggedIn = pref.getBoolean("is_loggedin",false);
            if(isLoggedIn){
                Intent i = new Intent(MainActivity.this,driver_dashboard.class);
                startActivity(i);
            }else{
                Intent i = new Intent(MainActivity.this,DriverLogin.class);
                startActivity(i);
            }
        });
    }
}