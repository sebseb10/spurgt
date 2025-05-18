package com.example.osmparsing;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;

public class Address implements Serializable {
    String houseNo;
    String street;
    String city;
    //String municipality;
    String postalCode;
    long id;
    double lon;
    double lat;

    public Address(String street, String houseNo, String city, String postalCode, long id, double lat, double lon) {
        this.houseNo = houseNo;
        this.street = street;
        this.city = city;
        //this.municipality = municipality;
        this.postalCode = postalCode;
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public String getAddress() {
        return street + " " + houseNo + " " + city + " " + postalCode;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public long getId() { return id; }

    public void drawPin(GraphicsContext gc, Color color, double x, double y) {
        gc.setStroke(color);
        gc.setFill(color);
        gc.fillOval(x, y, 1e-4, 1e-4);
    }




}
