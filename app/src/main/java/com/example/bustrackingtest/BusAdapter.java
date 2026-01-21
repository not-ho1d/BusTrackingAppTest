package com.example.bustrackingtest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.Holder>{
    FindBus.Bus[] data;
    public BusAdapter(FindBus.Bus[] buses){
        this.data = buses;
    }
    class Holder extends RecyclerView.ViewHolder{
        TextView bus_time,to,from,name;
        public Holder(View itemView){
            super(itemView);
            bus_time = itemView.findViewById(R.id.bus_time);
            to = itemView.findViewById(R.id.bus_to);
            from = itemView.findViewById(R.id.bus_from);
            name = itemView.findViewById(R.id.bus_name);
        }
    }
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bus_view, parent, false);
        return new Holder(view);
    }
    public void onBindViewHolder(Holder holder,int position){
        holder.bus_time.setText(data[position].time);
        holder.to.setText(data[position].to);
        holder.from.setText(data[position].from);
        holder.name.setText(data[position].name);
    }
    @Override
    public int getItemCount() {
        return data.length;
    }


}
