package com.example.fyp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Planner extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TimePicker stp;
    private TimePicker etp;
    private Button sb;
    private Button eb;
    public Spinner spinner;
    String[] dayOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    FirebaseAuth auth;
    FirebaseUser fUser;
    DatabaseReference dr;

    LatLng latLng;
    int dst;

    int userStartTime, userEndTime;
    String parkStartTime, parkEndTime;


    ArrayList<ParkingMeters> compatibleParking = new ArrayList();
    ArrayList<Double> distanceFromLocation = new ArrayList();
    ArrayList<Double> priceList = new ArrayList();
    ArrayList<String> show = new ArrayList();

    DecimalFormat df = new DecimalFormat("###.##");


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.planner_layout);

        auth = FirebaseAuth.getInstance();
        fUser = auth.getCurrentUser();

        stp = (TimePicker) findViewById(R.id.startTimePicker);
        stp.setIs24HourView(true);
        stp.setVisibility(View.VISIBLE);
        etp = (TimePicker) findViewById(R.id.endTimePicker);
        etp.setIs24HourView(true);
        etp.setVisibility(View.INVISIBLE);

        sb = (Button) findViewById(R.id.startTimeButton);
        sb.setTextColor(getResources().getColor(R.color.quantum_white_100));
        eb = (Button) findViewById(R.id.endTimeButton);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter aa = new ArrayAdapter(this, R.layout.spinner_item, dayOfWeek);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);


    }

    public void showStartTime(View v) {
        stp.setVisibility(View.VISIBLE);
        sb.setTextColor(getResources().getColor(R.color.quantum_white_100));
        etp.setVisibility(View.INVISIBLE);
        eb.setTextColor(getResources().getColor(R.color.quantum_black_100));

    }

    public void showEndTime(View v) {
        etp.setVisibility(View.VISIBLE);
        eb.setTextColor(getResources().getColor(R.color.quantum_white_100));
        stp.setVisibility(View.INVISIBLE);
        sb.setTextColor(getResources().getColor(R.color.quantum_black_100));

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void confirm(View v) {
        Boolean loc = false;
        Boolean dis = false;
        Boolean tm = false;

        //Get location
        EditText locationInput = (EditText) findViewById(R.id.locationInput);
        String location = locationInput.getText().toString();
        List<Address> addressList = null;

        if (location != null && !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
                Address address = addressList.get(0);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                Location startPoint = new Location("dublin");
                startPoint.setLatitude(53.417629);
                startPoint.setLongitude(-6.251626);

                Location endPoint = new Location("searchLocation");
                endPoint.setLatitude(latLng.latitude);
                endPoint.setLongitude(latLng.longitude);
                double distance = startPoint.distanceTo(endPoint);
                if (distance < 30000) {
                    loc = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Error: Please Search Within Dublin.", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException i) {
                Toast.makeText(getApplicationContext(), "Error: Location Could Not Be Found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Error: Please Enter A Location.", Toast.LENGTH_SHORT).show();
        }

        //Get distance
        EditText dist = (EditText) findViewById(R.id.distanceInput);

        if(loc == true){
            try{
                dst = Integer.parseInt(dist.getText().toString());
                dis = true;
            }catch(NumberFormatException e){
                Toast.makeText(getApplicationContext(), "Error: Please Enter Your Preferred Distance To Search.", Toast.LENGTH_SHORT).show();
            }
        }



        //Get day
        final String dayChosen = spinner.getSelectedItem().toString();

        //Get time
        int startHr = stp.getHour();
        int startMins = stp.getMinute();
        String shr="", smin="";
        if(stp.getHour() == 1 ||stp.getHour() == 2 ||stp.getHour() == 3 ||stp.getHour() == 4||stp.getHour() == 5 ||stp.getHour() == 6 ||
                stp.getHour() == 7 ||stp.getHour() == 8 ||stp.getHour() == 9 ){
             shr = "0" + String.valueOf((stp.getHour()));
        }else if (stp.getHour() == 0 ){
             shr = "00";
        }else{
             shr = String.valueOf((stp.getHour()));
        }

        if(stp.getMinute() == 1 ||stp.getMinute() == 2 ||stp.getMinute() == 3 ||stp.getMinute() == 4||stp.getMinute() == 5 ||stp.getMinute() == 6 ||
                stp.getMinute() == 7 ||stp.getMinute() == 8 ||stp.getMinute() == 9 ){
             smin = "0" + String.valueOf((stp.getMinute()));
        }else if (stp.getMinute() == 0 ){
             smin = "00";
        }else{
             smin = String.valueOf((stp.getMinute()));
        }
        String sT = shr + smin;
        userStartTime = Integer.parseInt(sT);

        int endHr = etp.getHour();
        int endMins = etp.getMinute();
        String ehr="", emin="";
        if(etp.getHour() == 1 ||etp.getHour() == 2 ||etp.getHour() == 3 ||etp.getHour() == 4||etp.getHour() == 5 ||etp.getHour() == 6 ||
                etp.getHour() == 7 ||etp.getHour() == 8 ||etp.getHour() == 9 ){
            ehr = "0" + String.valueOf((etp.getHour()));
        }else if (etp.getHour() == 0 ){
            ehr = "00";
        }else{
            ehr = String.valueOf((etp.getHour()));
        }

        if(etp.getMinute() == 1 ||etp.getMinute() == 2 ||etp.getMinute() == 3 ||etp.getMinute() == 4||etp.getMinute() == 5 ||etp.getMinute() == 6 ||
                etp.getMinute() == 7 ||etp.getMinute() == 8 ||etp.getMinute() == 9 ){
            emin = "0" + String.valueOf((etp.getMinute()));
        }else if (etp.getMinute() == 0 ){
            emin = "00";
        }else{
            emin = String.valueOf((etp.getMinute()));
        }
        String eT = ehr + emin;
        userEndTime = Integer.parseInt(eT);

        if(loc == true && dis == true){
            if (startHr > endHr) {
                Toast.makeText(getApplicationContext(), "Error: Start Time Cannot Be Earlier Than End Time.", Toast.LENGTH_SHORT).show();
            } else if (startHr == endHr && startMins >= endMins) {
                Toast.makeText(getApplicationContext(), "Error: Start Time Cannot Be Earlier Than End Time.", Toast.LENGTH_SHORT).show();
            } else {
                tm = true;
            }
        }




        if (loc == true && dis == true && tm == true) {

            dr = FirebaseDatabase.getInstance().getReference("Parking Meters");
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        ParkingMeters pm = snap.getValue(ParkingMeters.class);

                        LatLng latLong = new LatLng(Double.parseDouble(pm.getLatitude()), Double.parseDouble(pm.getLongitude()));
                        Location startPoint = new Location("pmLocation");
                        startPoint.setLatitude(latLong.latitude);
                        startPoint.setLongitude(latLong.longitude);

                        Location endPoint = new Location("searchLocation");
                        endPoint.setLatitude(latLng.latitude);
                        endPoint.setLongitude(latLng.longitude);
                        double distance = startPoint.distanceTo(endPoint);

                        if (!pm.getTimesOfOperation().equals("")) {
                            String parkTimes = pm.getTimesOfOperation().replaceAll("\\D+", "");

                            if (parkTimes.length() == 7) {
                                parkStartTime = parkTimes.substring(0, 3);
                                parkEndTime = parkTimes.substring(3);
                            } else if (parkTimes.length() == 8) {
                                parkStartTime = parkTimes.substring(0, 4);
                                parkEndTime = parkTimes.substring(4);
                            }
                        }



                        double price = 0;

                        if (distance <= dst) {
                            if (dayChosen.equals("Saturday")) {
                                if (pm.getTimesOfOperation().contains("Sat") || pm.getTimesOfOperation().contains("sat") || pm.getTimesOfOperation().contains("Saturday")
                                        || pm.getTimesOfOperation().contains("saturday")) {
                                    double finalPayTime = calculate();
                                    price = finalPayTime * Double.parseDouble(pm.getHourlyTariff());

                                    String p =df.format(price);
                                    price = Double.parseDouble(p);
                                    pm.setPrice(price);
                                    String d =df.format(distance);
                                    distance = Double.parseDouble(d);
                                    pm.setDistance(distance);
                                    compatibleParking.add(pm);
                                } else {
                                    price = 0;
                                    String p =df.format(price);
                                    price = Double.parseDouble(p);
                                    pm.setPrice(price);
                                    String d =df.format(distance);
                                    distance = Double.parseDouble(d);
                                    pm.setDistance(distance);
                                    compatibleParking.add(pm);
                                }
                            } else if (dayChosen.equals("Sunday")) {
                                if (pm.getTimesOfOperation().contains("Sun") || pm.getTimesOfOperation().contains("sun") || pm.getTimesOfOperation().contains("Sunday")
                                        || pm.getTimesOfOperation().contains("sunday")) {
                                    double finalPayTime = calculate();
                                    price = finalPayTime * Double.parseDouble(pm.getHourlyTariff());
                                    String p =df.format(price);
                                    price = Double.parseDouble(p);
                                    pm.setPrice(price);
                                    String d =df.format(distance);
                                    distance = Double.parseDouble(d);
                                    pm.setDistance(distance);
                                    compatibleParking.add(pm);
                                } else {
                                    price = 0;
                                    String p =df.format(price);
                                    price = Double.parseDouble(p);
                                    pm.setPrice(price);
                                    String d =df.format(distance);
                                    distance = Double.parseDouble(d);
                                    pm.setDistance(distance);
                                    compatibleParking.add(pm);

                                }
                            } else {
                                double finalPayTime = calculate();
                                price = finalPayTime * Double.parseDouble(pm.getHourlyTariff());
                                String p =df.format(price);
                                price = Double.parseDouble(p);
                                pm.setPrice(price);
                                String d =df.format(distance);
                                distance = Double.parseDouble(d);
                                pm.setDistance(distance);
                                compatibleParking.add(pm);

                            }
                        }
                    }

                    Intent i = new Intent(Planner.this, JourneyPlannerResults.class);
                    i.putExtra("suitablePM", compatibleParking);
                    startActivity(i);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }

    public double calculate() {
        //starts before start ends after end
        if (userStartTime <= Integer.parseInt(parkStartTime) && userEndTime >= Integer.parseInt(parkEndTime)) {
            String uTime = String.valueOf(userStartTime);
            String hr = uTime.substring(0, 2);
            String mn = uTime.substring(2);
            int hrNum = (Integer.parseInt(hr) * 60) + Integer.parseInt(mn);

            String uTime2 = String.valueOf(userEndTime);
            String hr2 = uTime2.substring(0, 2);
            String mn2 = uTime2.substring(2);
            int hrNum2 = (Integer.parseInt(hr2) * 60) + Integer.parseInt(mn2);

            double endMinusStart = hrNum2 - hrNum;
            endMinusStart = endMinusStart / 60;
            return endMinusStart;

        //starts before start and ends before end
        } else if (userStartTime <= Integer.parseInt(parkStartTime) && userEndTime < Integer.parseInt(parkEndTime)) {
            //starts before start and ends before start
            if(userEndTime < Integer.parseInt(parkStartTime)){
                return 0;
            }else{
                String uTime = String.valueOf(userStartTime);
                String hr ="";
                String mn ="";
                if(uTime.length()==3){
                    hr = uTime.substring(0, 1);
                    mn = uTime.substring(1);
                }else if (uTime.length()==4){
                    hr = uTime.substring(0, 2);
                    mn = uTime.substring(2);
                }

                int hrNum = (Integer.parseInt(hr) * 60) + Integer.parseInt(mn);

                String uTime2 = String.valueOf(userEndTime);
                String hr2 = uTime2.substring(0, 2);
                String mn2 = uTime2.substring(2);
                int hrNum2 = (Integer.parseInt(hr2) * 60) + Integer.parseInt(mn2);

                String hr3 = parkEndTime.substring(0, 2);
                String mn3 = parkEndTime.substring(2);
                int hrNum3 = (Integer.parseInt(hr3) * 60) + Integer.parseInt(mn3);

                int endMinusStart = hrNum2 - hrNum;
                int usrEndMinusPmEnd = hrNum3 - hrNum2;
                double finalPayTime = endMinusStart - usrEndMinusPmEnd;
                finalPayTime = finalPayTime / 60;
                return finalPayTime;
            }
        //start after start ends after end
        } else if (userStartTime > Integer.parseInt(parkStartTime) && userEndTime >= Integer.parseInt(parkEndTime)) {
            //start after end and end after end
            if(userStartTime<=Integer.parseInt(parkEndTime)){
                return 0;
            }else{
                String uTime = String.valueOf(userStartTime);
                String hr = uTime.substring(0, 2);
                String mn = uTime.substring(2);
                int hrNum = (Integer.parseInt(hr) * 60) + Integer.parseInt(mn);

                String uTime2 = String.valueOf(userEndTime);
                String hr2 = uTime2.substring(0, 2);
                String mn2 = uTime2.substring(2);
                int hrNum2 = (Integer.parseInt(hr2) * 60) + Integer.parseInt(mn2);

                String hr3 = parkStartTime.substring(0, 2);
                String mn3 = parkStartTime.substring(2);
                int hrNum3 = (Integer.parseInt(hr3) * 60) + Integer.parseInt(mn3);

                int endMinusStart = hrNum2 - hrNum;
                int usrStMinusPmSt = hrNum3 - hrNum;
                double finalPayTime = endMinusStart - usrStMinusPmSt;
                finalPayTime = finalPayTime / 60;
                return finalPayTime;
            }

        //starts after start ends before end
        } else if (userStartTime > Integer.parseInt(parkStartTime) && userEndTime < Integer.parseInt(parkEndTime)) {
            String uTime = String.valueOf(userStartTime);
            String hr = uTime.substring(0, 2);
            String mn = uTime.substring(2);
            int hrNum1 = (Integer.parseInt(hr) * 60) + Integer.parseInt(mn);

            String uTime2 = String.valueOf(userEndTime);
            String hr2 = uTime2.substring(0, 2);
            String mn2 = uTime2.substring(2);
            int hrNum2 = (Integer.parseInt(hr2) * 60) + Integer.parseInt(mn2);

            double endMinusStart = hrNum2 - hrNum1;
            endMinusStart = endMinusStart / 60;
            return endMinusStart;
        }else{
            return 0;
        }



    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
