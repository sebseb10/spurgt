package com.example.osmparsing.wayEnums;

public enum WayType {
    HIGHWAY, BUILDING, NATURAL, LANDUSE, UNCLASSIFIED, AMENITY, COASTLINE;

    public static WayType fromKey(String key) {
        return switch (key) {
            case "highway" -> HIGHWAY;
            case "building" -> BUILDING;
            case "natural" -> NATURAL;
            case "landuse" -> LANDUSE;
            case "amenity" -> AMENITY;
            default -> UNCLASSIFIED;
        };
    }
}
