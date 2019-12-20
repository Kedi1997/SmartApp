package com.example.smartparking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFragment extends Fragment {
    private ArrayList<BayItem> addresses;
    private ArrayList<BayItem> addressesShow;
    public static BayListAdapter getBayListAdapter() {
        return bayListAdapter;
    }
    private Geocoder geocoder;
    private boolean flag_loading = false;
    private static BayListAdapter bayListAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addresses = DataAccess.getAddresses();
        addressesShow = DataAccess.getAddressesShow();
        // Inflate the layout for this fragment
        bayListAdapter = new BayListAdapter(this.getContext(), addressesShow);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout view = (FrameLayout) inflater.inflate(R.layout.fragment_list, container, false);
        ListView bayListView = (ListView)view.findViewById(R.id.bayList);
        bayListView.setAdapter(bayListAdapter );
        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataAccess.updateData(getActivity(),false); // your code
                pullToRefresh.setRefreshing(false);
            }
        });
        DataAccess.updateData(getActivity(),false);

        bayListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.d("get Address","dddd");
                if (firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)     {

                    if(flag_loading == false)
                    {   Log.d("get Address","aaa");
                        Log.d("get Address",Integer.toString(addresses.size()));
                        flag_loading = true;
                        addresses = (ArrayList<BayItem>) addresses.clone();

                        for(int i = 0; i < 5 && addresses.size()>5; i++){
                            Log.d("get Address",Integer.toString(i));

                            try {
                                String address = geocoder.getFromLocation(addresses.get(20).getLat(),addresses.get(20).getLon() , 1).get(0).getAddressLine(0).replace("AustraliaVictoriaMelbourne","").replace("AustraliaVictoriaPort Phillip - West","");
                                if(address.equals("")){
                                    addresses.get(20).setAddress("unknown");
                                }
                                else{addresses.get(20).setAddress(address);}

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            addressesShow.add(addresses.get(20));
                            addresses.remove(20);

                        }
                        bayListAdapter.notifyDataSetChanged();
                        flag_loading = false;
                    }

                }


            }


        });

        bayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BayItem item = addresses.get(position);

                //Included for Alarm/Profile Activity
                String tempLocation = item.getAddress();
                saveLocationSharedInfo(tempLocation);
                //---Included for Alarm/Profile Activity


                String[] options = {"Show in Map", "Get Directions"};
                // 1. Instantiate an AlertDialog Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Pick an Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Log.d("ListFragment", "Clicked on Show in Map");
                                Bundle bundle = new Bundle();
                                bundle.putFloat("lat", (float)item.getLat());
                                bundle.putFloat("lon", (float)item.getLon());
                                MapFragment map_loc = new MapFragment();
                                map_loc.setArguments(bundle);
                                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                                fragmentTransaction.replace(R.id.main_frame, map_loc);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                                break;
                            case 1:
                                Log.d("ListFragment", item.toString());
                                Uri gmmIntentUri = Uri.parse(String.format(Locale.US,"google.navigation:q=%f,%f&mode=d",item.getLat(),item.getLon()));
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");

                                if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(mapIntent);
                                }

                                break;
                            default:
                                Log.d("ListFragment", "Broken");
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return view;
    }


    //Included for Alarm/Profile activity
    public void saveLocationSharedInfo(String locationtemp){

        Log.d("GAN_DBG","Save Location Shared info entered. Location: "+ locationtemp);
        //Shared pref name is "locationstring"
        SharedPreferences sharedPrefLocation =
                getActivity().getSharedPreferences("locationString",MODE_PRIVATE);
        SharedPreferences.Editor spEdit = sharedPrefLocation.edit();

        //Shared pref variable name is also "location"
        //Save a default Location
        spEdit.putString("locationString",locationtemp);

        //Complete Save the information
        spEdit.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
