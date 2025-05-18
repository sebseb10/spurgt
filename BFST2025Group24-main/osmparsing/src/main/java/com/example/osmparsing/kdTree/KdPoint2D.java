package com.example.osmparsing.kdTree;

import java.io.Serializable;

public class KdPoint2D implements Serializable {
    private final double x;
    private final double y;
    private final long id;

    // same as osm node??
    public KdPoint2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.id = -1;
    }

    public KdPoint2D(long id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public double x(){ return x; }
    public double y(){ return y; }
    public long id(){ return id; }

    public double distanceSquaredTo(KdPoint2D that) {
        double dx = this.x - that.x;
        double dy = this.y - that.y;
        return dx*dx + dy*dy;
    }


    @Override
    public boolean equals(Object other) {
        if(other == this) return true;
        if(other == null) return false;
        if(other.getClass() != this.getClass()) return false;
        KdPoint2D that = (KdPoint2D) other;
        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        int hashX = ((Double) x).hashCode();
        int hashY = ((Double) y).hashCode();
        return hashX + hashY;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}


