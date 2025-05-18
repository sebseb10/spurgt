package com.example.osmparsing.RTree;

import java.io.Serializable;
import java.util.*;

import com.example.osmparsing.OSMElement;

public class RTree implements Serializable {
    RTreeNode root;

    public RTree(List<? extends OSMElement> elements) {
        this.root = new RTreeNode(elements, 0);
        root.computeBounds();
    }

    public List<OSMElement> rangeSearch(double qMinLat, double qMaxLat, double qMinLon, double qMaxLon) {
        List<OSMElement> result = new ArrayList<>();
        rangeSearchRecursive(root, qMinLat, qMaxLat, qMinLon, qMaxLon, result);
        return result;
    }

    public List<OSMElement> rangeSearch(BoundingBox box){
        double qMinLat = box.getMinX();
        double qMaxLat = box.getMaxX();
        double qMinLon = box.getMinY();
        double qMaxLon = box.getMaxY();

        List<OSMElement> result = new ArrayList<>();
        rangeSearchRecursive(root, qMinLat, qMaxLat, qMinLon, qMaxLon, result);
        return result;
    }

    private void rangeSearchRecursive(RTreeNode node, double qMinLat, double qMaxLat, double qMinLon, double qMaxLon, List<OSMElement> result) {
        if(!node.intersects(qMinLat, qMaxLat, qMinLon, qMaxLon)) { return;}
        if(node.isLeaf){
            for(OSMElement element : node.elements) {
                if(!(element.maxLat < qMinLat || element.minLat > qMaxLat ||element.maxLon < qMinLon || element.minLon > qMaxLon)) {
                    result.add(element);
                }
            }
        } else {
            for(RTreeNode child : node.children) {
                rangeSearchRecursive(child, qMinLat, qMaxLat, qMinLon, qMaxLon, result);
            }
        }
    }

    public OSMElement findNearest(double lat, double lon) {
        // Define a small initial search window
        double radius = 0.0005; // ~50m depending on zoom level
        OSMElement nearest = null;
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < 10; i++) { // Try enlarging search area up to 10 times if nothing found
            BoundingBox searchBox = new BoundingBox(
                    lat - radius, lat + radius,
                    lon - radius, lon + radius
            );

            List<OSMElement> candidates = this.rangeSearch(searchBox);
            for (OSMElement element : candidates) {
                // For now use centroid of element bounds
                double elat = (element.minLat + element.maxLat) / 2;
                double elon = (element.minLon + element.maxLon) / 2;
                double dist = euclideanDist(lat, lon, elat, elon);

                if (dist < bestDistance) {
                    bestDistance = dist;
                    nearest = element;
                }
            }

            if (nearest != null) break;
            radius *= 2; // Expand search window
        }

        return nearest;
    }

    private double euclideanDist(double lat1, double lon1, double lat2, double lon2) {
        double dx = lat1 - lat2;
        double dy = lon1 - lon2;
        return dx * dx + dy * dy;
    }
}
