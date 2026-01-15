package com.example.bustrackingtest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView fromStand = findViewById(R.id.fromStand);
        AutoCompleteTextView toStand = findViewById(R.id.toStand);
        CardView view_map_button = findViewById(R.id.view_map_button);

        view_map_button.setOnClickListener(v->{
            Intent i = new Intent(MainActivity.this,ViewInMap.class);
            startActivity(i);
        });

        String[] stands = {"dwaraka","mananthavady","4th-mile","thonichal","nadakkal","tharuvana"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.dropdown,stands);
        fromStand.setAdapter(adapter);
        toStand.setAdapter(adapter);
    }
}