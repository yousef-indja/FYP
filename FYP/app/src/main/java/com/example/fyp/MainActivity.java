package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String stringGeoJsonData;

    public static final String LOCATION_KEY = "LOCATION";
    public static final String GEODATA = "GEODATA";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    public DatabaseReference dr;
    GoogleSignInClient mGoogleSignInClient;
    com.google.android.gms.common.SignInButton mSignInButton;

    Button addParking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        mSignInButton=(com.google.android.gms.common.SignInButton)findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        addParking = (Button)findViewById(R.id.addParkingButton);
        if(mUser != null){
            if(mUser.getEmail().equals("indja98@gmail.com")){
                addParking.setVisibility(View.VISIBLE);
            }
        }

    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_homescreen, menu);
        return true;
    }

    public void signUp(MenuItem menuItem){

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Toast.makeText(MainActivity.this, "Please Sign In First!", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("MainActivity", "usersignedin");
            Intent in = new Intent(MainActivity.this, UserPage.class);
            startActivity(in);
        }

    }

    public void maps(View v){

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Toast.makeText(MainActivity.this, "Please Sign In First!", Toast.LENGTH_SHORT).show();

        }else{
            Intent i = new Intent(MainActivity.this, Maps.class);
            i.putExtra(GEODATA, stringGeoJsonData);
            startActivity(i);
        }

    }

    public void enterLocation(View v){
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Toast.makeText(MainActivity.this, "Please Sign In First!", Toast.LENGTH_SHORT).show();
        }else{
            TextView l = (TextView)findViewById(R.id.location);
            String location = l.getText().toString();
            List<Address> addressList = null;
            LatLng latLng;
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
                        Intent i = new Intent(MainActivity.this, Maps.class);
                        i.putExtra(LOCATION_KEY, location);
                        startActivity(i);
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

        }



    }
    public void myLocation(View v){
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Toast.makeText(MainActivity.this, "Please Sign In First!", Toast.LENGTH_SHORT).show();
        }else{
            Intent i = new Intent(MainActivity.this, Maps.class);
            startActivity(i);
        }


    }



    public void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 2);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 2) {

            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            final GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

            if(acct == null){
            }else{
                AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    final FirebaseUser user = mAuth.getCurrentUser();
                                    final User user1 = new User(acct.getDisplayName(), acct.getEmail());
                                    dr = FirebaseDatabase.getInstance().getReference().child("Users");
                                    dr.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            boolean dup = false;
                                            for (DataSnapshot snap: dataSnapshot.getChildren()) {
                                                User u = snap.getValue(User.class);
                                                if(u.getEmail().equals(acct.getEmail())){
                                                    dup = true;
                                                }
                                            }

                                            if(dup == true){
                                                Toast.makeText(MainActivity.this, acct.getEmail() + " signed in!", Toast.LENGTH_SHORT).show();
                                                Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                                                startActivity(refresh);
                                            }else{
                                                dr.child(user.getUid()).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(MainActivity.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                                                        Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                                                        startActivity(refresh);
                                                    }
                                                })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(MainActivity.this, "Error: Sign Up Failure!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                } else {
                                    Toast.makeText(MainActivity.this, "Error: Sign Up Failure!" + task.getException(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

            }

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(MainActivity.this, "Error: Sign Up Failure! Start" + String.valueOf(e.getStatusCode()), Toast.LENGTH_SHORT).show();
        }
    }

    public void addParking(View v){
        String dlrGeoString = "https://data.smartdublin.ie/dataset/41660b5a-e0f2-4694-af90-e6a53a1ecbc2/resource/ccab4d09-1a2c-4bbf-9046-9903d075e822/download/dlr_parking_meter.geojson";
        String dccGeoString = "https://data.smartdublin.ie/dataset/58969481-417e-4f5a-b8ea-18b56419d0ed/resource/a38b3d50-96ae-495e-ae69-899d833404cf/download/dccrdpandd.geojson";
        new MainActivity.GetDataTask().execute(dlrGeoString);
        new MainActivity.GetDataTask().execute(dccGeoString);
        Toast.makeText(MainActivity.this, "Parking Meters Updated!", Toast.LENGTH_SHORT).show();
    }

    private class GetDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            String urlString = params[0];
            String carparks="";
            try {
                URL url = new URL(urlString);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                if (httpURLConnection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    String l = "";
                    String id = "";

                    String[] arrOfString = null;
                    String[] arrOfLatLong = null;
                    String correctLatLng = "";
                    List<LatLng> polys = new ArrayList<>();

                    int counter = 0;
                    int counting =0;
                    String geoMeter = "", lat = "", lng = "", location= "", areaRef= "", numSpaces= "", hours= "",days= "", tarrif= "", restrictions;
                    ParkingMeters pm = new ParkingMeters();
                    dr = FirebaseDatabase.getInstance().getReference();
                    while ((line = reader.readLine()) != null) {
                        if (urlString.equals("https://data.smartdublin.ie/dataset/41660b5a-e0f2-4694-af90-e6a53a1ecbc2/resource/ccab4d09-1a2c-4bbf-9046-9903d075e822/download/dlr_parking_meter.geojson")) {
                            if (line.contains("coordinates")) {
                                l = line;
                                l = l.replace("\"", "").replace("coordinates", "").replace("[", "").replace("]", "").
                                        replace(":", "").replace("\t", "");
                                arrOfLatLong = l.split(",");
                                int cnt = 0;
                                for (String s : arrOfLatLong) {
                                    if (cnt == 0) {
                                        lng = s.replace(" ", "");
                                        cnt++;
                                    } else if (cnt == 1) {
                                        lat = s.replace(" ", "");;
                                        cnt++;
                                    }
                                }
                                counting++;
                            }

                            String meter = "";
                            if (line.contains("Meter_Number")) {
                                geoMeter = line;
                                geoMeter = geoMeter.replace("\"Meter_Number\" : ", "").replace(",", "").replace("\t", "") + "(DLR)";
                                counting++;
                            }

                            if (line.contains("Location")) {
                                location = line.replace("\"Location\" : \"", "").replace("\",", "").replace("\t", "");
                                counting++;
                            }


                            if (line.contains("Area_Ref")) {
                                areaRef = line.replace("\"Area_Ref\" : \"", "").replace("\",", "").replace("\t", "");
                                counting++;
                            }

                            if (line.contains("Number_of_Paid_Parking_Spaces_")) {
                                numSpaces = line.replace("\"Number_of_Paid_Parking_Spaces_\" : ", "").replace(",", "").replace("\t", "");
                                counting++;
                            }

                            if (line.contains("Hours_of_operation")) {
                                hours = line.replace("\"Hours_of_operation\" : \"", "").replace("\",", "").replace("\t", "");
                                counting++;
                            }

                            if (line.contains("Days_of_operation")) {
                                days = line.replace("\"Days_of_operation\" : \"", "").replace("\",", "").replace("\t", "").replace("Saturda", "Saturday");
                                counting++;
                            }

                            if (line.contains("Hourly_Tarrif")) {
                                tarrif = line.replace("\"Hourly_Tarrif\" : ", "").replace(",", "").replace("\t", "");
                                if(tarrif.contains("1.0") || tarrif.contains("2.") || tarrif.contains("0.")){
                                    tarrif = tarrif+"0";
                                }
                                counting++;
                            }

                            if(line.contains("Restrictions")){
                                restrictions = line.replace("\"Restrictions\" : \"", "").replace("\"", "").replace("\t", "");
                                ParkingMeters p = new ParkingMeters(geoMeter, location, areaRef, numSpaces, (days+ " " + hours), tarrif, restrictions, lat, lng);
                                dr.child("Parking Meters").child(geoMeter).setValue(p);
                                counting = 0;
                            }else if(counting == 8){
                                restrictions = "None";
                                ParkingMeters p = new ParkingMeters(geoMeter, location, areaRef, numSpaces, (days+ " " + hours), tarrif, restrictions, lat, lng);
                                dr.child("Parking Meters").child(geoMeter).setValue(p);
                                counting = 0;
                            }

                        } else if(urlString.equals("https://data.smartdublin.ie/dataset/58969481-417e-4f5a-b8ea-18b56419d0ed/resource/a38b3d50-96ae-495e-ae69-899d833404cf/download/dccrdpandd.geojson")){
                            line = line.replace("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[", "").replace(",{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[", "").
                                    replace("]},\"properties\":{\"", "").replace(",{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[", "").replace("{\"name\":\"DCC_RD_PandD\",\"type\":\"FeatureCollection\"", "")
                                    .replace(",[-", "").replace(",53", "").replace(",51", "").replace("]]},\"properties\":{\"", "").replace(",\"features\":[", "").replace("}}", "").replace(",-", "")
                                    .replace("Ave,", "Ave").replace(", ", " ");


                            arrOfString = line.replace("19.00,\"","19.00\"").replace("\"", "").split(",");

                            int count = 1;
                            ParkingMeters pms = new ParkingMeters();
                            for (String s : arrOfString) {

                                if (count == 1) {
                                    count++;
                                } else if (count == 2) {
                                    pms.setTimesOfOperation(s.replace("Hours_of_Operation:", ""));
                                    count++;
                                } else if (count == 3) {
                                    count++;
                                } else if (count == 4) {
                                    if (s.startsWith("No") || s.startsWith("Max") || s.startsWith("Credit") || s.startsWith("Bus")) {
                                        pms.setRestrictions(s.replace("Further_Information:", ""));
                                    } else {
                                        pms.setRestrictions("None");
                                    }
                                    count++;
                                } else if (count == 5) {
                                    count++;
                                } else if (count == 6) {
                                    count++;
                                    if (s ==null) {
                                        pms.setAreaRef("Dublin City");
                                    } else {
                                        pms.setAreaRef(s.replace("Exact_Location:", ""));
                                    }
                                } else if (count == 7) {
                                    count++;
                                } else if (count == 8) {
                                    count++;
                                    String t = s.replace("Tariff:", "");
                                    if(t.contains("1.") || t.contains("2.") || t.contains("0.")){
                                        t = t+"0";
                                    }
                                    pms.setHourlyTariff(t);
                                } else if (count == 9) {
                                    count++;
                                    pms.setMeterNumber(s.replace("No:","") + "(DCC)");
                                } else if (count == 10) {
                                    count++;
                                    pms.setLocation(s.replace("Location:", ""));
                                } else if (count == 11) {
                                    count++;
                                } else if (count == 12) {
                                    count++;
                                } else if (count == 13) {
                                    count++;
                                    pms.setNumSpace(s.replace("No_Spaces:", ""));
                                } else if (count == 14) {
                                    count++;
                                    pms.setLongitude(s.replace("Longitude:", ""));
                                } else if (count == 15) {
                                    pms.setLatitude(s.replace("Latitude:", ""));
                                    count++;
                                    dr.child("Parking Meters").child(pms.getMeterNumber()).setValue(pms);

                                }
                            }
                        }
                    }

                    CarParks parnell = new CarParks("Parnell", "500", "Open 24-7", "\u20ac3.50 per hour. Max. 24hr charge \u20ac23.00", "Overnight rate \u20ac13.00 (\u20ac8.50 if paid in advance at pay station within 15 minutes of arrival) 17:30-08:30. Student Rate \u20ac3.50 per hour, \u20ac11.00 per day(up to Midnight). Sunday Day Rate \u20ac15.00. Top Up value card available.","500", "53.35063438682471", "-6.268399380362479");
                    CarParks ilac = new CarParks("Ilac", "1000", "Mon, Tues, Wed, Fri, Sat: 7:00-21:00, Thur: 7:00-22:00, Sun: 10:00-20:00", "\u20ac3.40 per hour. Max. charge of \u20ac36.00 for 24 hours", "None","1000", "53.35098284246581", "-6.264887230045508");
                    CarParks arnotts = new CarParks("Arnotts", "360", "Mon-Fri: 07:30-19.30, Sat:07:30-18:00", "\u20ac3.00 per hour. \u20ac15.00 daily max charge.", "\u20ac7.00 for 3 hours for customers in Arnotts, scan barcode at the pay station to avail of discount","360", "53.34910929509294", "-6.262233398160084");
                    CarParks marlborough = new CarParks("Marlborough", "570", "Open 24-7", "\u20ac3.00 per hour for first 2 hours, \u20ac4.00 per hour for up to 7 hours. \u20ac32.00 for 24 hours.", "Overnight rate: \u20ac10.00 for 19:00-08:00. Sunday: \u20ac12.00 for the day.","567", "53.35259737820968", "-6.258405756017644");
                    CarParks setanta = new CarParks("Setanta", "150", "Open 24-7", "\u20ac4.00 per hour for first 2 hours, \u20ac5.00 per hour for up to 7 hours. \u20ac36.00 for 24 hours.", "Overnight rate(17:00-08:00): \u20ac12.00 ","146", "53.34221493322797", "-6.255986376900927");
                    CarParks dawson = new CarParks("Dawson", "370", "Open 24-7", "\u20ac4.00 per hour. \u20ac40.00 for 24 hours.", "Overnight rate(17:00-09:00): \u20ac12.00. Sundays(09:00-21:00): \u20ac10.00 ","370", "53.34058433243418", "-6.255834648258611");
                    CarParks stephensGreen = new CarParks("Stephens Green", "1210", "Open 24-7", "\u20ac3.90 per hour. \u20ac38.00 for 24 hours.", "Overnight rate(19:00-08:00): \u20ac10.00. Evenings: Pay on Arrival(17:00-03:00): \u20ac8.00 ","1127", "53.33955158634798", "-6.2625410542489695");
                    CarParks drury = new CarParks("Drury", "1130", "Open 24-7", "\u20ac4.00 per hour.", "Evening rate(19:00-02:00): \u20ac10.00. Night rate(19:00-09:00) \u20ac12.00. Sunday Day Rate 05:30-17:30: \u20ac11.00. Sunday Max Daily Charge 05:30-02:00: \u20ac21.00","465", "53.34192798631105", "-6.2640354949229415");
                    CarParks brownThomas = new CarParks("Brown Thomas", "380", "Open 24-7", "\u20ac3.60 per hour.", "Overnight rate(17:00-09:00): \u20ac8.00.","380", "53.34309142926272", "-6.261473292632861");

                    dr.child("Car Parks").child(parnell.getName()).setValue(parnell);
                    dr.child("Car Parks").child(ilac.getName()).setValue(ilac);
                    dr.child("Car Parks").child(arnotts.getName()).setValue(arnotts);
                    dr.child("Car Parks").child(marlborough.getName()).setValue(marlborough);
                    dr.child("Car Parks").child(setanta.getName()).setValue(setanta);
                    dr.child("Car Parks").child(dawson.getName()).setValue(dawson);
                    dr.child("Car Parks").child(stephensGreen.getName()).setValue(stephensGreen);
                    dr.child("Car Parks").child(drury.getName()).setValue(drury);
                    dr.child("Car Parks").child(brownThomas.getName()).setValue(brownThomas);


                    httpURLConnection.disconnect();



                } else {
                    Log.w("Error", "Couldnt contact database");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            if (carparks.equals("")){
                return "done";
            }else{
                return carparks;
            }

        }
    }

}