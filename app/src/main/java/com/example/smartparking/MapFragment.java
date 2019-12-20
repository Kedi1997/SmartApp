package com.example.smartparking;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {
//    implements GoogleMap.OnMarkerClickListener

    private MapView mMapView;
    private GoogleMap googleMap;
    private static final float DEFAULT_ZOOM = 17;
    private static final String TAG = "MapFragment";
    private SearchView searchView;
    private Geocoder geocoder;
    private static LatLng latLng;

    private ArrayList<BayItem> addresses;

    public static LatLng getLatLng() {
        return latLng;
    }

//    private List<Marker> markers;

    private BayItem item;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataAccess.getMapPermissions(getActivity());
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        addresses = new ArrayList<BayItem>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        searchView=(SearchView) rootView.findViewById(R.id.search_bar);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        addresses = DataAccess.getAddresses();
        try {
            final float lat = getArguments().getFloat("lat");
            final float lon = getArguments().getFloat("lon");

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    googleMap.setMyLocationEnabled(true);
                    LatLng location = new LatLng(lat, lon);
                    googleMap.addMarker(new MarkerOptions().position(location)
                            .title("Parking"));
                    moveCamera(location,DEFAULT_ZOOM);
                }
            });
        }
        catch (Exception e) {

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;
                    try {
                        googleMap.setMyLocationEnabled(true);
                        addresses = DataAccess.getAddressesShow();
                        for (BayItem item : addresses
                        ) {
                            LatLng loc = new LatLng(item.getLat(), item.getLon());
                            googleMap.addMarker(new MarkerOptions().position(loc)
                                    .title("Parking"));
                        }
                        Location currentLocation = DataAccess.getLocation(getActivity());
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                DEFAULT_ZOOM-1);
                        searchLocation();
                    } catch (Exception e) {
                        Log.d("Map", "Got exception");
                    }
                }
            });
        }


        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        try {
            // For showing a move to my location button
            googleMap.setMyLocationEnabled(true);
            addresses = DataAccess.getAddressesShow();
            for (BayItem item : addresses
            ) {
                LatLng loc = new LatLng(item.getLat(), item.getLon());
                googleMap.addMarker(new MarkerOptions().position(loc)
                        .title("Parking"));
            }
            Location currentLocation = DataAccess.getLocation(getActivity());
            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),
                    DEFAULT_ZOOM-1);
            searchLocation();
        }
        catch (Exception e){
            searchLocation();
            Log.d("Map", "Got exception");
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d("MAP", "moveCamera: moving the camera to : lat: "+ latLng.latitude+"long: "+latLng.longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

    public void searchLocation(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location=searchView.getQuery().toString();
                Log.d(TAG, "onQueryTextSubmit: 地址收到了么 "+ location);
                List<Address> addresseList =null;
                if(location!=null || !location.equals("") ){
                    Log.d(TAG, "onQueryTextSubmit:  地址是什么"+location);
                    try {
                        addresseList=geocoder.getFromLocationName(location,1);
                        Address address=addresseList.get(0);
                        latLng=new LatLng(address.getLatitude(),address.getLongitude());
                        //get the loaction here could just try it
                        moveCamera(latLng, DEFAULT_ZOOM);
                        setMarks(latLng);
                    } catch (Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "No Location Found",
                                Toast.LENGTH_SHORT).show();
                    }

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setMarks(LatLng latLng) throws InterruptedException {

        List<BayItem> bayItems=DataAccess.getNearestBaysTo(latLng.latitude,latLng.longitude,5);
        Log.d(TAG, "setMarks: 有多少个marker？？" + bayItems.size());
        LatLng l =new LatLng(bayItems.get(0).getLat(), bayItems.get(0).getLon());
        googleMap.addMarker(new MarkerOptions().position(l));
        //调用dataaccess的方法，返回一个地址
        for(BayItem bay:bayItems){
            Log.d(TAG, "setMarks: 开始标了");
            LatLng la =new LatLng(bay.getLat(),bay.getLon());
            Log.d(TAG, "setMarks: 地标都有哪些？" + la);
            googleMap.addMarker(new MarkerOptions().position(la));
        }
    }

//    @Override
//    public boolean onMarkerClick(Marker marker){
//        for(Marker m: markers){
//            if(marker.equals(m)){
//                //this (marker.getPosition().latitude,marker.getPosition().longitude) will be the locaiton of the user's choose
//                Uri gmmIntentUri = Uri.parse(String.format(Locale.US,"google.navigation:q=%f,%f&mode=d",marker.getPosition().latitude,marker.getPosition().longitude));
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                mapIntent.setPackage("com.google.android.apps.maps");
//                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                    startActivity(mapIntent);
//                }
//                Log.w("Click", "test");
//                return true;
//            }
//        }
//        return false;
//    }

}
