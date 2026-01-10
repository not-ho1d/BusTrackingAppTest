package com.example.bustrackingtest;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView fromStand = findViewById(R.id.fromStand);
        AutoCompleteTextView toStand = findViewById(R.id.toStand);

        String[] stands = {"dwaraka","mananthavady","4th-mile","thonichal","nadakkal","tharuvana"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.dropdown,stands);
        fromStand.setAdapter(adapter);
        toStand.setAdapter(adapter);
    }
}