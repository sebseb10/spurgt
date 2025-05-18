package com.example.osmparsing.Routing.RoutingAlgorithms;

import com.example.osmparsing.Node;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class Graph implements Serializable {

    public Map<Long, Node> nodes = new HashMap<>();
    public Map<Long, List<Edge>> adjacentEdges = new HashMap<>();

    public void addNode(Node node) {
        nodes.putIfAbsent(node.getId(), node);
        adjacentEdges.putIfAbsent(node.getId(), new ArrayList<>());
    }

    public void addEdge(long fromId, long toId) {
        Node from = nodes.get(fromId);
        Node to   = nodes.get(toId);
        if (from == null && to == null) return;
            Edge e = new Edge(from, to);
            adjacentEdges.get(fromId).add(e);   // outgoing
            adjacentEdges.get(toId).add(e);     // **incoming, for contraction**

    }
    public void addEdge(long fromId, long toId, double weight) {
        Node from = nodes.get(fromId);
        Node to = nodes.get(toId);
        if (from != null && to != null) {
            Edge e = new Edge(from, to, weight); // Use the constructor with weight
            adjacentEdges.get(fromId).add(e);
            adjacentEdges.get(toId).add(e);
        }
    }

    public List<Edge> getAllEdges() {
        // LinkedHashSet keeps insertion order while removing duplicates
        Set<Edge> uniq = new LinkedHashSet<>();
        for (List<Edge> edges : adjacentEdges.values()) {
            uniq.addAll(edges);
        }
        return new ArrayList<>(uniq);
    }

    private void deletusEdgus(Edge e) {
        adjacentEdges.get(e.getFrom().getId()).remove(e);
        adjacentEdges.get(e.getTo().getId()).remove(e);
    }
    public void contractNodes() {
        List<Long> nodesToRemove = new ArrayList<>();

        for (Long nodeId : new ArrayList<>(nodes.keySet())) {

            Set<Node> neighbours = adjacentEdges.get(nodeId)
                    .stream().map(e -> e.getFrom().getId() == nodeId
                    ? e.getTo() : e.getFrom())
                    .collect(Collectors.toSet());

            if (neighbours.size() != 2) continue;                    //skipper nodes, der ikk er del af kæden
            Iterator<Node> it = neighbours.iterator();
            Node v = it.next();
            Node w = it.next();
            if (v == w) continue;

            Node currentNode = nodes.get(nodeId);
/** !!!!!!!!!!!!!!!!!! THIS IS WHERE OUR STRAIGHT ROADS CAN TURN CURVY,
 *  !!!!!!!!!!!!!!!!!! But due to Highway needing to be a polyline, and runtime speed.
 *  !!!!!!!!!!!!!!!!!! This has been commented out */
            double angle = calculateAngle(v, currentNode, w);
            if (Math.abs(180 - angle) > 3) { // Justeres efter behov nok
                continue;
            }

            double bestV = Double.POSITIVE_INFINITY,
                    bestW = Double.POSITIVE_INFINITY;

            for (Edge e : adjacentEdges.get(nodeId)) {
                if (e.getFrom().getId() == v.getId() && e.getTo().getId()==nodeId ||
                        e.getTo()==v   && e.getFrom().getId()==nodeId)
                    bestV = Math.min(bestV, e.getDistanceWeight());

                if (e.getFrom().getId() ==w.getId() && e.getTo().getId()==nodeId ||
                        e.getTo()==w   && e.getFrom().getId()==nodeId)
                    bestW = Math.min(bestW, e.getDistanceWeight());
            }
            double contractedWeight = bestW + bestV;
            addEdge(v.getId(), w.getId(), contractedWeight);
            addEdge(w.getId(), v.getId(), contractedWeight);

            for (Edge e : new ArrayList<>(adjacentEdges.get(nodeId))) {
                adjacentEdges.get(e.getFrom().getId()).remove(e);
                adjacentEdges.get(e.getTo().getId()).remove(e);
            }
            nodesToRemove.add(nodeId);
        }
        System.out.println("Contracted " + nodesToRemove.size() + " nodes");

        // *insert the cleaning lady from family guy* "noo noo"
        for (Long nodeId : nodesToRemove) {
            nodes.remove(nodeId);
            adjacentEdges.remove(nodeId);
        }
    }

    private double calculateAngle(Node v, Node currentNode, Node w) {
        double dx1 = v.getLon() - currentNode.getLon();
        double dy1 = v.getLat() - currentNode.getLat();
        double dx2 = w.getLon() - currentNode.getLon();
        double dy2 = w.getLat() - currentNode.getLat();

        double dot = dx1 * dx2 + dy1 * dy2;
        double mag1 = Math.hypot(dx1, dy1);
        double mag2 = Math.hypot(dx2, dy2);

        if (mag1 == 0 || mag2 == 0) return 180.0;

        double cosTheta = Math.max(-1.0, Math.min(1.0, dot / (mag1 * mag2)));
        return Math.toDegrees(Math.acos(cosTheta));
    }

    public Map<Long, List<Edge>> getAdjacentEdges() {
        return adjacentEdges;
    }
}