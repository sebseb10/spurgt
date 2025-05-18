package com.example.osmparsing;

import com.example.osmparsing.Routing.RoutingAlgorithms.Graph;
import com.example.osmparsing.MVC.Controller;
import com.example.osmparsing.MVC.Model;
import com.example.osmparsing.MVC.View;
import com.example.osmparsing.Routing.RoutingAlgorithms.HaversineDistance;
import javafx.application.Platform;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DijkstraTest {
    private Controller controller;
    private Model testModel;

    @BeforeAll
    static void initJavaFX() {
        Platform.startup(() -> {
        });
    }

    void setController() throws NonInvertibleTransformException {
        controller = new Controller(testModel, new View(testModel, new Stage()));
    }

    Model setUp() {
        try {
            testModel = Model.load("data/bornholm.osm.zip");
        } catch (FileNotFoundException e) {
            fail("Test OSM file not found: " + e.getMessage());
        } catch (IOException | XMLStreamException | ClassNotFoundException e) {
            fail("Error loading test OSM file: " + e.getMessage());
        }
        return testModel;
    }
    Graph makeSimpleChain() {
        Graph g = new Graph();

        Node A = new Node(1, 0, 0);
        Node B = new Node(2, 0, 1);
        Node C = new Node(3, 0, 2);

        g.addNode(A);
        g.addNode(B);
        g.addNode(C);

        g.addEdge(1, 2);   // A → B
        g.addEdge(2, 3);   // B → C

        return g;
    }
    @Test
    void addNode() {
        Graph graph = new Graph();
        Node nodeA = new Node(955570889, 55.1001400, 14.7040600);
        graph.addNode(nodeA);
        assertTrue(graph.nodes.containsKey(nodeA.getId()));
    }

    @Test
    void testAddEdge() {
        Graph g = makeSimpleChain();
        assertEquals(1, g.adjacentEdges.get(1L).size());
        assertEquals(2, g.adjacentEdges.get(2L).size());
    }

    @Test
    void getAllEdges() {
        Graph g = makeSimpleChain();
        assertEquals(2, g.getAllEdges().size());
    }

    @Test
    void contractNodes() {
        Graph g = makeSimpleChain();
        System.out.println(g.getAdjacentEdges());
        g.contractNodes();
        System.out.println(g.getAdjacentEdges());
        assertFalse(g.nodes.containsKey(2L));          // B removed
        assertEquals(2, g.getAllEdges().size());       // A→C plus its reverse C→A
        boolean hasShortcut = g.getAllEdges()
                .stream()
                .anyMatch(e -> e.getFrom().getId()==1L
                        && e.getTo().getId()==3L);
        assertTrue(hasShortcut);
    }

    @Test
    void testInvalidAddressReturnsEmpty() {
        Model m = setUp();
        List<Address> matches = testModel.getMatchingAddresses("Nonexistent Road 999");
        assertTrue(matches.isEmpty());
    }

    @Test
    void testFindClosestNodeToAddress() throws Exception {
        Model m = setUp();
        List<Address> hits = m.getMatchingAddresses("Lille Torv 8 Rønne 3700");
        assertFalse(hits.isEmpty());

        Address adr = hits.get(0);

        Controller con = new Controller(m);
        Node picked = invokeFindClosest(con, adr);

        assertEquals(2454811600L, picked.getId(),
                        "KD-tree slected wrong node for Lille Torv 8");
    }
    private Node invokeFindClosest(Controller c, Address a) throws Exception {
        Method m = Controller.class.getDeclaredMethod("findClosestGraphNode", Address.class);
        m.setAccessible(true);
        return (Node) m.invoke(c, a);
    }
    @Test
    void testPathBetweenKnownAddresses() throws Exception {
        Model m = setUp();
        Address from = m.getMatchingAddresses("Lille Torv 8 Rønne 3700")
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Start address not found"));
        Address to = m.getMatchingAddresses("Kirkevej 2 Aakirkeby 3720")
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("End address not found"));
        Controller con = new Controller(m);
        con.computeRouteBetween(from, to);
        List<Node> path = con.getRouteNodes();

        assertFalse(path.isEmpty(), "Path is empty");
        Node expectedStart = invokeFindClosest(con, from);
        assertEquals(expectedStart.getId(), path.get(0).getId(),
                "Path does not start at the nearest road node");

        Node expectedEnd = invokeFindClosest(con, to);
        assertEquals(expectedEnd.getId(), path.get(path.size() - 1).getId(),
                "Route does not end at snapped destination node");

        double startDist = HaversineDistance.haversine(
                expectedStart.getLat(), expectedStart.getLon(),
                from.getLat(), from.getLon());
        assertTrue(startDist < 25, "Snapping distance too high (" + startDist + " m)");

        assertTrue(path.size() > 1, "Trivial path returned");
    }

    @Test
    void testGraphContractionReduceNodes(){
        Graph g = testGraph();
        assertEquals(3, g.nodes.size(), "graph should have 3 nodes before contraction");
        assertEquals(4, g.getAllEdges().size(), "graph should have 4 edges before contraction");
        g.contractNodes();

        assertFalse(g.nodes.containsKey(2L), "middle node not removed");
        assertEquals(2, g.getAllEdges().size(), "unexpected edge count after contraction");
        boolean fwd = g.getAllEdges().stream()
                .anyMatch(e -> e.getFrom().getId()==1L && e.getTo().getId()==3L);
        boolean rev = g.getAllEdges().stream()
                .anyMatch(e -> e.getFrom().getId()==3L && e.getTo().getId()==1L);
        assertTrue(fwd && rev, "shortcut A⇄C not found");
    }

    @Test
    void testEdgeWeightsArePositive() {
        Graph g = makeSimpleChain();
        g.contractNodes();
        assertTrue(g.getAllEdges().stream()
                        .allMatch(e -> e.getDistanceWeight() > 0),
                "Negative or zero edge weight found");
    }

    @Test
    void testClosestNodeIsReachable() throws Exception {
        Model m = setUp();
        Address adr = m.getMatchingAddresses("Lille Torv 8 Rønne 3700").get(0);
        Controller con = new Controller(m);           // headless
        Node n = invokeFindClosest(con, adr);
        int idx = m.getNodeIdToIndex().get(n.getId());
        assertNotNull(idx, "Node not present in mapping");
        assertTrue(m.edgeWeightedGraph.adj(idx).iterator().hasNext(),
                "Closest node is isolated – no outgoing edges");
    }

    @Test
    void testAddressSearchFindsValidAddress() {
        Model m = setUp();
        assertFalse(m.getMatchingAddresses("Lille Torv 8 Rønne 3700").isEmpty());
    }


    Graph testGraph() {
        Graph g = new Graph();

        Node a = new Node(1, 55.00, 12.00);
        Node b = new Node(2, 55.01, 12.01);
        Node c = new Node(3, 55.02, 12.02);

        g.addNode(a); g.addNode(b); g.addNode(c);
        g.addEdge(1, 2);        // A→B
        g.addEdge(2, 1);        // B→A
        g.addEdge(2, 3);        // B→C
        g.addEdge(3, 2);        // C→B
        return g;
    }
}