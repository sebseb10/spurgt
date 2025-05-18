package com.example.osmparsing;

import java.io.Serializable;

public class POI implements Serializable {
    double lat;
    double lon;


    public POI(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat(){
        return lat;
    }

    public double getLon(){
        return lon;
    }
}