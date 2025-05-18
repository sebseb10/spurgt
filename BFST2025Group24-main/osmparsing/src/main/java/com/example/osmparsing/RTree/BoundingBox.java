package com.example.osmparsing.RTree;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.Serializable;

public class BoundingBox implements Serializable {
    protected double minX, minY, maxX, maxY;

    public BoundingBox(double x1, double y1, double x2, double y2) {
        this.minX = Math.min(x1,x2);
        this.minY = Math.min(x1,x2);
        this.maxX = Math.min(y1, y2);
        this.maxY = Math.max(y1, y2);
    }

    public boolean isOutside(BoundingBox other, double tolerance) {
        return (this.maxX + tolerance) < other.minX ||
                (this.minX - tolerance) > other.maxX ||
                (this.maxY + tolerance) < other.minY ||
                (this.minY - tolerance) > other.maxY;
    }

    public void drawBoundingBox(GraphicsContext gc, Color color) {
        gc.setStroke(color);
        gc.beginPath();
        gc.moveTo(minX, minY);
        gc.lineTo(maxX, minY);
        gc.stroke();
        gc.moveTo(maxX, minY);
        gc.lineTo(maxX, maxY);
        gc.stroke();
        gc.moveTo(maxX, maxY);
        gc.lineTo(minX, maxY);
        gc.stroke();
        gc.moveTo(minX, maxY);
        gc.lineTo(minX, minY);
        gc.stroke();
        gc.closePath();
    }

    public double getMinX() { return minX; }
    public double getMinY() { return minY; }
    public double getMaxX() { return maxX; }
    public double getMaxY() { return maxY; }

    @Override
    public String toString() {
        return "[" + minX + ", " + maxX + "] x [" + minY + ", " + maxY + "]";
    }
}
