package com.example.monitorme.Object;

import java.io.Serializable;

/// not actually in use
// ws made for an idea but wasnt capable of running it
// will be implemented after college

public class MapsObject implements Serializable {

    private String  uid;
    public double latitude;
    public double longitude;

    public MapsObject(double longitude, double latitude, String uid) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
