package com.example.osmparsing.wayEnums;

import javafx.scene.paint.Color;

public enum HighwayType {
    TRUNK("trunk", 7e-5, Color.ORANGERED, 80),
    MOTORWAY("motorway", 7e-5, Color.RED, 90),
    PRIMARY("primary",5e-5, Color.web("#fcd6a4"), 50),
    SECONDARY("secondary",4e-5, Color.web("#f7fabf"), 50),
    TERTIARY("tertiary",3e-5, Color.BLACK, 50),
    FOOTWAY("footway",2e-5, Color.BLACK, 5),
    PATH("path",2e-5, Color.BLUE, 5),
    RESIDENTIAL("residential",2e-5, Color.BLACK, 50),
    PEDESTRIAN("pedestrian",2e-5, Color.BLACK, 5),
    PROPOSED("proposed",2e-5, Color.TRANSPARENT, 0),
    UNCLASSIFIED("unclassified",2e-5,Color.BLACK, 50);

    private final String value;
    private double lineWidth;
    private Color color;
    private final int defaultSpeed;


    HighwayType(String value, double lineWidth, Color color, int defaultSpeed) {
        this.value = value;
        this.lineWidth = lineWidth;
        this.color = color;
        this.defaultSpeed = defaultSpeed;
    }

    public String getType() { return value; }
    public double getLineWidth() { return lineWidth; }
    public Color getColor() { return color; }
    public int getDefaultSpeed() { return defaultSpeed; }
    public double getDefaultSpeedMps() { return defaultSpeed / 3.6; }

    public static HighwayType fromValue(String value) {
        return switch (value) {
            case "motorway" -> MOTORWAY;
            case "motorway_link" -> MOTORWAY;
            case "motorway_junction" -> MOTORWAY;

            case "trunk" -> TRUNK;
            case "trunk_link" -> TRUNK;
            case "primary" -> PRIMARY;
            case "secondary" -> SECONDARY;
            case "tertiary" -> TERTIARY;
            case "footway" -> FOOTWAY;
            case "path" -> PATH;
            case "residential" -> RESIDENTIAL;
            case "pedestrian" -> PEDESTRIAN;

            case "proposed" -> PROPOSED;
            default -> UNCLASSIFIED;
        };
    }

    public void setColor(Color color){
        this.color = color;
    }

    public static void updateColorsForMode(String mode){
        switch(mode){
            case "Protanopia":
                PRIMARY.setColor(Color.web("#EBC08B"));
                SECONDARY.setColor(Color.web("#E3E6A8"));
                break;

            case "Deuteranopia":
                PRIMARY.setColor(Color.web("#E5C296"));
                SECONDARY.setColor(Color.web("#DADCB3"));
                break;

            case "Tritanopia":
                PRIMARY.setColor(Color.web("#EBC29D"));
                SECONDARY.setColor(Color.web("#E3E3A3"));
                break;

            case "Default":
                PRIMARY.setColor(Color.web("#ff0000"));
                SECONDARY.setColor(Color.web("#f7fabf"));
                break;

            default:
                System.out.println("Unknown color mode: " + mode);
                return;
        }

        TERTIARY.setColor(Color.BLACK);
        FOOTWAY.setColor(Color.BLACK);
        RESIDENTIAL.setColor(Color.BLACK);
        PEDESTRIAN.setColor(Color.BLACK);
        UNCLASSIFIED.setColor(Color.BLACK);
    }
}
