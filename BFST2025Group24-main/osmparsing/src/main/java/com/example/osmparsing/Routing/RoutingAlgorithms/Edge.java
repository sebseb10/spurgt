package com.example.osmparsing.Routing.RoutingAlgorithms;

import com.example.osmparsing.Node;

import java.io.Serializable;
import java.util.Objects;

public class Edge implements Serializable {
    //repræsenterer edge imellem 2 nodes

    private final Node from;
    private final Node to;
    private final double distanceWeight;
    //private final Way way;

    public Edge(Node from, Node to) {
        this.from = from;
        this.to = to;
        this.distanceWeight = from.distanceTo(to);
    }
    public Edge(Node from, Node to, double weight) {
        this.from = from;
        this.to = to;
        this.distanceWeight = weight;
        //this.way = way;
    }


    public Node getFrom() {
        return from;
    }
    public Node getTo() {
        return to;
    }
    public double getDistanceWeight() {
        return distanceWeight;
    }

    @Override
    public boolean equals(Object o) {
        Edge other = (Edge) o;

        long a1 = from.getId(), b1 = to.getId();
        long a2 = other.from.getId(), b2 = other.to.getId();

        return (a1 == a2 && b1 == b2) || (a1 == b2 && b1 == a2);
    }
    @Override
    public int hashCode() {
        long a = from.getId(), b = to.getId();
        long lo = Math.min(a, b), hi = Math.max(a, b);
        return Objects.hash(lo, hi);
    }
}