package com.example.smartparking;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


import android.app.Activity;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.FrameLayout;
import android.widget.Toast;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import android.app.FragmentManager;



public class MainActivity extends AppCompatActivity {


    private Map<Integer,String> bayAddressMap= new HashMap<Integer,String>();

    private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    private ListFragment listFragment;
    private MapFragment mapFragment;
    private FirebaseAuth mAuth;


    private static final String TAG = "MyActivity";
    DatabaseHelper myDb;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainFrame = (FrameLayout) findViewById(R.id.main_frame);
        listFragment = new ListFragment();
        mapFragment = new MapFragment();
        mAuth = FirebaseAuth.getInstance();

        setBottomNavigation();
        readBayAddressDate();

        setFragment(listFragment);


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onResumeFragments();
    }



    public static Geocoder getGeocoder(final FragmentActivity activity){
        Geocoder geocoder=new Geocoder(activity);
        return geocoder;
    }

    private void readBayAddressDate(){
        InputStream is = getResources().openRawResource(R.raw.bay_address);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line;
        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                bayAddressMap.put(Integer.parseInt(tokens[0]),tokens[1]);
            }
        }catch(IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_center, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.user_profile:
                Log.d(TAG,"page jumps to Profile");
                Intent intent_profile = new Intent(MainActivity.this ,ProfileActivity.class);
                item.setIntent(intent_profile);
                startActivity(intent_profile);
                return true;
            case R.id.find_my_car:
                Log.d(TAG,"page jumps to find_my_car");
                Intent intent_find = new Intent(MainActivity.this ,FindMyCarActivity.class);
                item.setIntent(intent_find);
                startActivity(intent_find);
                return true;
            case R.id.information:
                Log.d(TAG,"page jumps to information");
                Intent intent_info = new Intent(MainActivity.this ,InformationActivity.class);
                item.setIntent(intent_info);
                startActivity(intent_info);
                return true;
            case R.id.timer:
                Log.d(TAG,"page jumps to timer");
                Intent intent_timer = new Intent(MainActivity.this ,TimerActivity.class);
                item.setIntent(intent_timer);
                startActivity(intent_timer);
                return true;
            case R.id.login:
                Log.d(TAG,"page jumps to login");
                Intent intent_login = new Intent(MainActivity.this , LoginActivity.class);
                item.setIntent(intent_login);
                startActivity(intent_login);
                return true;
            case R.id.logout:
                Log.d(TAG,"user logout");
                Intent intent_logout = new Intent(MainActivity.this , LogoutActivity.class);
                item.setIntent(intent_logout);
                startActivity(intent_logout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(mAuth.getCurrentUser()==null){
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.login).setVisible(true);
        }else {
            menu.findItem(R.id.logout).setVisible(true);
            menu.findItem(R.id.login).setVisible(false);
        }

        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                } catch (Exception e) {
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    private void setBottomNavigation(){
        mMainNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mMainNav.setSelectedItemId(R.id.nav_list);
        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_map:
                        setFragment(mapFragment);
                        return true;

                    case R.id.nav_list:
                        setFragment(listFragment);
                        return true;

                    default:
                        setFragment(listFragment);
                        return false;
                }
            }
        });
    }
    private void setFragment(Fragment fragment) {
        androidx.fragment.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame,fragment);
        fragmentTransaction.commit();
    }


}