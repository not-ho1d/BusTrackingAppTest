package com.example.bustrackingtest;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class driver_dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        ListView list = findViewById(R.id.bus_list);

        ArrayList<Driver> drivers = new ArrayList<>();

        drivers.add(new Driver("Test #01", "Thonichal → Kellur", "06:00"));
        drivers.add(new Driver("Test #02", "Mananthavady → Dwaraka", "06:10"));

        DriverAdapter adapter = new DriverAdapter(this, drivers);

        list.setAdapter(adapter);

    }
}