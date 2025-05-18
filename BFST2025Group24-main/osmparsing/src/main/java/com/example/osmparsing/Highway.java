package com.example.osmparsing;

import com.example.osmparsing.wayEnums.HighwayType;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Arrays;

public class Highway extends Way{
    HighwayType highwayType;
    String maxSpeed;
    Color color;
    String name;

    public Highway(long id, ArrayList<Node> way, HighwayType highwayType) {
        super(id, way);
        this.highwayType = highwayType;
    }

    public void setMaxSpeed(String maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public double getSpeedInMetersPerSecond() {
        if (maxSpeed != null) {
            try {
                // Case: "50", "70 km/h"
                if (maxSpeed.matches("\\d+")) {
                    return Double.parseDouble(maxSpeed) / 3.6;
                }

                if (maxSpeed.matches("\\d+\\s*km/h")) {
                    return Double.parseDouble(maxSpeed.split("\\s")[0]) / 3.6;
                }

                if (maxSpeed.equalsIgnoreCase("none")) {
                    // Assume 130 km/h (e.g., German autobahn)
                    return 130 / 3.6;
                }

                // Could add support for "mph" here
            } catch (NumberFormatException ignored) {}
        }

        // fallback to default for highway type
        return highwayType.getDefaultSpeedMps();
    }

    @Override
    public void drawEle(GraphicsContext gc) {
        int nPoints = coords.length / 2;
        double[] xPoints = new double[nPoints];
        double[] yPoints = new double[nPoints];

        for (int i = 0; i < nPoints; ++i) {
            xPoints[i] = coords[2 * i];
            yPoints[i] = coords[2 * i + 1];
        }

        gc.setStroke(this.highwayType.getColor());
        gc.setLineWidth(this.highwayType.getLineWidth());
        gc.strokePolyline(xPoints, yPoints, nPoints);
    }
}