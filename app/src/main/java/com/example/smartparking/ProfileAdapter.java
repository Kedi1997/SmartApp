package com.example.smartparking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.smartparking.Profile;
import com.example.smartparking.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProfileAdapter extends BaseAdapter {
    private List<Profile> profiles;
    private Context context;

    public ProfileAdapter(){}

    public ProfileAdapter(List<Profile> profiles, Context context){
        this.profiles = profiles;
        this.context = context;
    }

    @Override
    public int getCount() {
        return profiles.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder{
        TextView profile_startTime;
        TextView profile_endTime;
        TextView profile_location;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.profile_list,parent,false);
            holder = new ViewHolder();
            holder.profile_startTime = (TextView) convertView.findViewById(R.id.profile_startTime);
            holder.profile_endTime = (TextView) convertView.findViewById(R.id.profile_endTime);
            holder.profile_location = (TextView) convertView.findViewById(R.id.profile_location);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Timestamp startTime = profiles.get(position).getStartTime();
        holder.profile_startTime.setText(dateFormat.format(startTime.toDate()));


        Timestamp endTime = profiles.get(position).getEndTime();
        holder.profile_endTime.setText(dateFormat.format(endTime.toDate()));

        holder.profile_location.setText(profiles.get(position).getLocation());
        return convertView;
    }

}
