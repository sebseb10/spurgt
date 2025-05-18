package com.example.osmparsing;
import java.io.Serializable;
import java.util.*;

public class POIController implements Serializable {
    private List<POI> poiList;

    public POIController() {
        this.poiList = new ArrayList<>();
    }

    public void addPOI(POI poi) {
        poiList.add(poi);
    }

    public List<POI> getAllPOIs() {
        return poiList;
    }
}