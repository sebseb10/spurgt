package com.example.osmparsing.Routing.RoutingAlgorithms;

import com.example.osmparsing.Node;

/**
 * This Haversine formula has been taken from: <a href="https://rosettacode.org/wiki/Haversine_formula#Java">...</a>
 */
public class HaversineDistance {
    public static final double R = 6372.8; // In kilometers

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
    public static double haversine(Node a, Node b) {
        return haversine(a.getLat(), a.getLon(), b.getLat(), b.getLon());
    }
    public static void main(String[] args) {
        System.out.println(haversine(36.12, -86.67, 33.94, -118.40));
    }
}