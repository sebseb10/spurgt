package com.example.osmparsing;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.ArrayList;
import javafx.scene.paint.Color;

public class Building extends Way implements Serializable {
    Color color;

    public Building(Long id, ArrayList<Node> way, String value) {
        super(id, way);
        super.value = value;
    }

    public void drawEle(GraphicsContext gc) {
        color = Color.LIGHTGRAY;
        int nPoints = coords.length / 2;
        double[] xPoints = new double[nPoints];
        double[] yPoints = new double[nPoints];
        for (int i = 0 ; i < nPoints ; ++i) {
            xPoints[i] = coords[2 * i];
            yPoints[i] = coords[2 * i + 1];
        }

        gc.setFill(color);
        gc.setStroke(this.color);
        gc.fillPolygon(xPoints, yPoints, nPoints);
    }
}
