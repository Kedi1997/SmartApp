package com.example.smartparking;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SmartParking.db";
    private static final String TABLE_NAME = "bayAddress";
    private static final String COL_1 = "bayID";
    private static final String COL_2 = "lat";
    private static final String COL_3 = "lon";
    private static final String COL_4 = "address";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 2);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (bayID INTEGER PRIMARY KEY,lat,lon,address)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(int bayID,double lat, double lon, String address){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,bayID);
        contentValues.put(COL_2,lat);
        contentValues.put(COL_3,lon);
        contentValues.put(COL_4,address);
        long result = db.insert(TABLE_NAME,null,contentValues);
        if (result == -1){
            return false;
        }
        else{
            return true;
        }


    }
}