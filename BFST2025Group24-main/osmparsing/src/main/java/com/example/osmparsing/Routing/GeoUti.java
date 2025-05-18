package com.example.osmparsing.Routing;

import com.example.osmparsing.Routing.RoutingAlgorithms.HaversineDistance;

public class GeoUti {
    private GeoUti() {}

    public static double haversineNodes(int idxA, int idxB, double[] lat, double[] lng) {
        return HaversineDistance.haversine(lat[idxA], lng[idxA], lat[idxB], lng[idxB]);
    }
}
