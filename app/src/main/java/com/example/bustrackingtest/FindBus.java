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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
        String searchResult = searchInfo.getString("search_result");
        ArrayList<Bus> buses = new ArrayList<>();
        if(searchResult == null || searchResult.equals("fail")){

        }else{
            try {
                JSONArray bus_data = new JSONArray(searchResult);
                JSONArray busArray = new JSONArray(searchResult);

                for (int i = 0; i < busArray.length(); i++) {

                    JSONObject bus = busArray.getJSONObject(i);

                    String busName = bus.getString("bus_name");
                    String busTime = bus.getString("bus_time");
                    String busRoute = bus.getString("bus_route");
                    int splitPos = busRoute.indexOf("-");
                    Log.d("BUS_NAME,TIME ", busName+"----"+busTime);

                    Bus b = new Bus();
                    b.time = busTime;
                    b.from = busRoute.substring(0,splitPos);
                    b.to = busRoute.substring(splitPos+1);
                    b.name = busName;

                    buses.add(b);
                }
            }catch(Exception e){

            }

        }

        TextView fromTv = findViewById(R.id.from);
        TextView toTv = findViewById(R.id.to);

        fromTv.setText(fromStand);
        toTv.setText(toStand);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setClickable(true);
        backButton.setOnClickListener(v->{
            finish();
        });
        RecyclerView recycler = (RecyclerView)findViewById(R.id.recycler_view);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new BusAdapter(buses));

    }
}