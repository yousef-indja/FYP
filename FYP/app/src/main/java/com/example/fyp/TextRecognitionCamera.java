package com.example.fyp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;


import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class TextRecognitionCamera extends AppCompatActivity {

    private static final String TAG = null;
    private Button captureImageButton, detectTextButton;
    private ImageView imageView;
    private TextView textView, capInfo;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;
    private Image image;
    private String currentPhotoPath;
    private Uri photoURI;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private LatLng currentLocation;

    private String address, lat,lng;

    public static final String LOCATION = "MESSAGE1";
    public static final String HOURS = "MESSAGE2";
    public static final String PRICE = "MESSAGE3";
    public static final String RESTRICTIONS = "MESSAGE4";
    public static final String LATITUDE = "MESSAGE5";
    public static final String LONGITUDE = "MESSAGE6";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_text_recognition);

        captureImageButton = findViewById(R.id.capture_image);
        detectTextButton = findViewById(R.id.detect_image);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_display);

        capInfo = findViewById(R.id.captureInfo);

        Intent i = getIntent();
        address = i.getStringExtra(Maps.KEY1);
        lat = i.getStringExtra(Maps.KEY2);
        lng = i.getStringExtra(Maps.KEY3);


        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                capInfo.setVisibility(View.INVISIBLE);
            }
        });

        detectTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    private void detectTextFromImage() {
        if(imageBitmap!=null){
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
            firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    try {
                        displayTextFromImage(firebaseVisionText);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(TextRecognitionCamera.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(TextRecognitionCamera.this, "Please capture the image first!"  , Toast.LENGTH_SHORT).show();
        }



    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) throws IOException {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();

        if (blockList.size() == 0) {
            Toast.makeText(this, "There is no text to detect", Toast.LENGTH_SHORT).show();
        } else {
            String text = "";
            String info = "";
            String hours = "";
            String restrictions = "";
            String price = "";
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                for (FirebaseVisionText.Line line : block.getLines()) {
                    if (line.getText().contains("MAX") || line.getText().contains("Max")) {
                        if(line.getText().contains("hour")||line.getText().contains("hours")){
                            restrictions += line.getText();
                        }
                    }
                    if (line.getText().contains("mins") || line.getText().contains("Mins")||line.getText().contains("minutes")||line.getText().contains("Minutes")) {
                        price += line.getText();
                    }


                    for (FirebaseVisionText.Element element : line.getElements()) {
                        if (element.getText().contains("00") || element.getText().contains("30") || element.getText().contains("MON")|| element.getText().contains("Mon") ||
                                element.getText().contains("SUN") || element.getText().contains("Sun")|| element.getText().equals("Saturday") || element.getText().equals("SAT")||
                                element.getText().contains(":") || element.getText().contains("00-")) {
                            if(!element.getText().contains("c")&&!element.getText().contains("C")){
                                hours += element.getText() + " ";
                            }
                        } else if (element.getText().contains("C1") || element.getText().contains("C2") || element.getText().contains("C3")) {

                        }

                    }
                }

            }

            hours = hours.replace("0 1", "0-1").replace("0 2", "0-2").replace(" SAT ", "-SAT, ")
                    .replace(" FRI ", "-FRI, ").replace("001", "00-1").replace("002", "00-2")
                    .replace("301", "30-1").replace("302", "30-2");
            try {
                price = price.replace("l", "1").replace("L", "1").replace("c", "").replace("C", "")
                        .replace("-", "").replace("60 mins", "").replace("60mins", "").replace("60 minutes", "")
                        .replace("60minutes", "").replace("E", "").replace("e", "").replace("i", "1")
                        .replace("I", "1").replace("=", "").replace(" ", "");

            } catch (IndexOutOfBoundsException e) {
                Toast.makeText(TextRecognitionCamera.this, "price", Toast.LENGTH_SHORT).show();
            }

            textView.setText("Hours: " + hours + "\n--------\n" + "Restrictions: " + restrictions + "\n--------\n"
                    + "Price: " + price + "\n--------\n" + text);





            if(address == null){
                Intent in = new Intent(TextRecognitionCamera.this, Maps.class);
                Toast.makeText(TextRecognitionCamera.this, "Bad read. Please try again!", Toast.LENGTH_LONG).show();
                startActivity(in);
            }else if(price.length()!=4){
                Toast.makeText(TextRecognitionCamera.this, "Bad read. Please re-capture image!", Toast.LENGTH_LONG).show();
            }else{
                Intent i = new Intent(TextRecognitionCamera.this, CheckNewMeter.class);
                i.putExtra(LOCATION, address);
                i.putExtra(HOURS, hours);
                i.putExtra(PRICE, price);
                i.putExtra(RESTRICTIONS, restrictions);
                i.putExtra(LATITUDE, lat);
                i.putExtra(LONGITUDE, lng);
                startActivity(i);
            }


        }
    }


}
