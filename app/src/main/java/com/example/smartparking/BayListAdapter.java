package com.example.smartparking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class BayListAdapter extends ArrayAdapter<BayItem> {
    public BayListAdapter(@NonNull Context context, ArrayList<BayItem> fruits) {
        super(context,R.layout.parking_list_item ,fruits);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myInflater = LayoutInflater.from(getContext());
        View customView = myInflater.inflate(R.layout.parking_list_item,parent,false);
        BayItem singlefruitItem = getItem(position);
        TextView location = (TextView) customView.findViewById(R.id.location);
        ImageView distanceImg = (ImageView) customView.findViewById(R.id.distanceImage);
        distanceImg.setImageResource(R.drawable.distance_icon_png);
        TextView distanceTxt = (TextView) customView.findViewById(R.id.distanceText);
        location.setText(singlefruitItem.getAddress());

        distanceTxt.setText(singlefruitItem.getDistance()+"\nmeters");
        return customView;
    }
}
