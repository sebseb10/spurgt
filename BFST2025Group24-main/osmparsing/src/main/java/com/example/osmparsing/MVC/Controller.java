package com.example.osmparsing.MVC;
import com.example.osmparsing.*;
import com.example.osmparsing.Routing.AStarSP;
import com.example.osmparsing.Routing.DijkstraSP;
import com.example.osmparsing.Routing.RoutingAlgorithms.DirectedEdge;
import com.example.osmparsing.Routing.RoutingMode;
import com.example.osmparsing.algorithms.Bag;
import com.example.osmparsing.algorithms.SymbolTable;
import com.example.osmparsing.kdTree.KdPoint2D;
import javafx.geometry.Point2D;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Controller {
    private final Model model;
    private final View view;
    double lastX;
    double lastY;

    public int zoomSteps = 0;
    public final double baseZoomFactor = 1.1;
    private double zoomLevel = 1.0;
    POIController poiController = new POIController();

    private OSMElement nearestHoverElement = null;

//------------------- ROUTING FIELDS (dean far) -------------------
    public List<Node> currentRoute = new ArrayList<>();
    private List<Node> routeNodes = new ArrayList<>();
    private Bag<DirectedEdge> exploredEdges = new Bag<>();
  
    public double routeTime;
    private RoutingMode mode = RoutingMode.ASTAR;
//-------------------------------------------------------------------

    public POIController getPOIController() {
        return poiController;
    }

    public Controller(Model model) {
        this(model, null);
    }
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
        if (view != null) mouseHandlers();
    }
    private void mouseHandlers() {
        mousePressed();
        mouseDragged();
        mouseMoved();
    }
    private void mousePressed() {
        view.canvas.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();
            Point2D poiPoint = view.mousetoModel(lastX, lastY);
            double poiX = poiPoint.getX() / 0.56;
            double poiY = poiPoint.getY() * -1;

            // Use views method to add the POI
            view.addPOI(poiY, poiX);
        });
    }
    private void mouseDragged() {
        view.canvas.setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                // DRAW ON MAP
                // Point2D lastmodel = view.mousetoModel(lastX, lastY);
                // Point2D newmodel = view.mousetoModel(e.getX(), e.getY());
                // model.add(lastmodel, newmodel);
                try {
                    view.redraw();
                } catch (NonInvertibleTransformException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                double dx = e.getX() - lastX;
                double dy = e.getY() - lastY;
                try {
                    view.pan(dx, dy, true);
                } catch (NonInvertibleTransformException ex) {
                    throw new RuntimeException(ex);
                }
            }
            lastX = e.getX();
            lastY = e.getY();
        });
    }

    private void mouseMoved() {
        view.canvas.setOnMouseMoved(e -> {

            System.out.printf("Mouse moved to: x = %.2f, y = %.2f\n", e.getX(), e.getY());

            Point2D modelPoint = view.mousetoModel(e.getX(), e.getY());
            double lat = modelPoint.getY();
            double lon = modelPoint.getX();

            System.out.printf("Model coords: lon = %.6f, lat = %.6f\n", lon, lat);

            OSMElement nearest = model.highwayRTree.findNearest(lat, lon);
            if(nearest != null) {
                Highway nearestHighway = (Highway) nearest;
                System.out.println("NEAREST NOT NULL: " + nearest);
                System.out.println("Nearest ID: " + nearestHighway.getId());
                if(nearestHighway.getName() != null) {
                    System.out.println("NEAREST HIGHWAY name: " + nearestHighway.getName());
                }
            }
            if(nearest != nearestHoverElement) {
                nearestHoverElement = nearest;
                view.setHighlightedElement(nearestHoverElement);

                try {
                    view.redraw();
                } catch (NonInvertibleTransformException exception) {
                    throw new RuntimeException(exception);
                }
            }

        });
    }

/// ROUTE FINDING CALLS AND ALL RELATED ---------------------------------------------------------------------------------

public void setRoutingMode(RoutingMode m) { this.mode = m; }

    public void computeRouteBetween(Address from, Address to) {
        Node startNode = findClosestGraphNode(from);
        Node endNode   = findClosestGraphNode(to);

        int startIdx = model.getNodeIdToIndex().get(startNode.getId());
        int endIdx   = model.getNodeIdToIndex().get(endNode.getId());

        switch (mode) {
            case DIJKSTRA -> runDijkstra(startIdx, endIdx, startNode, endNode);
            case ASTAR    -> runAStar  (startIdx, endIdx, startNode, endNode);
        }
    }

    public void runDijkstra(int startIdx, int endIdx, Node startNode, Node endNode) {
        System.out.println("----------------!!! computeRouteBetween called!!!----------");
        long t0 = System.nanoTime();
        DijkstraSP sp = new DijkstraSP(model.edgeWeightedGraph, startIdx, endIdx);
        long t1 = System.nanoTime();
        System.out.printf("Dijkstra took %.3f ms, explored %d edges%n",
                (t1-t0)/1e6, sp.getExploredEdges().size());
        exploredEdges = sp.getExploredEdges();

        currentRoute.clear();
        if (sp.hasPathTo(endIdx)) {
            Stack<DirectedEdge> pathEdges = (Stack<DirectedEdge>) sp.pathTo(endIdx);

            currentRoute.add(model.id2node.get(model.indexToId[endIdx]));
            for (DirectedEdge e : sp.pathTo(endIdx))
                currentRoute.add(model.id2node.get(model.indexToId[e.from()]));
            Collections.reverse(currentRoute);
        } else System.out.println("No path found");

        routeNodes = new ArrayList<>(currentRoute);
        System.out.println("Start node ID: " + startNode.getId() + ", index: " + startIdx);
        System.out.println("End node ID: " + endNode.getId() + ", index: " + endIdx);

        if (sp.hasPathTo(endIdx)) {
            Stack<DirectedEdge> pathEdges = (Stack<DirectedEdge>) sp.pathTo(endIdx);

            routeTime = 0;
            double defaultSpeed = 50.0 / 3.6;

            for (DirectedEdge edge : pathEdges) {
                double distance = edge.weight();
                System.out.printf("Edge from %d to %d: distance = %.2f meters\n", edge.from(), edge.to(), distance);
                Highway highway = model.edgeToHighwayMap.get(new Pair<>(edge.from(), edge.to()));

                double speed = (highway != null) ? highway.getSpeedInMetersPerSecond() : defaultSpeed;
                routeTime += distance / speed;
            }

            System.out.printf("Estimated travel time: %.2f minutes\n", routeTime / 60.0);
        }
    }

    public void runAStar(int startIdx, int endIdx, Node startNode, Node endNode) {
        long t0 = System.nanoTime();
        AStarSP sp = new AStarSP(model.edgeWeightedGraph, startIdx, endIdx, model.getLatArray(), model.getLonArray());
        long t1 = System.nanoTime();
        System.out.printf("A* took %.3f ms, explored %d edges%n", (t1-t0)/1e6, sp.getExploredEdges().size());

        exploredEdges = sp.getExploredEdges();
        currentRoute.clear();
        if (sp.hasPathTo(endIdx)) {
            currentRoute.add(model.id2node.get(model.indexToId[endIdx]));
            for (DirectedEdge e : sp.pathTo(endIdx))
                currentRoute.add(model.id2node.get(model.indexToId[e.from()]));
            Collections.reverse(currentRoute);
        }
        routeNodes = new ArrayList<>(currentRoute);

        if (sp.hasPathTo(endIdx)) {
            Stack<DirectedEdge> pathEdges = (Stack<DirectedEdge>) sp.pathTo(endIdx);

            routeTime = 0;
            double defaultSpeed = 50.0 / 3.6;

            for (DirectedEdge edge : pathEdges) {
                double distance = edge.weight();
                System.out.printf("Edge from %d to %d: distance = %.2f meters\n", edge.from(), edge.to(), distance);
                Highway highway = model.edgeToHighwayMap.get(new Pair<>(edge.from(), edge.to()));

                double speed = (highway != null) ? highway.getSpeedInMetersPerSecond() : defaultSpeed;
                routeTime += distance / speed;
            }

            System.out.printf("Estimated travel time: %.2f minutes\n", routeTime / 60.0);
        }
}

    public boolean hasRoute() {
        return !currentRoute.isEmpty();
    }

    public List<Node> getRouteNodes() {
        return currentRoute;
    }

    public Bag<DirectedEdge> getExploredEdges() {
        return exploredEdges;
    }

    private long getRealNodeIdByIndex(int index) {
        for (SymbolTable.STEntry<Long, Integer> entry : model.getNodeIdToIndex().entries()) {
            if (entry.getValue() == index) return entry.getKey();
        }
        throw new IllegalStateException("Index not found");
    }

    private Node findClosestGraphNode(Address address) {
        KdPoint2D point = new KdPoint2D(address.getLat(), address.getLon());
        KdPoint2D nearest = model.kdTree.nearestNeighbour(point);

        Node closestNode = model.id2node.get(nearest.id());
        if (closestNode != null) {
            return closestNode;
        } else {
            Node closest = null;
            double minDist = Double.MAX_VALUE;

            for (Node node : model.graph.nodes.values()) {
                double dist = Math.pow(node.getLat() - nearest.x(), 2) + Math.pow(node.getLon() - nearest.y(), 2);
                if (dist < minDist) {
                    minDist = dist;
                    closest = node;
                }
            }


            if (closest != null) {
                System.out.println("Closest node found with relaxed match: " + closest.getId());
                return closest;
            }

            throw new IllegalStateException("No matching node found near address: " + address.getAddress());
        }
    }
/// ROUTE FINDING CALLS AND ALL RELATED ---------------------------------------------------------------------------------
    public double getRouteTime() { return routeTime; }


    // public double getZoomLevel() { return Math.pow(baseZoomFactor, zoomSteps); }
    public double getZoomLevel() { return zoomLevel; }

    public void zoomIn() { zoomSteps++; }
    public void zoomOut() { zoomSteps--; }
    public void setZoomLevel(double zoomLevel) { this.zoomLevel = zoomLevel;
    System.out.println("ZOOMLEVEL: " + zoomLevel);}

}
