package com.example.osmparsing.RTree;

import com.example.osmparsing.OSMElement;
import java.util.*;

public class RTreeNode {
    boolean isLeaf;
    List<RTreeNode> children = new ArrayList<>();
    List<? extends OSMElement> elements = new ArrayList<>();
    double minLat, maxLat, minLon, maxLon;

    public RTreeNode(List<? extends OSMElement> elements, int depth) {
        this.elements = elements;
        if(depth % 2 == 0) {
            elements.sort((OSMElement self, OSMElement other) -> Double.compare(self.minLat, other.minLat));
        } else {
            elements.sort((OSMElement self, OSMElement other) -> Double.compare(self.minLon, other.minLon));
        }

        if(elements.size() > 5) {
            int pivot = elements.size() / 2;
            List<? extends OSMElement> left = elements.subList(0, pivot + 1);
            List<? extends OSMElement> right = elements.subList(pivot + 1, elements.size());

            children.add(new RTreeNode(left, depth + 1));
            children.add(new RTreeNode(right, depth + 1));
            isLeaf = false;

        } else {
            isLeaf = true;
            this.elements = elements;
        }
    }

    public boolean intersects(double qMinLat, double qMaxLat, double qMinLon, double qMaxLon) {
        return !(qMinLat > maxLat || qMaxLat < minLat || qMinLon > maxLon || qMaxLon < minLon);
    }

    public void computeBounds(){
        minLat = Double.MAX_VALUE;
        maxLat = -Double.MAX_VALUE;
        minLon = Double.MAX_VALUE;
        maxLon = -Double.MAX_VALUE;

        if(isLeaf){
            for(OSMElement element : elements){
                minLat = Math.min(minLat, element.minLat);
                maxLat = Math.max(maxLat, element.maxLat);
                minLon = Math.min(minLon, element.minLon);
                maxLon = Math.max(maxLon, element.maxLon);
            }
        } else {
            for(RTreeNode child : children){
                child.computeBounds();
                minLat = Math.min(minLat, child.minLat);
                maxLat = Math.max(maxLat, child.maxLat);
                minLon = Math.min(minLon, child.minLon);
                maxLon = Math.max(maxLon, child.maxLon);
            }
        }
    }
}
