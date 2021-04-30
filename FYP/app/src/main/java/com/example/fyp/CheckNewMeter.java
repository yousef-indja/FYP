package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CheckNewMeter extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    public DatabaseReference dr;
    private String location = "", hours="", price="", restrict="", areaRef="", lat="", lng="";
    private EditText metNum, loc, space, hrs, cost, rest;
    private String meterNumber ="";
    private String numOfSpace ="";
    private String restrictions = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_meter_check);
        mAuth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        location = i.getStringExtra(TextRecognitionCamera.LOCATION);
        hours = i.getStringExtra(TextRecognitionCamera.HOURS);
        price = i.getStringExtra(TextRecognitionCamera.PRICE);
        restrict = i.getStringExtra(TextRecognitionCamera.RESTRICTIONS);
        lat = i.getStringExtra(TextRecognitionCamera.LATITUDE);
        lng = i.getStringExtra(TextRecognitionCamera.LONGITUDE);


        metNum = (EditText)findViewById(R.id.meterNumInput);
        loc = (EditText)findViewById(R.id.locationInput);
        space = (EditText)findViewById(R.id.numSpaceInput);
        hrs = (EditText)findViewById(R.id.hoursInput);
        cost = (EditText)findViewById(R.id.priceInput);
        rest = (EditText)findViewById(R.id.restrictionsInput);

        loc.setText(location);
        hrs.setText(hours);
        cost.setText(price);

        if(!restrict.equals("")){
            rest.setText(restrict);
            rest.setInputType(0);
            rest.setClickable(false);
            rest.setEnabled(false);
        }else{
            rest.setClickable(true);
            rest.setEnabled(true);
            rest.setHint("(Eg. Max stay. Leave blank if none.)");
        }


    }

    public void retake(View v){
        Intent intent = new Intent(CheckNewMeter.this, TextRecognitionCamera.class);
        startActivity(intent);
    }

    public void confirm(View v){
        metNum = (EditText)findViewById(R.id.meterNumInput);
        space = (EditText)findViewById(R.id.numSpaceInput);
        rest = (EditText)findViewById(R.id.restrictionsInput);
        ImageView errMet = (ImageView)findViewById(R.id.errorMet);
        ImageView errRest = (ImageView)findViewById(R.id.errorRest);

        meterNumber = metNum.getText().toString();
        numOfSpace = space.getText().toString();
        restrictions = rest.getText().toString();

        if(meterNumber.equals("")){
            Toast.makeText(CheckNewMeter.this, "Meter Number cannot be empty!", Toast.LENGTH_SHORT).show();
            errMet.setVisibility(View.VISIBLE);
            errRest.setVisibility(View.INVISIBLE);
        }else if(meterNumber.length()>4){
            Toast.makeText(CheckNewMeter.this, "Meter Number cannot be more than 4 digits!", Toast.LENGTH_SHORT).show();
            errMet.setVisibility(View.VISIBLE);
            errRest.setVisibility(View.INVISIBLE);
        }else if(!restrictions.equals("")){
            if(!restrictions.contains("Max") && !restrictions.contains("max") &&
                    !restrictions.contains("Min")&&!restrictions.contains("Min")){
                Toast.makeText(CheckNewMeter.this, "The restrictions you entered don't meet the correct criteria!", Toast.LENGTH_LONG).show();
                errRest.setVisibility(View.VISIBLE);
                errMet.setVisibility(View.INVISIBLE);
            }else{
                errRest.setVisibility(View.INVISIBLE);
                errMet.setVisibility(View.INVISIBLE);
                dr = FirebaseDatabase.getInstance().getReference("Parking Meters");

                dr.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean found = false;
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            ParkingMeters pm = snap.getValue(ParkingMeters.class);
                            String mn = pm.getMeterNumber().replace("(DLR)", "").replace("(DCC)", "");
                            if(mn.equals(meterNumber) ){
                                found = true;
                            }else if(pm.getLongitude().equals(String.valueOf(lng)) && pm.getLatitude().equals(String.valueOf(lat))){
                                found = true;
                            }
                        }

                        if(found == false){
                            ParkingMeters pm = new ParkingMeters(meterNumber, location, "", numOfSpace, hours, price, restrictions, lat, lng );
                            dr.child(meterNumber).setValue(pm);
                            Toast.makeText(CheckNewMeter.this, "Success!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(CheckNewMeter.this, Maps.class);
                            startActivity(i);
                        }else{
                            Toast.makeText(CheckNewMeter.this, "Error: This meter already exists!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(CheckNewMeter.this, Maps.class);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

        }else{
            errRest.setVisibility(View.INVISIBLE);
            errMet.setVisibility(View.INVISIBLE);
            dr = FirebaseDatabase.getInstance().getReference("Parking Meters");
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean found = false;
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if(snap.getKey().equals(meterNumber)){
                            found = true;
                        }
                    }

                    if(found == false){
                        ParkingMeters pm = new ParkingMeters(meterNumber, location, "", numOfSpace, hours, price, restrictions, lat, lng );
                        dr.child(meterNumber).setValue(pm);
                        Toast.makeText(CheckNewMeter.this, "Success!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(CheckNewMeter.this, Maps.class);
                        startActivity(i);
                    }else{
                        Toast.makeText(CheckNewMeter.this, "Error: This meter already exists!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(CheckNewMeter.this, Maps.class);
                        startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


}
