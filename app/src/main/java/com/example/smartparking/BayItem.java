package com.example.smartparking;

import androidx.annotation.Nullable;

public class BayItem implements Comparable{
    private String address;
    private String distance;
    private int bay_id;
    private double lat;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    private double lon;


    public int getBay_id() {
        return bay_id;
    }

    public void setBay_id(int bay_id) {
        this.bay_id = bay_id;
    }

    @Override
    public String toString() {
        return "BayItem{" +
                "address='" + address + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }




    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }



    @Override
    public int compareTo(Object o) {
        BayItem ot = (BayItem) o;
        if (Double.parseDouble(this.distance) > Double.parseDouble(ot.distance)) return 1;
        if (Double.parseDouble(this.distance) < Double.parseDouble(ot.distance)) return -1;
        else return 0;
    }
}
