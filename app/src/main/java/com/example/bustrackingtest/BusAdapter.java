package com.example.bustrackingtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.Holder>{
    ArrayList<FindBus.Bus> data;

    private String toAmPm(String time) {
        try {
            String[] hm = time.split(":");

            int hour = Integer.parseInt(hm[0]);
            int min = Integer.parseInt(hm[1]);

            String ampm = "AM";

            if (hour >= 12) {
                ampm = "PM";
            }

            if (hour == 0) {
                hour = 12; // 00 -> 12 AM
            } else if (hour > 12) {
                hour -= 12;
            }

            return String.format("%02d:%02d %s", hour, min, ampm);

        } catch (Exception e) {
            return time; // fallback if something breaks
        }
    }
    private String getArrivalText(String busTime) {
        try {
            // current time
            java.util.Calendar now = java.util.Calendar.getInstance();
            int currMin = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                    now.get(java.util.Calendar.MINUTE);

            // bus time (HH:mm)
            String[] hm = busTime.split(":");
            int busMin = Integer.parseInt(hm[0]) * 60 +
                    Integer.parseInt(hm[1]);

            int diff = busMin - currMin;

            // handle next-day buses
            if (diff < 0) {
                diff += 24 * 60;
            }

            // formatting
            if (diff == 0) return "Arriving now";
            if (diff < 60) return diff + " min";
            return (diff / 60) + " hr " + (diff % 60) + " min";

        } catch (Exception e) {
            return "--";
        }
    }
    public BusAdapter(ArrayList<FindBus.Bus> buses){
        this.data = buses;
    }
    class Holder extends RecyclerView.ViewHolder{
        TextView bus_time,to,from,name,arriving_in;
        public Holder(View itemView){
            super(itemView);
            bus_time = itemView.findViewById(R.id.bus_time);
            to = itemView.findViewById(R.id.bus_to);
            from = itemView.findViewById(R.id.bus_from);
            name = itemView.findViewById(R.id.bus_name);
            arriving_in = itemView.findViewById(R.id.arrival_time);
        }
    }
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bus_view, parent, false);
        return new Holder(view);
    }
    @Override
    public void onBindViewHolder(Holder holder, int position) {
        FindBus.Bus bus = data.get(position);

        holder.bus_time.setText(toAmPm(bus.time));
        holder.to.setText(bus.to);
        holder.from.setText(bus.from);
        holder.name.setText(bus.name);
        holder.arriving_in.setText(getArrivalText(bus.time));

        holder.itemView.setOnClickListener(v -> {
            SharedPreferences pref = v.getContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
            new Thread(() -> {
                try {
                    JSONObject data = new JSONObject();
                    data.put("action", "get_route_coords");
                    data.put("bus_name", bus.name);

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
                        is = conn.getInputStream();
                    } else {
                        is = conn.getErrorStream();
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }

                    br.close();

                    String result = response.toString();

                    // parse JSON if needed
                    JSONObject json = new JSONObject(result);

                    ((Activity)v.getContext()).runOnUiThread(() -> {
                        Intent i = new Intent(v.getContext(), ViewInMap.class);
                        i.putExtra("bus_name",bus.name);
                        i.putExtra("time",bus.time);
                        i.putExtra("route_name",bus.from +"→"+bus.to);
                        i.putExtra("route_poly",result);
                        v.getContext().startActivity(i);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
    @Override
    public int getItemCount() {
        return data.size();
    }


}
