package com.example.bustrackingtest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DriverLogin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        TextView err = findViewById(R.id.err);
        CardView login = findViewById(R.id.login_button);
        EditText phoneET = findViewById(R.id.phone);
        EditText passkeyET = findViewById(R.id.passkey);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            finish();
        });

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences driver_pref = getSharedPreferences("driver_pref",MODE_PRIVATE);
        SharedPreferences.Editor storage = driver_pref.edit();

        login.setOnClickListener(v->{
            new Thread(()->{
                try {
                    JSONObject data = new JSONObject();
                    data.put("action","driver_login");
                    data.put("phone",phoneET.getText().toString());
                    data.put("passkey",passkeyET.getText().toString());
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
                    boolean status = res.getBoolean("login_success");
                    if(status){
                        runOnUiThread(()->{
                            storage.putBoolean("isLoggedIn",true);
                            storage.putString("phone_no",phoneET.getText().toString());
                            storage.apply();

                            Intent i = new Intent(DriverLogin.this,driver_dashboard.class);
                            i.putExtra("phone_no",phoneET.getText().toString());
                            startActivity(i);
                        });
                    }else{
                        runOnUiThread(() -> {
                            try {
                                String err_msg = res.getString("reason");
                                err.setText(err_msg);
                                err.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });

                    }
                }catch(Exception e){
                    runOnUiThread(()->{
                        err.setText("something went wrong");
                        err.setVisibility(View.VISIBLE);
                    });
                }
            }).start();
        });
    }
}