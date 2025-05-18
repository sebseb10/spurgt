package com.example.osmparsing;

import javafx.scene.canvas.GraphicsContext;

import javafx.scene.paint.Color;
import java.io.Serializable;
import java.util.ArrayList;

public class Landuse extends Way implements Serializable {
    public enum LanduseType {
        COMMERCIAL("commercial", Color.GREY),
        INDUSTRIAL("industrial", Color.LIGHTSLATEGREY),
        RESIDENTIAL("residential", Color.LIGHTPINK),
        GRASS("grass", Color.LIGHTGREEN),
        BASIN("basin", Color.LIGHTCORAL),
        CEMETERY("cemetery", Color.PURPLE),
        FOREST("forest",Color.GREEN),
        FARMLAND("farmland", Color.YELLOWGREEN),
        UNCLASSIFIED("unclassified", Color.TRANSPARENT);

        String value;
        Color color;

        LanduseType(String value, Color color){
            this.value = value;
            this.color = color;
        }

        public static LanduseType fromValue(String value) {
            return switch (value) {
                case "commercial" -> COMMERCIAL;
                case "industrial" -> INDUSTRIAL;
                case "residential" -> RESIDENTIAL;
                case "grass" -> LanduseType.GRASS;
                case "basin" -> LanduseType.BASIN;
                case "cemetery" -> LanduseType.CEMETERY;
                case "forest" -> LanduseType.FOREST;
                case "farmland" -> LanduseType.FARMLAND;
                default -> LanduseType.UNCLASSIFIED;
            };
        }

        public String getValue() { return value; }
        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }

        public static void updateColorsForMode(String mode) {
            switch (mode) {
                case "Protanopia":
                    COMMERCIAL.setColor(Color.DARKGRAY);
                    INDUSTRIAL.setColor(Color.GRAY);
                    RESIDENTIAL.setColor(Color.BURLYWOOD);
                    GRASS.setColor(Color.OLIVEDRAB);
                    BASIN.setColor(Color.INDIANRED);
                    CEMETERY.setColor(Color.SLATEBLUE);
                    FOREST.setColor(Color.DARKOLIVEGREEN);
                    FARMLAND.setColor(Color.DARKKHAKI);
                    break;

                case "Deuteranopia":
                    COMMERCIAL.setColor(Color.web("#A9A9A9")); // Dark Gray
                    INDUSTRIAL.setColor(Color.web("#708090")); // Slate Gray
                    RESIDENTIAL.setColor(Color.web("#FFA07A")); // Light Salmon
                    GRASS.setColor(Color.web("#87CEEB")); // Sky Blue
                    BASIN.setColor(Color.web("#4682B4")); // Steel Blue
                    CEMETERY.setColor(Color.web("#9370DB")); // Medium Purple
                    FOREST.setColor(Color.web("#4169E1")); // Royal Blue
                    FARMLAND.setColor(Color.web("#DAA520")); // Goldenrod
                    break;

                case "Tritanopia":
                    COMMERCIAL.setColor(Color.SLATEGRAY);
                    INDUSTRIAL.setColor(Color.GAINSBORO);
                    RESIDENTIAL.setColor(Color.PEACHPUFF);
                    GRASS.setColor(Color.MEDIUMSEAGREEN);
                    BASIN.setColor(Color.LIGHTPINK);
                    CEMETERY.setColor(Color.BLUEVIOLET);
                    FOREST.setColor(Color.FORESTGREEN);
                    FARMLAND.setColor(Color.DARKSEAGREEN);
                    break;

                case "Default":
                    COMMERCIAL.setColor(Color.GREY);
                    INDUSTRIAL.setColor(Color.LIGHTGRAY);
                    RESIDENTIAL.setColor(Color.LIGHTPINK);
                    GRASS.setColor(Color.LIGHTGREEN);
                    BASIN.setColor(Color.LIGHTCORAL);
                    CEMETERY.setColor(Color.PURPLE);
                    FOREST.setColor(Color.GREEN);
                    FARMLAND.setColor(Color.YELLOWGREEN);
                    break;

                default:
                    System.out.println("Unknown landuse color mode: " + mode);
                    break;
            }

            UNCLASSIFIED.setColor(Color.TRANSPARENT); // Always default
        }
    }

    LanduseType landuseType;

    public Landuse(long id, ArrayList<Node> way, LanduseType landuseType) {
        super(id, way);
        this.landuseType = landuseType;
    }

    public LanduseType getLanduseType() { return landuseType; }

    @Override
    public void drawEle(GraphicsContext gc){
        /*
        Color color = switch (landuseType) {
            case GRASS -> Color.GREEN;
            case BASIN -> Color.BLUE;
            case FOREST -> Color.DARKGREEN;
            case CEMETERY -> Color.PURPLE;
            default -> Color.BLACK;
        };

         */
        int nPoints = coords.length / 2;
        double[] xPoints = new double[nPoints];
        double[] yPoints = new double[nPoints];
        for (int i = 0 ; i < nPoints ; ++i) {
            xPoints[i] = coords[2 * i];
            yPoints[i] = coords[2 * i + 1];
        }
        gc.setFill(this.landuseType.getColor());
        gc.setStroke(this.landuseType.getColor());
        gc.fillPolygon(xPoints, yPoints, nPoints);
    }
}
