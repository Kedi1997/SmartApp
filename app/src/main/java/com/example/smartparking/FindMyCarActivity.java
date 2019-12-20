package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

public class FindMyCarActivity extends AppCompatActivity  implements OnMapReadyCallback,
        TaskLoadedCallback{

    private FirebaseAuth mAuth;

    private static final String TAG = "FindMyCarActivity";
    private static final String FINE_LOCATION= Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION= Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE =1234;
    public static final float DEFAULT_ZOOM=15.5f;
    private static final String apiKey ="AIzaSyAj_20wVrCc4Vl67eUC9MDJ5R1GTCXLnuU";
    private Polyline currentPolyline;
    private LatLng start, end;

    //widgets
    private ImageView mGps;
    private LatLng place1, place2;
    private Button button;
    private EditText editText;


    //vars
    private Boolean mLocationPermissionsGranted=false;
    private GoogleMap gMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private BayItem item;
    private LatLng latLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataAccess.getMapPermissions(FindMyCarActivity.this);
        setContentView(R.layout.activity_find_my_car);
        getSupportActionBar().setTitle("Find My Car");

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            Places.initialize(getApplicationContext(), apiKey);
            // Create a new Places client instance
            PlacesClient placesClient = Places.createClient(this);
            // do find my car logic here
            button=(Button)findViewById(R.id.get_direction);
            editText=(EditText)findViewById(R.id.text_location_place_input);
            String str = getSavedSharedPreferences();
            Geocoder geocoder=new Geocoder(this);
            List<Address> addresseList =null;
            if(str!=null || !str.equals("") ) {
                try {
                    addresseList = geocoder.getFromLocationName(str, 1);
                    Address address = addresseList.get(0);
                    latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    //get the loaction here could just try it

                } catch (Exception e) {
                    Log.d(TAG, "onCreate: something wrong? sorry, cannot help" );
                }
            }
            initMap();
            Location current= DataAccess.getLocation(this);
            place1=new LatLng(current.getLatitude(),current.getLongitude());
//            place1=new LatLng(-37.8143960, 144.9463973);
            place2 = latLng;
//            place2=new LatLng(-37.8052018,144.9634263);

        } else {
            // jumps to login page
            Toast.makeText(FindMyCarActivity.this,R.string.not_login_remind, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FindMyCarActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = gMap.addPolyline((PolylineOptions) values[0]);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this,"Map is Ready", Toast.LENGTH_SHORT).show();
        gMap=googleMap;
        moveCamera(place1, DEFAULT_ZOOM,"start");
        drawLine(place1, place2);
        getDirection();
    }

    private void moveCamera(LatLng latLng, float zoom, String name) {
        Log.d(TAG, "moveCamera: moving the camera to : lat: " + latLng.latitude + "long: " + latLng.longitude);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        gMap.addMarker(new MarkerOptions().position(latLng).title(name));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(FindMyCarActivity.this);
    }


    private void drawLine(LatLng current, LatLng distination){
        gMap.addMarker(new MarkerOptions().position(current).title("Start"));
        gMap.addMarker(new MarkerOptions().position(distination).title("End"));
        new FetchURL(FindMyCarActivity.this).execute(getUrl(current, distination, "Walking"), "Walking");
    }
    //method 2
    private String getUrl(LatLng origin, LatLng dest, String directionMode){
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.map_api_key);
        return url;
    }
    public String getSavedSharedPreferences(){
        //Get all the Shared Preference Values

        String location ="Melbourne Central";

        try{
            SharedPreferences sharedPref = getSharedPreferences("parkinginfosharedpref", MODE_PRIVATE);
            location = sharedPref.getString("locationString","Melbourne central");

        } catch (Exception e){
        }
        return location;
    }

    private void getDirection(){
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse(String.format(Locale.US,"google.navigation:q=%f,%f&mode=d", place2.latitude,place2.longitude));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }

        });
    }
}
