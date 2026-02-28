package com.example.bustrackingtest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor storage = pref.edit();

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            finish();
        });

        EditText serverUrlField = findViewById(R.id.server_url);
        serverUrlField.setText(pref.getString("server_url","http://127.0.0.0:8000"));


        ImageView saveButton = findViewById(R.id.save);
        saveButton.setOnClickListener(v->{
            String server_url = serverUrlField.getText().toString();
            storage.putString("server_url",server_url);
            storage.apply();
            Log.d("URL", server_url);
        });
    }
}