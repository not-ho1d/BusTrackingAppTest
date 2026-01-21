package com.example.bustrackingtest;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class FindBus extends AppCompatActivity {

    RecyclerView recyclerView;
    List<String> busList;

    static class Bus{
        String time,from,to,name;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_bus);

        Bundle searchInfo = getIntent().getBundleExtra("searchInfo");
        String fromStand = searchInfo.getString("from");
        String toStand = searchInfo.getString("to");

        TextView fromTv = findViewById(R.id.from);
        TextView toTv = findViewById(R.id.to);

        fromTv.setText(fromStand);
        toTv.setText(toStand);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            finish();
        });

        Bus b1 = new Bus();
        b1.time = "10:30";
        b1.from = "mananthavady";
        b1.to = "panamaram";
        b1.name = "KSRTC";

        Bus b2 = new Bus();
        b2.time = "2:50";
        b2.from = "mananthavady";
        b2.to = "niravilpuzha";
        b2.name = "KSRTC";

        Bus b3 = new Bus();
        b3.time = "7:45";
        b3.from = "mananthavady";
        b3.to = "padinjarthara";
        b3.name = "karthika";

        Bus b4 = new Bus();
        b4.time = "4:54";
        b4.from = "mananthavady";
        b4.to = "vadakara";
        b4.name = "KSRTC";

        Bus b5 = new Bus();
        b5.time = "10:30";
        b5.from = "mananthavady";
        b5.to = "panamaram";
        b5.name = "KSRTC";

        Bus b6 = new Bus();
        b6.time = "2:50";
        b6.from = "mananthavady";
        b6.to = "niravilpuzha";
        b6.name = "KSRTC";

        Bus b7 = new Bus();
        b7.time = "7:45";
        b7.from = "mananthavady";
        b7.to = "padinjarthara";
        b7.name = "karthika";

        Bus b8 = new Bus();
        b8.time = "4:54";
        b8.from = "mananthavady";
        b8.to = "vadakara";
        b8.name = "KSRTC";

        Bus b9 = new Bus();
        b9.time = "10:30";
        b9.from = "mananthavady";
        b9.to = "panamaram";
        b9.name = "KSRTC";

        Bus bb6 = new Bus();
        bb6.time = "2:50";
        bb6.from = "mananthavady";
        bb6.to = "niravilpuzha";
        bb6.name = "KSRTC";

        Bus bb7 = new Bus();
        bb7.time = "7:45";
        bb7.from = "mananthavady";
        bb7.to = "padinjarthara";
        bb7.name = "karthika";

        Bus bb8 = new Bus();
        bb8.time = "4:54";
        bb8.from = "mananthavady";
        bb8.to = "vadakara";
        bb8.name = "KSRTC";

        Bus[] buses = {b1,b2,b3,b4,b5,b6,b7,b8,b9,bb6,bb7,bb8};
        RecyclerView recycler = (RecyclerView)findViewById(R.id.recycler_view);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new BusAdapter(buses));

    }
}