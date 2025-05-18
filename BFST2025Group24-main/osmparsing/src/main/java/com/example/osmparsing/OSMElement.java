package com.example.osmparsing;

import javafx.scene.canvas.GraphicsContext;

public abstract class OSMElement {
    public long id;
    public double minLat, maxLat, minLon, maxLon;

    OSMElement(Long id) {
        this.id = id;
    }

    public abstract void drawEle(GraphicsContext gc);
}
