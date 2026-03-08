package com.example.bustrackingtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DriverAdapter extends BaseAdapter {

    Context context;
    ArrayList<Driver> drivers;

    public DriverAdapter(Context context, ArrayList<Driver> drivers) {
        this.context = context;
        this.drivers = drivers;
    }

    @Override
    public int getCount() {
        return drivers.size();
    }

    @Override
    public Object getItem(int position) {
        return drivers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.bus_row, parent, false);
        }

        TextView name = convertView.findViewById(R.id.bus_name);
        TextView route = convertView.findViewById(R.id.bus_route);
        TextView time = convertView.findViewById(R.id.bus_time);

        Driver driver = drivers.get(position);

        name.setText(driver.busName);
        route.setText(driver.route);
        time.setText(driver.time);

        return convertView;
    }
}
