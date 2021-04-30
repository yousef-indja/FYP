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

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements View.OnClickListener{
    private List<ParkingMeters> list_pm;
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


    public MyAdapter( List<ParkingMeters> list_pm){
        this.list_pm = list_pm;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.row_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyAdapter.MyViewHolder holder, int position) {

        final ParkingMeters pm = list_pm.get(position);
        String top = "<b><u><h3>" + pm.getLocation() + "</h3></u></b>";
        String main = pm.toStringForRV().replace("\n", "<br>");
        String total = top + main;
        holder.textView.setText(Html.fromHtml(total));
        holder.textView.setText(Html.fromHtml(total));
        holder.textView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Intent i = new Intent(v.getContext(), Maps.class);
                i.putExtra("chosenLocation", pm.getMeterNumber());
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(list_pm ==  null){
            return 0;
        } else {
            return list_pm.size();
        }

    }

    public void remove(int position){
        list_pm.remove(position);
        notifyItemRemoved(position);
    }

}
