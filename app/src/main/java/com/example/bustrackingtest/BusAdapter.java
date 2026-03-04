package com.example.bustrackingtest;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.Holder>{
    ArrayList<FindBus.Bus> data;
    public BusAdapter(ArrayList<FindBus.Bus> buses){
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
    @Override
    public void onBindViewHolder(Holder holder, int position) {
        FindBus.Bus bus = data.get(position);

        holder.bus_time.setText(bus.time);
        holder.to.setText(bus.to);
        holder.from.setText(bus.from);
        holder.name.setText(bus.name);

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ViewInMap.class);
            i.putExtra("bus_name",bus.name);
            v.getContext().startActivity(i);
        });
    }
    @Override
    public int getItemCount() {
        return data.size();
    }


}
