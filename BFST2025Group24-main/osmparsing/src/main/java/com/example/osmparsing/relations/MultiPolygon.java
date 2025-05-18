package com.example.osmparsing.relations;

import com.example.osmparsing.Node;
import com.example.osmparsing.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.paint.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MultiPolygon implements Serializable {
    private Map<Long, Way> wayMapping;
    private List<Way> outerWay;
    private List<Way> innerWay;
    private Map<String, String> tags;

    private List<List<Node>> orderedOuterRings;
    private List<List<Node>> orderedInnerRings;

    private List<List<Node>> incompleteOuterRings = new ArrayList<>();
    private List<List<Node>> incompleteInnerRings = new ArrayList<>();

    public MultiPolygon(Map<Long, Way> wayMapping, List<RelationMember> members, Map<String, String> tags) {
        this.wayMapping = wayMapping;
        this.tags = tags;
        // Splitter members til indre og ydre ways
        outerWay = new ArrayList<>();
        innerWay = new ArrayList<>();
        for (RelationMember member : members) {
            if (member.getType() == RelationMember.RelationType.WAY) {
                System.out.println("Outer member [" + member + "] [" + member.getRole() + "] [" + member.getRef() + "]");
                long ref = member.getRef();
                Way way = wayMapping.get(ref);
                System.out.printf("Checking Member: ref=%d, way exists=%b%n", ref, (way != null)); // Debug log
                if (way != null) {
                    if ("outer".equals(member.getRole())) {
                        outerWay.add(way);
                    }
                    if ("inner".equals(member.getRole())) {
                        innerWay.add(way);
                    }
                }
            }
        }
        System.out.println("MultiPolygon: outer ways: " + outerWay + " inner ways: " + innerWay);
        orderedOuterRings = orderWays(outerWay, true);
        orderedInnerRings = orderWays(innerWay, false);
    }
    /// Metode for at sorterer ways i relationerne
    public List<List<Node>> orderWays(List<Way> ways, boolean isOuter) {
        List<Way> unorderedWays = new ArrayList<>(ways);
        List<List<Node>> rings = new ArrayList<>();
        //Sorterer Array nodes nu!
        while (!unorderedWays.isEmpty()) {
            List<Node> currentRing = new ArrayList<>();
            Way currentWay = unorderedWays.remove(0);
            currentRing.addAll(currentWay.getNodes());

            boolean extended = true;
            while (extended) {
                extended = false;
                Node lastNode = currentRing.get(currentRing.size() - 1);

                for (int i = 0 ; i < unorderedWays.size() ; i++) {
                    Way candidateWay = unorderedWays.get(i);
                    List<Node> candidateNodes = candidateWay.getNodes();
                    Node firstCandidateNode = candidateNodes.get(0);
                    Node lastCandidateNode = candidateNodes.get(candidateNodes.size() - 1);

                    //Match making, hvor matches den sidste og første node
                    if(lastNode.equals(firstCandidateNode)) {
                        System.out.println("Last node of ring: " + lastNode + " == first node: " + firstCandidateNode +" of candidateNodes");
                        currentRing.addAll(candidateNodes.subList(1, candidateNodes.size()));
                        unorderedWays.remove(i);
                        extended = true;
                        break;
                    }

                    //Omvendt hvis det ikke er den første, men sidste til sidste
                    if(lastNode.equals(lastCandidateNode)) {
                        System.out.println("Last node of ring: " + lastNode.getId() + " == last node: " + firstCandidateNode.getId() +" of candidateNodes");
                        Collections.reverse(candidateNodes);
                        currentRing.addAll(candidateNodes.subList(1, candidateNodes.size()));
                        unorderedWays.remove(i);
                        extended = true;
                        break;
                    }
                }
            }
            //Checker hvorvidt ringen er lukket med den første og sidste node er lig hinanden.
            // Samt handling af hvis det ikke er sådan.
            if (currentRing.get(0).equals(currentRing.get(currentRing.size() - 1))) {
                rings.add(currentRing);
            } else {
                System.err.println("Inordered rings!!!!!!!!");
                if (isOuter) {
                    incompleteOuterRings.add(currentRing);
                } else {
                    incompleteInnerRings.add(currentRing);
                }
            }
        }
        return rings;
    }
    //En polygon metode for at lave
    private Polygon createPolygonFromRing(List<Node> ring) {
        Polygon polygon = new Polygon();
        for (Node node : ring) {
            polygon.getPoints().addAll(node.getLon(), node.getLat());
        }
        return polygon;
    }
    /**
     * Laver multipolygon form med JavaFX!!!
     */
    public Shape buildShape(List<List<Node>> outerRings, List<List<Node>> innerRings, String type) {
        Shape outerShape = null;
        for (List<Node> ring: outerRings) {
            Polygon polygon = createPolygonFromRing(ring);
            if (outerShape == null) {
                outerShape = polygon;
            } else {
                outerShape = Shape.union(outerShape, polygon);
            }
        }
        if (outerShape != null) {
            for (List<Node> ring: innerRings) {
                Polygon hole = createPolygonFromRing(ring);
                outerShape = Shape.subtract(outerShape, hole);
            }
        }
        if (type != null) {
            switch (type) {
                case "building":
                    outerShape.setFill(Color.web("#F2E8DE"));
            }
        }
        return outerShape;
    }


    public Map<String, String> getTags() { return tags; }
    public Map<Long, Way> getWayMapping() { return wayMapping; }
    public List<Way> getOuterWays() {
        return outerWay;
    }
    public List<Way> getInnerWays() {
        return innerWay;
    }
    public List<List<Node>> getOrderedOuterRings() { return orderedOuterRings; }
    public List<List<Node>> getOrderedInnerRings() { return orderedInnerRings; }
}