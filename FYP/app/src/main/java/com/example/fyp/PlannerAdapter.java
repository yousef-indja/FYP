package com.example.fyp;

import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlannerAdapter extends RecyclerView.Adapter<PlannerAdapter.MyViewHolder> implements View.OnClickListener{
    private List<ParkingMeters> listPm;


    public static final String KEY1 = "ID";
    private ParkingMeters parkingMeters;

    @Override
    public void onClick(View v) {


    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView textView;

        public MyViewHolder(View itemView){
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.recyclerTextView);
        }
    }


    public PlannerAdapter( List<ParkingMeters> listPm){
        this.listPm = listPm;
    }

    @NonNull
    @Override
    public PlannerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.row_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PlannerAdapter.MyViewHolder holder, int position) {
        parkingMeters = listPm.get(position);
        String top = "<b><u><h3>" + parkingMeters.getLocation() + "</h3></u></b>";
        String mid = parkingMeters.toStringForRV2();
        String main = mid.replace("\n", "<br>");
        String total = top + main;
        holder.textView.setText(Html.fromHtml(total));
        holder.textView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                ParkingMeters pm = listPm.get(position);
                Intent i = new Intent(v.getContext(), Maps.class);
                i.putExtra("chosenLocation", pm.getMeterNumber());
                v.getContext().startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(listPm ==  null){
            return 0;
        } else {
            return listPm.size();
        }

    }

    public void remove(int position){
        listPm.remove(position);
        notifyItemRemoved(position);
    }







}
