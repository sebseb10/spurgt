package com.example.osmparsing;

import java.io.Serializable;
import java.util.ArrayList;

import com.example.osmparsing.RTree.BoundingBox;
import com.example.osmparsing.wayEnums.WayType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Way extends OSMElement implements Serializable{
    private ArrayList<Node> nodes;
    double[] coords;
    String value;
    boolean isArea;

    public Way(Long id, ArrayList<Node> way) {
        super(id);
        isArea = false;
        this.nodes = way;
        coords = new double[way.size() * 2];

        populateCoords();
        computeBoundingBox();
    }

    private void populateCoords() {
        for (int i = 0; i < nodes.size(); i++) {
            var node = nodes.get(i);
            coords[2 * i] = 0.56 * node.lon;
            coords[2 * i + 1] = -node.lat;
        }
    }

    private void computeBoundingBox() {
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (int i = 0; i < coords.length; i += 2) {
            double x = coords[i];
            double y = coords[i + 1];

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // If you still want to store the results in the original minLat/maxLat etc.,
        // you can rename them or map them appropriately:
        this.minLon = minX;
        this.maxLon = maxX;
        this.minLat = minY;
        this.maxLat = maxY;
    }



    public void setValue(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public long getId() {
        return id;
    }
    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(minLon, minLat, maxLon, maxLat);
    }
    public double getMinLat() { return minLat; }
    public double getMaxLat() { return maxLat; }
    public double getMinLon() { return minLon; }
    public double getMaxLon() { return maxLon; }

    public void draw(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(coords[0], coords[1]);
        for (int i = 2 ; i < coords.length ; i += 2) {
            gc.lineTo(coords[i], coords[i+1]);
        }
        gc.stroke();
    }

    public boolean isClosed() {
        return !getNodes().isEmpty()
                && getNodes().get(0).equals(getNodes().get(getNodes().size()-1));
    }

    public void drawColor(GraphicsContext gc, Color color) {}

    @Override
    public void drawEle(GraphicsContext gc) {
    }
}