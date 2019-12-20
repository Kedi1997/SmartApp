package com.example.smartparking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class DataAccess {

    private static final String ENDPOINT = "https://data.melbourne.vic.gov.au/resource/vh2v-4nfs.json?$limit=10000";
    private static RequestQueue queue;
    private static Geocoder geocoder;
    private int mapPermissions = 0;

    public static ArrayList<BayItem> getAddresses() {
        /*Get current version of bay information
        * Every item of the return list contains bay_id,
        * latitude, longitude, and human-readable address*/
        return addresses;
    }

    public static ArrayList<BayItem> getAddressesShow() {
        /*Get bay information that are shown on the list*/
        return addressesShow;
    }

    private static ArrayList<BayItem> addresses = new ArrayList<>();;
    private static ArrayList<BayItem>  addressesShow = new ArrayList<>();;
    @RequiresApi(api = Build.VERSION_CODES.M)

    public static void updateData(final FragmentActivity activity, final boolean map){
        /*Fetch the newest data from endpoint and update list*/
        addresses.clear();
        addressesShow.clear();
        geocoder = new Geocoder(activity, Locale.getDefault());
        queue = Volley.newRequestQueue(activity);

        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, ENDPOINT, null,
                new Response.Listener<JSONArray>() {

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onResponse(JSONArray response) {
                        // display response
                        Location location = getLocation(activity);
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        Log.d("Your Location: ",Double.toString(longitude) + "  " + Double.toString(latitude));
                        Location startPoint = new Location("locationA");
                        if (MapFragment.getLatLng() != null) {
                            startPoint.setLatitude(MapFragment.getLatLng().latitude);
                            startPoint.setLongitude(MapFragment.getLatLng().longitude);
                        }
                        else {
                            startPoint.setLatitude(latitude);
                            startPoint.setLongitude(longitude);
                        }

                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject bay = response.getJSONObject(i);
                                if (bay.getString("status").equals("Unoccupied")) {

                                    JSONObject GPSJSON = bay.getJSONObject("location");

                                    Location endPoint = new Location("locationA");
                                    endPoint.setLatitude(GPSJSON.getDouble("latitude"));
                                    endPoint.setLongitude(GPSJSON.getDouble("longitude"));

                                    String distance = String.format("%.2f",startPoint.distanceTo(endPoint));

                                    BayItem bayItem = new BayItem();
                                    bayItem.setBay_id(Integer.parseInt(bay.getString("bay_id")));
                                    bayItem.setDistance(distance);
                                    bayItem.setLat(GPSJSON.getDouble("latitude"));
                                    bayItem.setLon(GPSJSON.getDouble("longitude"));
                                    addresses.add(bayItem);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Collections.sort(addresses);

                        for(int i = 0; i < 20; i++) {
                            try {
                                String address = geocoder.getFromLocation(addresses.get(i).getLat(),addresses.get(i).getLon() , 1).get(0).getAddressLine(0).replace("AustraliaVictoriaMelbourne","").replace("AustraliaVictoriaPort Phillip - West","");
                                if(address.equals("")){
                                    addresses.get(i).setAddress("unknown");
                                }
                                else{addresses.get(i).setAddress(address);}
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            addressesShow.add(addresses.get(i));
                        }

                        if (!map) {

                            ListFragment.getBayListAdapter().notifyDataSetChanged();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        queue.add(getRequest);
        Log.d("Response", Boolean.toString(addresses.isEmpty()));

    }

    public static  ArrayList<BayItem> getNearestBaysTo(Double latitude,Double longitude, int num){

        ArrayList<BayItem> NearestBays = (ArrayList<BayItem>) addresses.clone();

        Location startPoint = new Location("locationA");
        startPoint.setLatitude(latitude);
        startPoint.setLongitude(longitude);
        Location endPoint = new Location("locationA");

        for(int i = 0; i <  NearestBays.size(); i++){

            endPoint.setLatitude( NearestBays.get(i).getLat());
            endPoint.setLongitude( NearestBays.get(i).getLon());
            String distance = Double.toString(startPoint.distanceTo(endPoint));
            NearestBays.get(i).setDistance(distance);
        }

        Collections.sort(NearestBays);
        ArrayList<BayItem> result = new ArrayList<>();
        for (int i = 0; i < num; i++){
            result.add(NearestBays.get(i));
        }

        return result;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Location getLocation(FragmentActivity activity) {
        try {
            LocationManager locationManager = (LocationManager) activity
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {

                if (isNetworkEnabled) {
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    Activity#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for Activity#requestPermissions for more details.
                            return null;
                        }
                        Location location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            return location;
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (true) {

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            Location location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                return location;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return  null;
    }

    public static void getMapPermissions(final FragmentActivity activity){

        if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
    }

}
