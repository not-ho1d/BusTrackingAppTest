package com.example.bustrackingtest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

        String[] stands = {"dwaraka","mananthavady","4th-mile","thonichal","nadakkal","tharuvana"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.dropdown,stands);
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

                        URL url = new URL("http://10.167.170.30:8000/Api/");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        OutputStream os = conn.getOutputStream();
                        os.write(data.toString().getBytes());
                        os.close();
                        conn.getResponseCode();

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
                            runOnUiThread(()->{
                                Intent i = new Intent(MainActivity.this,FindBus.class);
                                i.putExtra("searchInfo",searchInfo);
                                startActivity(i);
                            });
                        }else{
                            Log.d("search_success", "fail");
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        view_map_button.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this,ViewInMap.class);
            startActivity(i);
        });
    }
}