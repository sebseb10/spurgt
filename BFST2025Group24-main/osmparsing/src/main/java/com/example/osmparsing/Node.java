package com.example.osmparsing;

import com.example.osmparsing.kdTree.KdPoint2D;
import com.example.osmparsing.kdTree.RectHV;
import javafx.scene.canvas.GraphicsContext;
import java.io.Serializable;

public class Node implements Serializable {
    long id;
    double lat, lon;
    public KdPoint2D p;
    public Node left, right;
    public RectHV rect;

    /// har fjernet extends OSMElement for så at have 2 constructors,
    /// tilføjede så også felted long id og this.id.
    public Node(long id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.p = new KdPoint2D(lat, lon);
    }
    /**
     * Hvis muligt, vil rigtigt meget gerne fjerne Node fra OSMElement,
     * fordi vil gerne gøre så jeg kan have forskellige argumenter til constructor.
     * @this.lat
     * @this.lon
     */
    public Node(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.p = new KdPoint2D(lat, lon);
    }

    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public long getId() {return id;}

    public double distanceTo(Node other) {
        final int R = 6371000; // Earth's radius in meters
        double lat1 = Math.toRadians(this.lat);
        double lat2 = Math.toRadians(other.lat);
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(other.lon - this.lon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /*
    public double distanceTo(Node other) {
        long otherId = other.getId();
        double otherLon = other.getLon();
        double otherLat = other.getLat();
        return HaversineDistance.haversine(lat, lon, otherLat, otherLon);
    }

     */
}