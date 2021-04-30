package com.example.fyp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class Maps extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener{

    private static final String TAG = Maps.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(53.3498, 6.2603);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    public static final String KEY1 = "ADDRESS";
    public static final String KEY2 = "LATITUDE";
    public static final String KEY3 = "LONGITUDE";


    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    Location mLastLocation;
    Marker mCurrLocationMarker;
    String searchLocation = "";


    private Marker selectedMarker;


    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    public DatabaseReference dr;
    Marker marker;
    private Map<String, Marker> markers = new HashMap<>();
    private List<ParkingMeters> pms = new ArrayList<>();
    private Map<String, CarParks> cps = new HashMap<>();
    private List<String> favs = new ArrayList<>();

    private LatLng currentLocation;
    private RelativeLayout layout;
    private DrawerLayout drawerLayout;
    private boolean free = false, one = false, oneFifty = false, two = false, twoFifty = false, three = false, threePlus =  false, allMark = false, onekm = false, twokm = false, threekm = false, fourkm = false,
            fivekm = false;
    private MenuItem freeM, oneE, oneFiftyE, twoE, twoFiftyE, threeE, threePlusE, onekil, twokil, threekil, fourkil, fivekil, allkil;

    boolean ready = false;

    private ParkingMeters currentParkingMeter;

    private PopupWindow currentPW;

    private ImageView currentIV;

    private int currentDistance = 1000;

    private Marker longClickMarker;

    String pmMetNum;

    String carparks="";
    String weather = "";
    String weatherLat="", weatherLng="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        dr = FirebaseDatabase.getInstance().getReference("Parking Meters");
        dr.push().setValue(marker);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.maps_layout);
        layout = (RelativeLayout) findViewById(R.id.layout);
        configureNavigationDrawer();
        configureToolbar();


        Intent i = getIntent();
        searchLocation = i.getStringExtra(MainActivity.LOCATION_KEY);

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.map_key));
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Intent in = getIntent();
        pmMetNum = in.getStringExtra("chosenLocation");
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void configureToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setHomeAsUpIndicator(R.drawable.ic_baseline_filter_alt_24);
        actionbar.setDisplayHomeAsUpEnabled(true);
    }

    public void configureNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.navigation);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Fragment f = null;
                int itemId = menuItem.getItemId();

                if(itemId == R.id.free){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        free = false;
                        freeM = menuItem;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        free = true;
                    }
                }else if(itemId == R.id.oneEuro){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        one = false;
                        oneE = menuItem;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        one = true;
                    }
                }else if(itemId == R.id.oneToOneFifty){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        oneFifty = false;
                        oneFiftyE=menuItem;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        oneFifty = true;
                    }
                }else if(itemId == R.id.oneFiftyToTwo){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        two = false;
                        twoE=menuItem;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        two = true;
                    }
                }else if(itemId == R.id.twoToTwoFifty){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        twoFifty = false;
                        twoFiftyE=menuItem;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        twoFifty = true;
                    }
                }else if(itemId == R.id.twoFiftyToThree){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        three = false;
                        threeE=menuItem;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        three = true;
                    }
                }else if(itemId == R.id.overThree){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        threePlus = false;
                        threePlusE=menuItem;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        threePlus = true;
                    }
                }else if(itemId == R.id.allMarkers){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        allMark = false;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        allMark = true;
                        allkil = menuItem;
                        if(onekil != null){
                            onekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(twokil != null){
                            twokil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(threekil != null){
                            threekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fourkil != null){
                            fourkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fivekil != null){
                            fivekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                    }
                }else if(itemId == R.id.oneK){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        onekm = false;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        onekm = true;
                        onekil = menuItem;
                        if(allkil != null){
                            allkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(twokil != null){
                            twokil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(threekil != null){
                            threekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fourkil != null){
                            fourkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fivekil != null){
                            fivekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                    }
                }else if(itemId == R.id.twoK){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        twokm = false;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        twokm = true;
                        twokil = menuItem;
                        if(onekil != null){
                            onekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(allkil != null){
                            allkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(threekil != null){
                            threekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fourkil != null){
                            fourkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fivekil != null){
                            fivekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                    }
                }else if(itemId == R.id.threeK){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        threekm = false;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        threekm = true;
                        threekil = menuItem;
                        if(onekil != null){
                            onekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(twokil != null){
                            twokil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(allkil != null){
                            allkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fourkil != null){
                            fourkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fivekil != null){
                            fivekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                    }
                }else if(itemId == R.id.fourK){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        fourkm = false;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        fourkm = true;
                        fourkil = menuItem;
                        if(onekil != null){
                            onekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(twokil != null){
                            twokil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(threekil != null){
                            threekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(allkil != null){
                            allkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fivekil != null){
                            fivekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }

                    }
                }else if(itemId == R.id.fiveK){
                    if(menuItem.isChecked()==true){
                        menuItem.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        fivekm = false;
                    }else if(menuItem.isChecked()==false) {
                        menuItem.setChecked(true).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_24));
                        fivekm = true;
                        fivekil=menuItem;
                        if(onekil != null){
                            onekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(twokil != null){
                            twokil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(threekil != null){
                            threekil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(fourkil != null){
                            fourkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                        if(allkil != null){
                            allkil.setChecked(false).setIcon(ContextCompat.getDrawable(Maps.this, R.drawable.ic_baseline_check_box_outline_blank_24));
                        }
                    }
                }

                List<Marker> freeMarkers = new ArrayList<>();
                List<Marker> oneMarkers = new ArrayList<>();
                List<Marker> oneFiftyMarkers = new ArrayList<>();
                List<Marker> twoMarkers = new ArrayList<>();
                List<Marker> twoFiftyMarkers = new ArrayList<>();
                List<Marker> threeMarkers = new ArrayList<>();
                List<Marker> threePlusMarkers = new ArrayList<>();
                List<Marker> priceMarkers = new ArrayList<>();
                if(itemId == R.id.apply){
                    for(Marker m: markers.values()){
                        m.setVisible(false);
                    }
                    applyFilters();
                    drawerLayout.closeDrawers();
                    if(free == true){
                        final String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()).replace(":", "");
                        for(Marker m:  markers.values()){
                            if(!m.getSnippet().equals("COACHES ONLY 24 HRS") && !m.getSnippet().equals("null")){
                                String times = m.getSnippet().replace("Monday", "").replace("Mon", "")
                                        .replace("Sunday", "").replace("Sun", "").replace("Friday", "").replace("Fri", "")
                                        .replace("Saturday", "").replace("Sat", "").replace("to", " ").replace("To", " ")
                                        .replace("-", " ").replace(".", ":").replace("000", "").replace(" ", "/")
                                        .replace("//", "/").replace("//", "/").replace("/", " ").replace(":", "").replace("  ", " ")
                                        .replace("COACHES ONLY 24 HRS", "null").replace("y", "");
                                StringBuilder time = new StringBuilder(times);
                                time.deleteCharAt(0);
                                String finalTime = String.valueOf(time);
                                String[] bothTimes = finalTime.split(" ");

                                try{
                                    if(bothTimes[0].equals("")||bothTimes[1].equals("")){

                                    }else{
                                        if(Integer.parseInt(bothTimes[0])>Integer.parseInt(currentTime)){
                                            freeMarkers.add(m);
                                        }else if(Integer.parseInt(bothTimes[1])<Integer.parseInt(currentTime)){
                                            freeMarkers.add(m);
                                        }
                                    }

                                }catch(ArrayIndexOutOfBoundsException e){

                                }



                            }

                        }
                    }else{
                        freeMarkers.clear();
                    }
                    if(one == true){
                        for(Marker m:  markers.values()){
                            if(m.getTitle().contains("\u20ac")){
                                Double price = Double.parseDouble(m.getTitle().replace("\u20ac", ""));
                                if(price<=1){
                                    oneMarkers.add(m);
                                }
                            }
                        }
                    }else{
                        oneMarkers.clear();
                    }
                    if(oneFifty==true){
                        for(Marker m:  markers.values()){
                            if(m.getTitle().contains("\u20ac")){
                                Double price = Double.parseDouble(m.getTitle().replace("\u20ac", ""));
                                if(price>= 1 && price<= 1.5 ){
                                    oneFiftyMarkers.add(m);
                                }
                            }
                        }
                    }else{
                        oneFiftyMarkers.clear();
                    }
                    if(two==true){
                        for(Marker m:  markers.values()){
                            if(m.getTitle().contains("\u20ac")){
                                Double price = Double.parseDouble(m.getTitle().replace("\u20ac", ""));
                                if(price>= 1.5 && price<=2){
                                    twoMarkers.add(m);
                                }
                            }

                        }
                    }else{
                        twoMarkers.clear();
                    }
                    if(twoFifty==true){
                        for(Marker m:  markers.values()){
                            if(m.getTitle().contains("\u20ac")){
                                Double price = Double.parseDouble(m.getTitle().replace("\u20ac", ""));
                                if(price>=2 && price<=2.5){
                                    twoFiftyMarkers.add(m);
                                }
                            }

                        }
                    }else{
                        twoFiftyMarkers.clear();
                    }
                    if(three==true){
                        for(Marker m:  markers.values()){
                            if(m.getTitle().contains("\u20ac")){
                                Double price = Double.parseDouble(m.getTitle().replace("\u20ac", ""));
                                if(price>=2.5 && price<=3){
                                    threeMarkers.add(m);
                                }
                            }

                        }
                    }else{
                        threeMarkers.clear();
                    }

                    if(threePlus==true){
                        for(Marker m:  markers.values()){
                            if(m.getTitle().contains("\u20ac")){
                                Double price = Double.parseDouble(m.getTitle().replace("\u20ac", ""));
                                if(price>=3){
                                    threePlusMarkers.add(m);
                                }
                            }
                        }
                    }else{
                        threePlusMarkers.clear();
                    }
                    for(Marker m:freeMarkers){
                        priceMarkers.add(m);
                    }
                    for(Marker m:oneMarkers){
                        priceMarkers.add(m);
                    }
                    for(Marker m:oneFiftyMarkers){
                        priceMarkers.add(m);
                    }
                    for(Marker m:twoMarkers){
                        priceMarkers.add(m);
                    }
                    for(Marker m:twoFiftyMarkers){
                        priceMarkers.add(m);
                    }
                    for(Marker m:threeMarkers){
                        priceMarkers.add(m);
                    }
                    for(Marker m:threePlusMarkers){
                        priceMarkers.add(m);
                    }


                    if(allkil != null){
                        if(priceMarkers.size()!=0){
                            if(allkil.isChecked()){
                                allMark = true;
                                try{
                                    int amount = showMarkers(10000,priceMarkers);
                                    if(amount == 0){
                                        Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                                        showMarkers(10000,  markers);
                                    }
                                    currentDistance=10000;

                                }catch(NullPointerException e){
                                }
                            }else{
                                allMark = false;
                            }
                        }else{
                            if(allkil.isChecked()){
                                allMark = true;
                                try{
                                    for (Marker marker :  markers.values()) {
                                        marker.setVisible(true);
                                    }
                                }catch(NullPointerException e){
                                }
                            }else{
                                allMark = false;
                            }
                        }
                    }
                    if(onekil != null){
                        if(priceMarkers.size()!=0){
                            if(onekil.isChecked()){
                                onekm=true;
                                int amount = showMarkers(1000, priceMarkers);
                                if(amount == 0){
                                    Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                                    showMarkers(1000,  markers);
                                }
                                currentDistance=1000;
                            }else{
                                onekm = false;
                            }
                        }else{
                            if(onekil.isChecked()){
                                onekm=true;
                                showMarkers(1000,  markers);
                                currentDistance=1000;
                            }else{
                                onekm = false;
                            }
                        }

                    }
                    if(twokil != null){
                        if(priceMarkers.size()!=0){
                            if(twokil.isChecked()){
                                twokm= true;
                                int amount = showMarkers(2000, priceMarkers);
                                if(amount == 0){
                                    Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                                    showMarkers(2000,  markers);
                                }
                                currentDistance=2000;
                            }else{
                                twokm = false;
                            }
                        }else{
                            if(twokil.isChecked()){
                                twokm= true;

                                showMarkers(2000,  markers);
                                currentDistance=2000;
                            }else{
                                twokm = false;
                            }
                        }

                    }
                    if(threekil != null){
                        if(priceMarkers.size()!=0){
                            if(threekil.isChecked()){
                                threekm = true;
                                int amount = showMarkers(3000, priceMarkers);
                                if(amount == 0){
                                    Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                                    showMarkers(3000,  markers);
                                }
                                currentDistance=3000;

                            }else{
                                threekm=false;
                            }
                        }else{
                            if(threekil.isChecked()){
                                threekm = true;
                                showMarkers(3000,  markers);
                                currentDistance=3000;
                            }else{
                                threekm=false;
                            }
                        }

                    }
                    if(fourkil != null){
                        if(priceMarkers.size()!=0){
                            if(fourkil.isChecked()){
                                fourkm=true;
                                int amount = showMarkers(4000, priceMarkers);
                                if(amount == 0){
                                    Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                                    showMarkers(4000,  markers);
                                }
                                currentDistance=4000;
                            }else{
                                fourkm = false;
                            }
                        }else{
                            if(fourkil.isChecked()){
                                fourkm=true;
                                showMarkers(4000,  markers);
                                currentDistance=4000;

                            }else{
                                fourkm = false;
                            }
                        }

                    }
                    if(fivekil != null){
                        if(priceMarkers.size()!=0){
                            if(fivekil.isChecked()){
                                fivekm = true;
                                int amount = showMarkers(5000, priceMarkers);
                                if(amount == 0){
                                    Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                                    showMarkers(5000,  markers);
                                }
                                currentDistance=5000;
                            }else{
                                fivekm = false;
                            }
                        }else{
                            if(fivekil.isChecked()){
                                fivekm = true;
                                showMarkers(5000,  markers);
                                currentDistance=5000;
                            }else{
                                fivekm = false;
                            }
                        }

                    }

                    if(allMark==false && onekm == false && twokm == false && threekm == false && fourkm == false && fivekm == false){
                        if(priceMarkers.size()!=0){
                            int amount = showMarkers(1000, priceMarkers);
                            if(amount == 0){
                                Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                                showMarkers(1000,  markers);
                            }
                        }else{
                            //Toast.makeText(getApplicationContext(), "No matching parking locations!", Toast.LENGTH_SHORT).show();
                            showMarkers(1000,  markers);
                        }
                    }


                }


                if (f != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame, f); //map
                    transaction.commit();
                    drawerLayout.closeDrawers();
                    return true;
                }
                return false;
            }
        });
    }

    public void applyFilters(){
        List <Marker> selectedMarkers = new ArrayList<Marker>();
        if(one==true && oneFifty==true && two==true && twoFifty==true && three==false){
            for(Marker m: markers.values()){
                m.setVisible(false);
                if(m.getSnippet().contains("0.") || m.getSnippet().contains("1.") || m.getSnippet().contains("2.00")){
                    m.setVisible(true);
                }
            }
        }
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            // Android home
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

            // manage other entries if you have it ...
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_get_location, menu);
        return true;
    }

    public void addNewMeter(MenuItem menuItem) throws IOException {
        Intent i = new Intent(Maps.this, TextRecognitionCamera.class);
        String address="";
        String finalAddress="";

        String lat = String.valueOf(currentLocation.latitude);
        String lng = String.valueOf(currentLocation.longitude);
        if(currentLocation!= null){
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);

            address = addresses.get(0).getAddressLine(0);
            String[] ad = address.split(",");
            finalAddress = ad[0];

        }

        i.putExtra(KEY1, finalAddress);
        i.putExtra(KEY2, lat);
        i.putExtra(KEY3, lng);
        startActivity(i);
    }

    public void openPlanner(MenuItem menuItem){
        Intent i = new Intent(Maps.this, Planner.class);
        startActivity(i);
    }



    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }



    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();



        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
// position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 2200, 2000, 0);





        final List<ParkingMeters> pmList = new ArrayList<>();
        final List<String> freeMarkers = new ArrayList<>();
        String result = "";
        if(pms.isEmpty()){
            dr = FirebaseDatabase.getInstance().getReference("Parking Meters");
            dr.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        ParkingMeters pm = snap.getValue(ParkingMeters.class);
                        pms.add(pm);
                        LatLng loc =new LatLng(Double.parseDouble(pm.getLatitude()),Double.parseDouble(pm.getLongitude()));
                        Double price = Double.parseDouble(pm.getHourlyTariff());
                        if(price<=1.5){
                            Drawable point = getResources().getDrawable(R.drawable.marker_green);
                            BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                            Marker marker = map.addMarker(new MarkerOptions().position(loc).title("\u20ac" + pm.getHourlyTariff()).visible(false).icon(icon).snippet(pm.getTimesOfOperation()));
                            markers.put(pm.getMeterNumber(), marker);
                        }else if (price > 1.5 && price <= 2.5){
                            Drawable point = getResources().getDrawable(R.drawable.marker_orange);
                            BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                            Marker marker = map.addMarker(new MarkerOptions().position(loc).title("\u20ac" +pm.getHourlyTariff()).visible(false).icon(icon).snippet(pm.getTimesOfOperation()));
                            markers.put(pm.getMeterNumber(), marker);
                        }else if(price>2.5){
                            Drawable point = getResources().getDrawable(R.drawable.marker_red);
                            BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                            Marker marker = map.addMarker(new MarkerOptions().position(loc).title("\u20ac" +pm.getHourlyTariff()).visible(false).icon(icon).snippet(pm.getTimesOfOperation()));
                            markers.put(pm.getMeterNumber(), marker);
                        }
                    }
                    showMarkers(1000, markers);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            dr = FirebaseDatabase.getInstance().getReference("Car Parks");
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        CarParks cp = snap.getValue(CarParks.class);
                        cps.put(cp.getName(), cp);
                        LatLng loc =new LatLng(Double.parseDouble(cp.getLatitude()),Double.parseDouble(cp.getLongitude()));
                        Drawable point = getResources().getDrawable(R.drawable.ic_baseline_local_parking_24);
                        BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                        Marker marker = map.addMarker(new MarkerOptions().position(loc).title(cp.getName()).snippet(cp.getTariff().substring(0,6)).visible(false).icon(icon));
                        markers.put(cp.getName(), marker);

                    }


                    showMarkers(1000, markers);

                    if (searchLocation != null) {
                        EditText locationSearch = (EditText) findViewById(R.id.inputLocation);
                        locationSearch.setText(searchLocation);
                        ImageButton b = (ImageButton) findViewById(R.id.searchLocation);
                        b.callOnClick();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if(FirebaseAuth.getInstance().getCurrentUser()!= null){
            dr = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid()).child("Favourites");
            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        String f = String.valueOf(snap.getValue());
                        favs.add(f);
                    }
                    for (Map.Entry<String, Marker> entry : markers.entrySet()) {
                        String key = entry.getKey();
                        Marker value = entry.getValue();

                        if(favs.contains(key)){
                            Drawable point = getResources().getDrawable(R.drawable.marker_blue);
                            BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                            value.setIcon(icon);
                        }
                    }
                    checkPlanner();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMapLongClickListener(this);



    }

    private void checkPlanner(){
        if(pmMetNum!=null && !pmMetNum.equals("")){
            for (Map.Entry<String, Marker> entry : markers.entrySet()) {
                String key = entry.getKey();
                Marker value = entry.getValue();
                value.setVisible(false);
                if (key.equals(pmMetNum)){
                    map.animateCamera(CameraUpdateFactory.newLatLng(value.getPosition()));
                    currentLocation=value.getPosition();
                    value.setVisible(true);
                }
            }
        }
    }



    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth()+40, drawable.getIntrinsicHeight()+40, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth() + 40, drawable.getIntrinsicHeight()+ 40);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void showMarkers(int distance, Map<String, Marker> mrk){
        try{
            for (Marker marker : mrk.values()) {
                marker.setVisible(false);
                if (SphericalUtil.computeDistanceBetween(currentLocation, marker.getPosition()) < distance) {
                    marker.setVisible(true);
                }
            }

        }catch(NullPointerException e){

        }

    }

    public int showMarkers(int distance, List<Marker> mrk){
        int counter = 0;
        try{
            for (Marker marker : mrk) {
                marker.setVisible(false);
                if (SphericalUtil.computeDistanceBetween(currentLocation, marker.getPosition()) < distance) {
                    marker.setVisible(true);
                    counter++;
                }
            }

        }catch(NullPointerException e){

        }
        return counter;
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                currentLocation =  new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));



                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = map.addMarker(markerOptions);

        //move map camera
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(11));


    }

    public void searchLocation(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.inputLocation);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;



        if (location != null && !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                Toast.makeText(getApplicationContext(), location, Toast.LENGTH_SHORT).show();
                addressList = geocoder.getFromLocationName(location, 1);
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                Location startPoint=new Location("dublin");
                startPoint.setLatitude(53.417629);
                startPoint.setLongitude(-6.251626);

                Location endPoint=new Location("searchLocation");
                endPoint.setLatitude(latLng.latitude);
                endPoint.setLongitude(latLng.longitude);
                double distance=startPoint.distanceTo(endPoint);
                if(distance<30000){
                    currentLocation = latLng;
                    //map.addMarker(new MarkerOptions().position(latLng).title(location));
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    for (Marker marker : markers.values()) {
                        marker.setVisible(false);
                        if (SphericalUtil.computeDistanceBetween(latLng, marker.getPosition()) < currentDistance) {
                            marker.setVisible(true);
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Error: Please Search Within Dublin.", Toast.LENGTH_LONG).show();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }catch(IndexOutOfBoundsException i){
                Toast.makeText(getApplicationContext(), "Error: Location Could Not Be Found.", Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(getApplicationContext(), "Error: Search Box Empty.", Toast.LENGTH_LONG).show();
        }
    }

    public void clearSearch(View v){
        EditText locationSearch = (EditText) findViewById(R.id.inputLocation);
        locationSearch.setText(null);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        selectedMarker = marker;
        TextView p = (TextView)findViewById(R.id.bottomPrice);
        p.setText("");
        TextView c = (TextView)findViewById(R.id.bottomCap);
        c.setText("");
        TextView h = (TextView)findViewById(R.id.bottomHours);
        h.setText("");

        LinearLayout l1 = (LinearLayout)findViewById(R.id.bottomTextLayout1);
        LinearLayout l2 = (LinearLayout)findViewById(R.id.bottomTextLayout2);

        Button b = (Button)findViewById(R.id.bottomButton);

        Button busyness = (Button)findViewById(R.id.busynessButton);

        if(marker.getTitle().contains("\u20AC")){

            b.setClickable(true);

            String id = "";
            String capacity = "";
            String hrs = "";
            String street = "";
            for (Map.Entry<String, Marker> entry : markers.entrySet()) {
                String key = entry.getKey();
                Marker value = entry.getValue();

                if(value.getPosition().toString().equals(marker.getPosition().toString())){
                    id = key;
                }
            }
            for(ParkingMeters parking: pms){
                if(id.equals(parking.getMeterNumber())){
                    currentParkingMeter =parking;
                    capacity = parking.getNumSpace();
                    hrs = parking.getTimesOfOperation();
                    street = parking.getLocation();

                }
            }
            String price = "Price: "+marker.getTitle();
            String cap = "Capacity: " + capacity.replace("(Total)", "");
            String hours = "Hours: " + hrs;

            p.setText(price);
            c.setText(cap);
            h.setText(hours);


            l1.setVisibility(View.VISIBLE);
            l2.setVisibility(View.VISIBLE);

            b.setText(street);
            b.setVisibility(View.VISIBLE);


            busyness.setVisibility(View.VISIBLE);
        }else{
            b.setClickable(false);
            String id = "";
            String capacity = "";
            String hrs = "";
            String street = "";
            for (Map.Entry<String, CarParks> entry : cps.entrySet()) {
                String key = entry.getKey();
                CarParks value = entry.getValue();

                if(key.equals(marker.getTitle())){
                    capacity = value.getCapacity();
                    hrs = value.getOpeningTimes();
                    street = value.getName();
                }
            }

            String price = "Price: "+marker.getSnippet();
            String cap = "Capacity: " + capacity;
            String hours = "Hours: " + hrs;

            p.setText(price);
            c.setText(cap);
            h.setText(hours);


            l1.setVisibility(View.VISIBLE);
            l2.setVisibility(View.VISIBLE);

            b.setText(street);
            b.setVisibility(View.VISIBLE);


            busyness.setVisibility(View.INVISIBLE);
        }


        return false;

    }



    public void showDetailedInfo(View v){
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.detailed_info_layout, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = 1330;//LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation);

        View myView = (DrawerLayout) findViewById(R.id.drawer_layout);
        // show the popup window
        popupWindow.showAtLocation(myView, Gravity.BOTTOM, 0, 0);
        currentPW=popupWindow;

        ImageView x = (ImageView) popupView.findViewById(R.id.lower);

        ImageView f = (ImageView) popupView.findViewById(R.id.fav);
        currentIV=f;
        boolean isFav = false;
        for(String fv:favs){
            if(fv.equals(currentParkingMeter.getMeterNumber())){
                isFav = true;
            }
        }

        if(isFav == true){
            f.setImageResource(R.drawable.ic_baseline_favorite_24);
        }else{
            f.setImageResource(R.drawable.ic_baseline_favorite_border_24);
        }
        TextView n = (TextView) popupView.findViewById(R.id.nameheading);
        TextView p = (TextView) popupView.findViewById(R.id.price);
        TextView c = (TextView) popupView.findViewById(R.id.capacity);
        TextView h = (TextView) popupView.findViewById(R.id.hours);
        TextView r = (TextView) popupView.findViewById(R.id.restrictions);
        TextView ar = (TextView) popupView.findViewById(R.id.areaRef);
        TextView mn = (TextView) popupView.findViewById(R.id.meterNum);

        n.setText(currentParkingMeter.getLocation());
        p.setText("\u20ac" + currentParkingMeter.getHourlyTariff());
        c.setText(currentParkingMeter.getNumSpace());
        h.setText(currentParkingMeter.getTimesOfOperation());
        r.setText(currentParkingMeter.getRestrictions());
        ar.setText(currentParkingMeter.getAreaRef());
        mn.setText(currentParkingMeter.getMeterNumber());

    }

    public void lower(View v){
        currentPW.dismiss();
    }

    public void manageFavourites(View v){
        ImageView ib = currentIV;
        if(FirebaseAuth.getInstance().getCurrentUser()!= null){
            dr = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid()).child("Favourites");
            boolean found = false;
            for(String f: favs){
                if(f.equals(currentParkingMeter.getMeterNumber())){
                    found = true;
                }
            }

            if(found == true){
                dr.child(currentParkingMeter.getMeterNumber()).removeValue();
                favs.remove(currentParkingMeter.getMeterNumber());
                dr.setValue(favs);
                ib.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                for (Map.Entry<String, Marker> entry : markers.entrySet()) {
                    String key = entry.getKey();
                    Marker value = entry.getValue();
                    if(key.equals(currentParkingMeter.getMeterNumber())){
                        Double price = Double.parseDouble(currentParkingMeter.getHourlyTariff());
                        if(price<=1.5){
                            Drawable point = getResources().getDrawable(R.drawable.marker_green);
                            BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                            value.setIcon(icon);
                        }else if (price > 1.5 && price <= 2.5){
                            Drawable point = getResources().getDrawable(R.drawable.marker_orange);
                            BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                            value.setIcon(icon);
                        }else if(price>2.5){
                            Drawable point = getResources().getDrawable(R.drawable.marker_red);
                            BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                            value.setIcon(icon);
                        }
                    }

                }
                Toast.makeText(Maps.this, "Removed From Favourites!", Toast.LENGTH_SHORT).show();

            }else{
                for(ParkingMeters prk:pms){
                    if(prk.getMeterNumber().contains(currentParkingMeter.getMeterNumber())){
                        favs.add(prk.getMeterNumber());
                        dr.setValue(favs);
                        ib.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Toast.makeText(Maps.this, prk.getLocation() +" Added To Favourites!", Toast.LENGTH_SHORT).show();
                        for (Map.Entry<String, Marker> entry : markers.entrySet()) {
                            String key = entry.getKey();
                            Marker value = entry.getValue();
                            if(key.equals(currentParkingMeter.getMeterNumber())){
                                Drawable point = getResources().getDrawable(R.drawable.marker_blue);
                                BitmapDescriptor icon = getMarkerIconFromDrawable(point);
                                value.setIcon(icon);
                            }

                        }

                    }
                }

            }
        }else{
            Toast.makeText(getApplicationContext(), "You must be logged in to use these features!", Toast.LENGTH_SHORT).show();

        }
    }



    public void checkBusyness(View v) throws ExecutionException, InterruptedException {

        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.availability_layout, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = 1500;//LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation);
        currentPW=popupWindow;

        View myView = (DrawerLayout) findViewById(R.id.drawer_layout);
        // show the popup window
        popupWindow.showAtLocation(myView, Gravity.BOTTOM, 0, 0);

        LatLng currLatLng = selectedMarker.getPosition();

        String spaces = "";
        ImageView x = (ImageView) popupView.findViewById(R.id.lower);

        TextView c = (TextView) popupView.findViewById(R.id.avgCarPark);
        TextView w = (TextView) popupView.findViewById(R.id.weather);
        TextView t = (TextView) popupView.findViewById(R.id.timeDate);
        TextView ev = (TextView) popupView.findViewById(R.id.event);
        TextView d = (TextView) popupView.findViewById(R.id.day);
        TextView rslt = (TextView) popupView.findViewById(R.id.result);
        ImageView a1 = (ImageView)popupView.findViewById(R.id.avail1img);
        ImageView a2 = (ImageView)popupView.findViewById(R.id.avail2img);
        ImageView a3 = (ImageView)popupView.findViewById(R.id.avail3img);
        ImageView a4 = (ImageView)popupView.findViewById(R.id.avail4img);
        ImageView a5 = (ImageView)popupView.findViewById(R.id.avail5img);



        String liveInfoString = "https://opendata.dublincity.ie/TrafficOpenData/CP_TR/CPDATA.xml";
        carparks = new Maps.GetDataTask().execute(liveInfoString).get();

        LatLng place = selectedMarker.getPosition();


        String[] arrOfString = carparks.split(",");

        double spacePercentage = 0;
        int counter = 0;
        ArrayList<Integer> totalSpaces = new ArrayList<>();

        for(CarParks cp: cps.values()){
            LatLng thisLoc = new LatLng(Double.parseDouble(cp.getLatitude()), Double.parseDouble(cp.getLongitude()));
            if (SphericalUtil.computeDistanceBetween(currLatLng, thisLoc) < 500) {
                for(String s: arrOfString){
                    if(s.contains(cp.getName().toUpperCase())){
                        String spc = s.replace(cp.getName().toUpperCase(), "").replace("spaces=", "").replace(" ", "");
                        double free = Double.parseDouble(spc);
                        double total = Double.parseDouble(cp.getCapacity());
                        double result = free/total;
                        double prc = result*100;

                        spacePercentage = spacePercentage + prc;
                        counter ++;

                    }
                }
            }
        }
        double spaceAmount=0;
        if(spacePercentage != 0){
            spaceAmount = 100 - (spacePercentage/counter);
        }

        DecimalFormat decF = new DecimalFormat("#.##");
        String sa = decF.format(spaceAmount);

        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        weatherLat = df.format(place.latitude);
        weatherLng = df.format(place.longitude);

        String weatherString = "https://api.openweathermap.org/data/2.5/weather?lat="+weatherLat+"&lon="+weatherLng+"&appid=a9938bb254af4ef618c72c488b9818ce&mode=xml&units=metric";
        weather = new Maps.GetDataTask().execute(weatherString).get();

        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

        String eventString = "https://dublin.ie/whats-on/";
        String currentEvents = new Maps.GetDataTask().execute(eventString).get();




        int cap =1, wthr=1, dy=1, tm=1, eve=1;

        if(Double.parseDouble(sa)==00.0){
            cap = 0;
        }else if(Double.parseDouble(sa)<=10.0){
            cap = 1;
        }else if(Double.parseDouble(sa)>10.0 && Double.parseDouble(sa)<=20.0){
            cap = 2;
        }else if(Double.parseDouble(sa)>20.0 && Double.parseDouble(sa)<=40.0){
            cap = 3;
        }else if(Double.parseDouble(sa)>40.0 && Double.parseDouble(sa)<=60.0){
            cap = 4;
        }else if(Double.parseDouble(sa)>60.0){
            cap = 5;
        }

        String[] tempArray = weather.split("\n");
        String justTemp = tempArray[0].replace("Temperature: ", "").replace("\u00B0C", "");

        if(Double.parseDouble(justTemp)>=12 ){
            if(!weather.contains("showers") || !weather.contains("rain")){
                wthr = 1;
            }else {
                wthr = 3;
            }
        }else {
            if(weather.contains("showers") || weather.contains("rain")){
                wthr = 5;
            }else{
                wthr = 3;
            }
        }

        if(day.equals("Monday") || day.equals("Tuesday") || day.equals("Sunday")){
            dy = 4;
        }else if(day.equals("Wednesday") || day.equals("Thursday")){
            dy = 3;
        }else if(day.equals("Friday") || day.equals("Saturday")){
            dy = 5;
        }

        String[] timeArray = currentTime.split(":");
        String hour = timeArray[0];

        if(hour.equals("00") || hour.equals("01") || hour.equals("02") || hour.equals("03") || hour.equals("04") || hour.equals("21") || hour.equals("22") || hour.equals("23")){
            tm = 1;
        }else if(hour.equals("05") || hour.equals("11") || hour.equals("20")){
            tm = 2;
        }else if(hour.equals("06") || hour.equals("12") || hour.equals("13") || hour.equals("14") || hour.equals("15") || hour.equals("19")){
            tm = 3;
        }else if(hour.equals("07") || hour.equals("10") || hour.equals("16") || hour.equals("18")){
            tm = 4;
        }else if(hour.equals("08") || hour.equals("09") || hour.equals("17")){
            tm = 5;
        }

        if(currentEvents.equals("No events.")){
            eve = 2;
        }else{
            eve = 4;
        }

        Double rating = Double.valueOf((cap + wthr + dy + tm + eve)/5);

        a1.setVisibility(View.INVISIBLE);
        a1.setVisibility(View.INVISIBLE);
        a1.setVisibility(View.INVISIBLE);
        a1.setVisibility(View.INVISIBLE);
        a1.setVisibility(View.INVISIBLE);

        if(rating<1.5){
            a1.setVisibility(View.VISIBLE);
            rslt.setText("Not busy.");
        }else if(rating>=1.5 && rating<2.6){
            a2.setVisibility(View.VISIBLE);
            rslt.setText("Slightly busy.");
        }else if(rating>=2.5 && rating<3.6){
            a3.setVisibility(View.VISIBLE);
            rslt.setText("Busy.");
        }else if(rating>=3.5 && rating<4.6){
            a4.setVisibility(View.VISIBLE);
            rslt.setText("Very busy.");
        }else if(rating>=4.6){
            a5.setVisibility(View.VISIBLE);
            rslt.setText("Extremely busy.");
        }

        c.setText("%"+ sa);
        w.setText(weather);
        t.setText(currentTime);
        ev.setText(currentEvents);
        d.setText(day);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;

            }
        });

    }




    private class GetDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String[] params) {
            String urlString = params[0];
            String add ="";

            try {
                URL url = new URL(urlString);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                if (httpURLConnection.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    String[] arrOfString = null;
                    String[] tempArray = null;
                    String temp="";
                    DecimalFormat decF = new DecimalFormat("#");


                    if(urlString.equals("https://opendata.dublincity.ie/TrafficOpenData/CP_TR/CPDATA.xml")){
                        dr = FirebaseDatabase.getInstance().getReference();
                        while ((line = reader.readLine()) != null) {
                            line = line.replace("\"","").replace("<carpark name=","").replace("> </carpark>", ",")
                                    .replace("MARLBORO", "MARLBOROUGH").replace("C/CHURCH", "CHRIST CHURCH").replace("ABBEY", "IRISH LIFE")
                                    .replace("GREENRCS", "STEPHENS GREEN").replace("B/THOMAS", "BROWN THOMAS");
                            if(line.contains("spaces")){
                                if(line.contains("spaces= ,")){
                                }else{
                                    add += line;
                                }
                            }
                        }
                    }else if(urlString.equals("https://api.openweathermap.org/data/2.5/weather?lat="+weatherLat+"&lon="+weatherLng+"&appid=a9938bb254af4ef618c72c488b9818ce&mode=xml&units=metric")){
                        dr = FirebaseDatabase.getInstance().getReference();
                        while ((line = reader.readLine()) != null) {
                            line = line.replace("<", "").replace("/", "");

                            arrOfString = line.split(">");
                            for(String s: arrOfString){
                                if(s.contains("temperature value=")){
                                    tempArray = s.replace("temperature value","temperaturevalue").split("\" ");
                                    add += tempArray[0].replace("temperaturevalue=", "Temperature: ").replace("\"", "") + "\u00B0C";
                                }else if(s.contains("weather number")){
                                    tempArray = s.replace("weather number", "weathernumber").split("\" ");
                                    add += "\n" + tempArray[1].replace("value=", "Weather: ").replace("\"", "");
                                }

                            }
                        }
                    } if(urlString.equals("https://dublin.ie/whats-on/")){
                        String events="";
                        Document dcmnt = null;
                        try {
                            dcmnt = Jsoup.connect("https://dublin.ie/whats-on/").get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Elements article = dcmnt.select("article");
                        for(Element a : article){
                            String name = "", location = "", day="", month="", year="", date1="", date2="";
                            Elements h2 = dcmnt.select("h2");
                            for (Element h: h2){
                                name = h2.text();
                            }

                            Elements pElem = dcmnt.select("p");
                            for (Element p: pElem){
                                location = p.text();
                            }

                            if(!location.equals("Online")){
                                Elements time = dcmnt.select("time");
                                for (Element t: time){
                                    if(t.text().contains("-")){
                                        String [] dates = t.text().split("-");
                                        int counter =1;
                                        for(String s: dates){
                                            day = t.text().replace("Mon","").replace("Tue","").replace("Wed","").replace("Thu","")
                                                    .replace("Fri","").replace("Sat","").replace("Sun","").replace("Jan","")
                                                    .replace("Feb","").replace("Mar","").replace("Apr","").replace("May","")
                                                    .replace("Jun","").replace("Jul","").replace("Aug","").replace("Sep","")
                                                    .replace("Oct","").replace("Nov","").replace("Dec","").replace("st","")
                                                    .replace("nd","").replace("rd","").replace("th","").replace(" ", "");
                                            month = t.text().replace(day, "").replace("st","")
                                                    .replace("nd","").replace("rd","").replace("th","").replace("Mon","")
                                                    .replace("Tue","").replace("Wed","").replace("Thu","")
                                                    .replace("Fri","").replace("Sat","").replace("Sun","").replace(" ", "");

                                            Calendar calendar = Calendar.getInstance();
                                            year = String.valueOf(calendar.get(Calendar.YEAR));

                                            if(counter == 1){
                                                date1 = day + "-" + month + "-" + year;
                                            }else if (counter == 2){
                                                date2 = day + "-" + month + "-" + year;
                                            }
                                            counter ++;
                                        }
                                    }

                                }

                                Calendar calendar = Calendar.getInstance();
                                Date today = calendar.getTime();


                                SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yyyy");
                                Date d1 = fmt.parse(date1);
                                Date d2 = fmt.parse(date2);

                                if(d1.compareTo(today) * today.compareTo(d2) >=0){
                                    events += name + "\n";
                                }
                            }


                        }
                        if(events.equals( "")){
                            events = "No events.";
                        }
                        add = events;
                    }


                    httpURLConnection.disconnect();

                } else {
                    Toast.makeText(Maps.this, "Unable to contact database.", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return add;
        }
    }




    @Override
    public void onMapClick(LatLng latLng) {
        LinearLayout l1 = (LinearLayout)findViewById(R.id.bottomTextLayout1);
        LinearLayout l2 = (LinearLayout)findViewById(R.id.bottomTextLayout2);
        l1.setVisibility(View.INVISIBLE);
        l2.setVisibility(View.INVISIBLE);
        Button b = (Button)findViewById(R.id.bottomButton);
        b.setVisibility(View.INVISIBLE);

        Button busyness = (Button)findViewById(R.id.busynessButton);
        busyness.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        currentLocation=latLng;

        try{
            longClickMarker.remove();
        }catch (NullPointerException e){

        }

        longClickMarker = map.addMarker(new MarkerOptions().position(latLng));

        for (Marker marker : markers.values()) {
            marker.setVisible(false);
            if (SphericalUtil.computeDistanceBetween(latLng, marker.getPosition()) < currentDistance) {
                marker.setVisible(true);
            }
        }
    }
}