package com.example.fyp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JourneyPlannerResults extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{


    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    public DatabaseReference dr;

    private RecyclerView rv;
    PlannerAdapter adapter;
    private ArrayList<ParkingMeters> parkingMeters;

    private String[] filterCategory = {"Select Option","Distance", "Price"};
    private String[] orderCategory = {"Ascending", "Descending"};

    private Button filter;
    private Spinner filterSpinner, orderSpinner;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jp_results);

        Intent i = getIntent();
        parkingMeters = (ArrayList)i.getSerializableExtra("suitablePM");

        setUpRV();

        filter = (Button)findViewById(R.id.filterButton);
        filterSpinner = (Spinner) findViewById(R.id.filterSpinner);
        List<String> filterList = new ArrayList<>(Arrays.asList(filterCategory));
        final ArrayAdapter<String> filterArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.spinner_item,filterList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };
        filterArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        filterSpinner.setAdapter(filterArrayAdapter);

        orderSpinner = (Spinner) findViewById(R.id.orderSpinner);
        orderSpinner.setOnItemSelectedListener(this);
        ArrayAdapter aa = new ArrayAdapter(this, R.layout.spinner_item, orderCategory);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(aa);

    }

    public void setUpRV() {
        rv = (RecyclerView) findViewById(R.id.myRecyclerView);
        rv.isClickable();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);

        adapter = new PlannerAdapter(parkingMeters);

        rv.addItemDecoration((new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)));

        rv.setAdapter(adapter);

        if(parkingMeters.isEmpty()){

        }else{

        }
    }

    private void redoAdapter(ArrayList<ParkingMeters> p){
        PlannerAdapter adapter = new PlannerAdapter(p);
        rv.addItemDecoration((new DividerItemDecoration(this, LinearLayoutManager.VERTICAL)));
        rv.setAdapter(adapter);


    }


    public void filterResults(View v){
        String categoryChosen = filterSpinner.getSelectedItem().toString();
        String order = orderSpinner.getSelectedItem().toString();

        if(categoryChosen.equals("Choose Option")){
            Toast.makeText(JourneyPlannerResults.this, "Error: Please select option from dropdown list!", Toast.LENGTH_SHORT).show();
        }else{
            if(categoryChosen.equals("Distance")){
                if(order.equals("Ascending")){
                    Collections.sort(parkingMeters, new SortByDistance());
                    redoAdapter(parkingMeters);
                }else{
                    Collections.sort(parkingMeters, Collections.<ParkingMeters>reverseOrder(new SortByDistance()));
                    redoAdapter(parkingMeters);
                }
            }

            if(categoryChosen.equals("Price")){
                if(order.equals("Ascending")){
                    Collections.sort(parkingMeters, new SortByPrice());
                    redoAdapter(parkingMeters);
                }else{
                    Collections.sort(parkingMeters, Collections.<ParkingMeters>reverseOrder(new SortByPrice()));
                    redoAdapter(parkingMeters);
                }
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
