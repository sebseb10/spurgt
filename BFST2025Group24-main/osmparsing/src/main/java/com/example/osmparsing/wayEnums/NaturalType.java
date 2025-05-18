package com.example.osmparsing.wayEnums;

import javafx.scene.paint.Color;

public enum NaturalType {
    COASTLINE("coastline", 1e-5, Color.BLACK),
    WATER("water", 1e-4, Color.web("#B6D9E8")),
    TREE("tree", 1e-4, Color.web("#B9D3A0")),
    GRASSLAND("grassland", 1e-4, Color.web("#D6F5B0")),
    HEATH("heath", 1e-4, Color.web("#E2E2A0")),
    SCRUB("scrub", 1e-4, Color.web("#B6D9E8")),
    TREE_ROW("tree_row", 1e-4, Color.web("#B9D3A0")),
    WOOD("wood", 1e-4, Color.web("#D6F5B0")),
    BEACH("beach", 1e-4, Color.web("#B6D9E8")),
    SHOAL("shoal", 1e-4, Color.web("#D6F5B0")),
    GLACIER("glacier", 1e-4, Color.web("#B6D9E8")),
    SAND("sand", 1e-4, Color.web("#B6D9E8")),
    UNCLASSIFIED("unclassified", 1e-4, Color.TRANSPARENT),;

    private final String value;
    private double lineWidth;
    private Color color;

    NaturalType(String value, double lineWidth, Color color) {
        this.value = value;
        this.lineWidth = lineWidth;
        this.color = color;
    }

    public String getType() { return value; }
    public double getLineWidth() { return lineWidth; }
    public Color getColor() { return color; }

    public static NaturalType fromValue(String value) {
        return switch (value) {
            case "coastline" -> COASTLINE;
            case "water" -> WATER;
            case "tree" -> TREE;
            case "grassland" -> GRASSLAND;
            case "heath" -> HEATH;
            case "scrub" -> SCRUB;
            case "tree_row" -> TREE_ROW;
            case "wood" -> WOOD;
            case "beach" -> BEACH;
            case "shoal" -> SHOAL;
            case "glacier" -> GLACIER;
            case "sand" -> SAND;
            default -> UNCLASSIFIED;
        };
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public static void updateColorsForMode(String color){
        switch(color){
            case "Protanopia":
                COASTLINE.setColor(Color.BLACK);
                WATER.setColor(Color.web("#A7CFE3"));
                TREE.setColor(Color.web("#A5C08B"));
                GRASSLAND.setColor(Color.web("#BEDC96"));
                HEATH.setColor(Color.web("#D4D492"));
                SCRUB.setColor(Color.web("#A7CFE3"));
                TREE_ROW.setColor(Color.web("#A5C08B"));
                WOOD.setColor(Color.web("#BEDC96"));
                BEACH.setColor(Color.web("#A7CFE3"));
                SHOAL.setColor(Color.web("#BEDC96"));
                GLACIER.setColor(Color.web("#A7CFE3"));
                SAND.setColor(Color.web("#A7CFE3"));
                UNCLASSIFIED.setColor(Color.TRANSPARENT);
                break;

            case "Deuteranopia":
                COASTLINE.setColor(Color.BLACK);
                WATER.setColor(Color.web("#A7CFE3"));
                TREE.setColor(Color.web("#B5B57F"));
                GRASSLAND.setColor(Color.web("#C6D89F"));
                HEATH.setColor(Color.web("#C9C97F"));
                SCRUB.setColor(Color.web("#A7CFE3"));
                TREE_ROW.setColor(Color.web("#B5B57F"));
                WOOD.setColor(Color.web("#C6D89F"));
                BEACH.setColor(Color.web("#A7CFE3"));
                SHOAL.setColor(Color.web("#C6D89F"));
                GLACIER.setColor(Color.web("#A7CFE3"));
                SAND.setColor(Color.web("#A7CFE3"));
                UNCLASSIFIED.setColor(Color.TRANSPARENT);
                break;

            case "Tritanopia":
                COASTLINE.setColor(Color.BLACK);
                WATER.setColor(Color.web("#C3C3C3"));
                TREE.setColor(Color.web("#BFBFA0"));
                GRASSLAND.setColor(Color.web("#E2E2B0"));
                HEATH.setColor(Color.web("#D4D48F"));
                SCRUB.setColor(Color.web("#C3C3C3"));
                TREE_ROW.setColor(Color.web("#BFBFA0"));
                WOOD.setColor(Color.web("#E2E2B0"));
                BEACH.setColor(Color.web("#C3C3C3"));
                SHOAL.setColor(Color.web("#E2E2B0"));
                GLACIER.setColor(Color.web("#C3C3C3"));
                SAND.setColor(Color.web("#C3C3C3"));
                UNCLASSIFIED.setColor(Color.TRANSPARENT);
                break;

            default:
        }
    }
}
