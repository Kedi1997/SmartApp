package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class InformationActivity extends AppCompatActivity {

    private static final int REQUEST_PHONE_CALL = 1;
    private List<InfoItem> infoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        getSupportActionBar().setTitle("Information");

        Button phoneCall = findViewById(R.id.phone_call);
        Button findOutMore = findViewById(R.id.find_out_more);

        findOutMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://www.melbourne.vic.gov.au/parking-and-transport/parking/Pages/parking-faqs.aspx");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        phoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + R.string.phone_call_number));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (ContextCompat.checkSelfPermission(InformationActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(InformationActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                }
                else
                {
                    Log.d("InformationActivity","calling");
                    startActivity(intent);
                }

            }
        });

        initData();

        ExpandableListView listview = ((ExpandableListView) findViewById(R.id.expendablelistview));

        MyExpandableListViewAdapter adapter = new MyExpandableListViewAdapter(infoItems,this);

        listview.setAdapter(adapter);
    }

    private void initData() {
        infoItems = new ArrayList<>();

        InfoItem info1 = new InfoItem();
        info1.name = "How much does it cost to park in the city?";
        List<String> details1 = new ArrayList<>();
        details1.add("In the central city the rate for short-term on-street parking is $7 per hour. Half hour parking is $3.50. The table summarises on-street parking rates which are shown on parking meters and ticket machines.");
        info1.details = details1;

        InfoItem info2 = new InfoItem();
        info2.name = "When does my parking time start?";
        List<String> details2 = new ArrayList<>();
        details2.add("Your parking time starts as soon as your car stops in the bay, whether or not you remain in the vehicle (for example, if you choose to finish a phone call before paying for parking and leaving your car). Your parking time does not start when you pay the parking fee.");
        info2.details = details2;

        InfoItem info3 = new InfoItem();
        info3.name = "Do time restrictions apply on public holidays?";
        List<String> details3 = new ArrayList<>();
        details3.add("Some parking restrictions are different on public holidays. Read more information and examples about parking on public holidays.");
        info3.details = details3;

        InfoItem info4 = new InfoItem();
        info4.name = "How much are parking fines?";
        List<String> details4 = new ArrayList<>();
        details4.add("\u200BFor the 2019-20 financial year, fines range from $83 to $165 depending on the offence.");
        info4.details = details4;


        infoItems.add(info1);
        infoItems.add(info2);
        infoItems.add(info3);
        infoItems.add(info4);


    }


}
