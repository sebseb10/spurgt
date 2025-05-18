package com.example.osmparsing;

import com.example.osmparsing.wayEnums.NaturalType;
import com.example.osmparsing.wayEnums.WayType;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;


import java.util.ArrayList;

public class Natural extends Way{
    NaturalType naturalType;
    Color color;

    public Natural(long id, ArrayList<Node> way, NaturalType naturalType) {
        super(id, way);
        //super.value = value;
        this.naturalType = naturalType;
    }

    public void setColor() {
        color = switch(naturalType){
            case COASTLINE -> Color.BLACK;
            case WATER -> Color.web("#B6D9E8");
            case TREE, TREE_ROW, WOOD -> Color.web("#B9D3A0");
            case GRASSLAND -> Color.web("#D6F5B0");
            case HEATH -> Color.web("#E2E2A0");
            case SCRUB -> Color.web("#CBDD9E");
            case BEACH, SHOAL -> Color.web("#FFF4AA");
            case SAND -> Color.web("#F4E7C1");
            case GLACIER -> Color.web("#E2F1F4");
            case UNCLASSIFIED -> color = Color.BLACK;
        };
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void drawEle(GraphicsContext gc) {
        setColor();
        int nPoints = coords.length / 2;
        double[] xPoints = new double[nPoints];
        double[] yPoints = new double[nPoints];
        for (int i = 0 ; i < nPoints ; ++i) {
            xPoints[i] = coords[2 * i];
            yPoints[i] = coords[2 * i + 1];
        }
        if (naturalType == NaturalType.COASTLINE) {
             gc.setStroke(this.naturalType.getColor());
             gc.setLineWidth(this.naturalType.getLineWidth());
             gc.strokePolyline(xPoints, yPoints, nPoints);
        } else {
             gc.setStroke(this.naturalType.getColor());
             gc.setLineWidth(this.naturalType.getLineWidth());
             if (naturalType == NaturalType.UNCLASSIFIED) {
                 gc.strokePolyline(xPoints, yPoints, nPoints);
             }
             else {
                 gc.setFill(this.naturalType.getColor());
                 gc.fillPolygon(xPoints, yPoints, nPoints);
             }

         }
    }

}
