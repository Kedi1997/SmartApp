package com.example.smartparking;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

public class MyExpandableListViewAdapter extends BaseExpandableListAdapter {
    public List<InfoItem> infoItems;
    private Activity activity;

    public MyExpandableListViewAdapter(List<InfoItem> infoItems, Activity activity){
        this.infoItems = infoItems;
        this.activity = activity;
    }

    @Override
    public int getGroupCount() {
        return infoItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return infoItems.get(groupPosition).details.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return infoItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return infoItems.get(groupPosition).details.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {


        return getGenericView(infoItems.get(groupPosition).name);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        return getGenericView(infoItems.get(groupPosition).details.get(childPosition));
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private TextView getGenericView(String string) {

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView textView = new TextView(activity);
        textView.setLayoutParams(layoutParams);

        textView.setGravity(Gravity.CENTER | Gravity.CENTER);

        textView.setPadding(40, 20, 0, 20);
        textView.setText(string);
        textView.setTextColor(Color.BLACK);
        return textView;
    }
}
