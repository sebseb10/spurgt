package com.example.osmparsing.MVC;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.zip.ZipInputStream;

import javax.xml.stream.*;

import com.example.osmparsing.*;
import com.example.osmparsing.RTree.RTree;
import com.example.osmparsing.Routing.RoutingAlgorithms.DirectedEdge;
import com.example.osmparsing.Routing.RoutingAlgorithms.Edge;
import com.example.osmparsing.Routing.RoutingAlgorithms.Graph;
import com.example.osmparsing.algorithms.EdgeWeightedDigraph;
import com.example.osmparsing.kdTree.KdPoint2D;
import com.example.osmparsing.kdTree.KdTree;
import com.example.osmparsing.algorithms.Bag;
import com.example.osmparsing.algorithms.SymbolTable;
import com.example.osmparsing.relations.RelationMember;
import com.example.osmparsing.wayEnums.HighwayType;
import com.example.osmparsing.wayEnums.NaturalType;
import com.example.osmparsing.relations.MultiPolygon;
import javafx.geometry.Point2D;
import javafx.util.Pair;

public class Model implements Serializable{
    List<Line> list = new ArrayList<>();
    public List<Way> ways = new ArrayList<>();
    List<Address> addresses = new ArrayList<>();
    public transient EdgeWeightedDigraph edgeWeightedGraph;

    public transient Graph graph; // rebuilding during parse

    public transient TST tst;
    public transient KdTree kdTree;
  
    public transient Map<Pair<Integer, Integer>, Highway> edgeToHighwayMap = new HashMap<>();


    public List<Highway> highwayList = new ArrayList<>();
    public List<Building> buildingList = new ArrayList<>();
    public List<OSMElement> otherList = new ArrayList<>();

    public transient RTree highwayRTree;
    public transient RTree buildingRTree;
    public transient RTree otherRTree;

    Set<KdPoint2D> uniquePoints = new HashSet<>();

    List<Relation> relations = new ArrayList<>();
    List<MultiPolygon> multipolygons = new ArrayList<>();
    Map<Long, Way> wayMapping = new HashMap<>();
    Way currentWay;
    /* ------------------------ OSM Bounds ------------------------ */
    double minlat, maxlat, minlon, maxlon;

    /* ------------------------ OSM element caches ------------------------ */
//    private transient final SymbolTable<Long, Way> wayMapping = new SymbolTable<>();
    transient final SymbolTable<Long, Node> id2node = new SymbolTable<>(); //Maps the Id to the Node
    private transient final Bag<Long> nodeIds = new Bag<>(); // Bags the nodeIds so they are collected
    private transient final SymbolTable<Long, double[]> tempLatLon = new SymbolTable<>(); // Maps a temporary node, without the Object by using and Array for lat/lon
    private transient final SymbolTable<Long,Integer> nodeIdToIndex = new SymbolTable<>(); // Indexes the node

    private transient final Bag<Double> latList = new Bag<>(); // Bag for latitudes
    private transient final Bag<Double> lonList = new Bag<>(); // Bag for longitudes
    private transient double[] latArr;
    private transient double[] lonArr;
    public long[] indexToId;
    /* ------------------------ Addresses Fields ------------------------ */
    private Long id;
    private double lat, lon;
    private String street, city, houseNo, postalCode;

    public static Model load(String filename) throws IOException, ClassNotFoundException, XMLStreamException, FactoryConfigurationError {
        if (filename.endsWith(".obj")) {
            try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                return (Model) in.readObject();
            }
        }
        return new Model(filename);
    }

    public Model(String filename) throws XMLStreamException, FactoryConfigurationError, IOException {

       //Final transient bliver ikke gemt- så prøver at gemme her!!!!!
        this.graph = new Graph();
        this.kdTree = new KdTree();
        this.tst = new TST();


        if (filename.endsWith(".osm.zip")) {
            parseZIP(filename);
        } else if (filename.endsWith(".osm")) {
            parseOSM(filename);
        } else {
            parseTXT(filename);
        }
        save(filename+".obj");
    }

    void save(String filename) throws IOException {
        try (var out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    private void parseZIP(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        var input = new ZipInputStream(new FileInputStream(filename));
        input.getNextEntry();
        parseOSM(input);
    }

    private void parseOSM(String filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        parseOSM(new FileInputStream(filename));
    }

    private void parseOSM(InputStream inputStream) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        var input = XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(inputStream));
//        var id2node = new SymbolTable<Long, Node>();
//        var tempLatLon = new SymbolTable<Long, double[]>();
//        var nodeIds = new Bag<Long>();
        var way = new ArrayList<Node>();
        var coast = false;

//        //ADDRESSES
//        id = null;
//        String street = null, city = null, houseNo = null, postalCode = null;
//        lat = 0;
//        lon = 0;

        //Relations
        boolean insideRelation = false;
        List<RelationMember> relationMembers = new ArrayList<>();
        Map<String, String> relationsTags = new HashMap<>();
        long currentRelationId = -1;
        List<Relation> relationsToProcess = new ArrayList<>();


        while (input.hasNext()) {
            var tagKind = input.next();
            if (tagKind == XMLStreamConstants.START_ELEMENT) {
                var name = input.getLocalName();
                switch (name) {
                    case "bounds":
                        parseBounds(input);
                        break;
                    case "node":
                        parseNode(input);
                        break;
//                    case "way":
//                        parseWay(input);
//                        break;
//                    case "relation":
//                        parseRelation(input);
//                        break;
//                    case "member":
//                        parseMember(input);
//                        break;
//                    case "tag":
//                        parseTag(input);
//                        break;
//                    case "nd":
//                        parseNd(input);
//                        break;
                }
                if (Objects.equals(name, "way")) {
                    id = Long.parseLong(input.getAttributeValue(null, "id"));
                    way.clear();
                    currentWay = null;
                    coast = false;
                }
                //begin parsing
                else if (Objects.equals(name, "relation")) {
                    insideRelation = true;
                    relationMembers.clear();
                    relationsTags.clear();
                    currentRelationId = Long.parseLong(input.getAttributeValue(null, "id"));
                }
                //PARSER RELATION MEMBER DETAILS!
                else if (insideRelation && name.equals("member")) {
                    var memberType = input.getAttributeValue(null, "type");
                    var role = input.getAttributeValue(null, "role");
                    var ref = Long.parseLong(input.getAttributeValue(null, "ref"));
                    RelationMember.RelationType relType;
                    if ("node".equals(memberType)) {
                        relType = RelationMember.RelationType.NODE;
                    } else if ("way".equals(memberType)) {
                        relType = RelationMember.RelationType.WAY;
                    } else if ("relation".equals(memberType)) {
                        relType = RelationMember.RelationType.RELATION;
                    } else {
                        continue;
                    }
                    relationMembers.add(new RelationMember(relType, role, ref));
                } else if (insideRelation && name.equals("tag")) {
                    var k = input.getAttributeValue(null, "k");
                    var v = input.getAttributeValue(null, "v");
                    relationsTags.put(k, v);
                }
                //TAG HANDLING
                else if (Objects.equals(name, "tag")) {
                    var k = input.getAttributeValue(null, "k");
                    var v = input.getAttributeValue(null, "v");

                    switch (k) {
                        case "highway":
                            currentWay = new Highway(id, way, HighwayType.fromValue(v));
                            /** Handles the actual paths and adds it to the graph, from which it draws */
                            if (way.size() >= 2 && currentWay instanceof Highway highway) {
                                for (int i = 0; i < way.size() - 1; i++) {
                                    Node from = way.get(i);
                                    Node to = way.get(i + 1);
                                    graph.addNode(from);
                                    graph.addNode(to);
                                    // Add both forward and reverse edges
                                    double distance = from.distanceTo(to);


                                    graph.addEdge(from.getId(), to.getId());
                                }
                            }
                            break;

                        case "building":
                            currentWay = new Building(id, way, v);
                            break;

                        case "natural":
                            currentWay = new Natural(id, way, NaturalType.fromValue(v));
                            break;

                        case "landuse":
                            currentWay = new Landuse(id, way, Landuse.LanduseType.fromValue(v));
                            break;

                        case "maxspeed":
                            if (currentWay instanceof Highway highway) {
                                highway.setMaxSpeed(v); // v is from OSM tag
                            }
                            break;

                        case "name":
                            if(currentWay instanceof Highway highway) {
                                highway.setName(v);
                            }

                        default:
                            break;
                    }

                    //READ ADDRESS
                    switch (k) {
                        case "addr:street":
                            street = v;
                            break;
                        case "addr:housenumber":
                            houseNo = v;
                            break;
                        case "addr:city":
                            city = v;
                            break;
                        case "addr:postcode":
                            postalCode = v;
                            break;
                    }

                    if (v.equals("coastline")) {
                        coast = true;
                    }

                } else if (Objects.equals(name, "nd")) {
                    var ref = Long.parseLong(input.getAttributeValue(null, "ref"));
                    var node = id2node.get(ref);
                    way.add(node);
                }
            } else if (tagKind == XMLStreamConstants.END_ELEMENT) {
                var name = input.getLocalName();
                switch (name) {
                    case "node":
                        endNode();
                        break;
                    case "way":
                        break;
                    case "relation":
                        break;
                }
                if (name.equals("way")) {
                    // Handle way end
                    if (currentWay == null) {
                        currentWay = new Way(id, new ArrayList<>(way));
                    }
                    ways.add(currentWay);
                    wayMapping.put(currentWay.getId(), currentWay);

                    if(currentWay instanceof Highway highway) {
                        highwayList.add(highway);
                    } else if(currentWay instanceof Building building) {
                        buildingList.add(building);
                    } else {
                        otherList.add(currentWay);
                    }

                } else if (name.equals("relation")) {
                    insideRelation = false;
                    // Check if it's a multipolygon
                    if ("multipolygon".equals(relationsTags.get("type"))) {
                        Relation relation = new Relation(currentRelationId, new ArrayList<>(relationMembers), new HashMap<>(relationsTags));
                        relations.add(relation);
                        relationsToProcess.add(relation); // Collect for later processing
//                        System.out.println("Parsed relation: " + relation);
                    }
                    // Reset relation data
                    relationMembers.clear();
                    relationsTags.clear();
                    currentRelationId = -1;
                }
            }
        }

        highwayRTree = new RTree(highwayList);
        buildingRTree = new RTree(buildingList);
        otherRTree = new RTree(otherList);

        for (Relation relation : relationsToProcess) {
            MultiPolygon mp = new MultiPolygon(wayMapping, relation.getMembers(), relation.getTags());
            multipolygons.add(mp);
        }
        processGraph();
    }
    public Map<Long, Way> getWayMapping() {
        return wayMapping;
    }


    private void parseTXT(String filename) throws FileNotFoundException {
        File f = new File(filename);
        try (Scanner s = new Scanner(f)) {
            while (s.hasNext()) {
                list.add(new Line(s.nextLine()));
            }
        }
    }

    //ADDRESSES
    //Get list of all addresses
    public List<Address> getAddresses() {
        return addresses;
    }

    //Add address to ternary tree
    public void addAddressToTree(Address address) {
        String key = address.getAddress().toLowerCase();
        tst.put(key, address); // Store address object in the TST
    }
    //Get the primitive Arrays
    public double[] getLatArray() { return latArr; }
    public double[] getLonArray() { return lonArr; }

    //Get the matching addresses to the prefix typed in search
    public List<Address> getMatchingAddresses(String prefix) {
        List<String> matchingKeys = tst.keysWithPrefix(prefix.toLowerCase());
        List<Address> matchingAddresses = new ArrayList<>();

        for (String key : matchingKeys) {
            Object obj = tst.get(key); // Retrieve the object from TST

            if (obj instanceof Address) { // Ensure it's an Address object
                matchingAddresses.add((Address) obj); // Add to the list
            }
        }
        return matchingAddresses;
    }
    public void add(Point2D p1, Point2D p2) {
        list.add(new Line(p1, p2));
    }
    public List<Relation> getRelations() {
        return relations;
    }
    public List<MultiPolygon> getMultipolygons() {
        return multipolygons;
    }

    /* ============================== Element-Specific Handling ============================== */
    /**
     * Extracs map bounds (min/max lat/lon) from the <bounds></bounds> in the OSM file.
     * @param input
     * @throws XMLStreamException
     */
    private void parseBounds(XMLStreamReader input) throws XMLStreamException {
        minlat = Double.parseDouble(input.getAttributeValue(null, "minlat"));
        maxlat = Double.parseDouble(input.getAttributeValue(null, "maxlat"));
        minlon = Double.parseDouble(input.getAttributeValue(null, "minlon"));
        maxlon = Double.parseDouble(input.getAttributeValue(null, "maxlon"));
    }

    /**
     * Takes the OSM node objects and handles them, first by their id's and lat/lon.
     * It then handles them by putting them into our SymbolTables<>() and Bags<>().
     * After it handles the kdPoint2D by putting into the tree, if possible.
     * Lastly it resets the addresses parsed.
     * @param input
     * @fields id, lat, lon, id2node, nodeIds, tempLatLon, uniquePoints, kdTree, street, city, houseNo, postalCode
     * @throws XMLStreamException
     */
    private void parseNode(XMLStreamReader input) throws XMLStreamException {
        id = Long.parseLong(input.getAttributeValue(null, "id"));
        lat = Double.parseDouble(input.getAttributeValue(null, "lat"));
        lon = Double.parseDouble(input.getAttributeValue(null, "lon"));

        Node node = new Node(id, lat, lon);
        id2node.put(id, node);
        nodeIds.add(id);
        tempLatLon.put(id, new double[]{lat, lon});

        int idx = latList.size();
        nodeIdToIndex.put(id, idx);
        latList.add(lat);
        lonList.add(lon);

        KdPoint2D point = new KdPoint2D(lat, lon);
        if (!uniquePoints.contains(point)) {
            uniquePoints.add(point);
            kdTree.insert(point);
        } else {
            System.out.println("Duplicate point found: " + point);
        }
        //RESET ADDRESS VALUES
        street = city = houseNo = postalCode = null;
    }

    /**
     * Address handling. Part of endNode.
     */
    private void endNode() {
        if (street != null && houseNo != null && city != null && postalCode != null) {
            Address addr = new Address(street, houseNo, city, postalCode, id, lat, lon);
            addresses.add(addr);
            addAddressToTree(addr);
        }
    }

//---------------------------------------------------
    public SymbolTable<Long, Integer> getNodeIdToIndex() {
        return nodeIdToIndex;
    }

    public long osmId(int idx) {           // Model.java
        return indexToId[idx];
    }

    private void rebuildGraphStructures() {
        // Clear end of data
        nodeIdToIndex.clear();
        latList.clear();
        lonList.clear();
        uniquePoints.clear();
        kdTree = new KdTree();

        List<Node> graphNodes = new ArrayList<>(graph.nodes.values());
        indexToId = new long[graphNodes.size()];

        for (int i = 0; i < graphNodes.size(); i++) {
            Node node = graphNodes.get(i);
            nodeIdToIndex.put(node.getId(), i);
            indexToId[i] = node.getId();

            // Rebuild lat/lon and KD-tree for contracted nodes
            latList.add(node.getLat());
            lonList.add(node.getLon());
            KdPoint2D point = new KdPoint2D(node.getId(), node.getLat(), node.getLon());
            kdTree.insert(point);
        }

        // Rebuild primitive arrays
        latArr = new double[latList.size()];
        lonArr = new double[lonList.size()];
        int i = 0;
        for (Double d : latList) latArr[i++] = d;
        i = 0;
        for (Double d : lonList) lonArr[i++] = d;

        // Build edgeWeightedGraph
        this.edgeWeightedGraph = new EdgeWeightedDigraph(graphNodes.size());
        for (Edge e : graph.getAllEdges()) {
            int fromIdx = nodeIdToIndex.get(e.getFrom().getId());
            int toIdx = nodeIdToIndex.get(e.getTo().getId());
            double weight = e.getDistanceWeight();
            edgeWeightedGraph.addEdge(new DirectedEdge(fromIdx, toIdx, weight));
            edgeWeightedGraph.addEdge(new DirectedEdge(toIdx, fromIdx, weight));
        }
        //---------------------------------!!!!!!!!!!!!!!!!-------------------------------------------------
        System.out.println("Graph node count: " + graph.nodes.size());
        System.out.println("Total edges: " + graph.getAllEdges().size());
    }
    private void processGraph() {
        graph.contractNodes();      // Contracts nodes for the graph
        rebuildGraphStructures();   //swagger rebuild
    }

    private void buildEdgeToHighwayMap() {
        edgeToHighwayMap = new HashMap<>();
        for (Way way : wayMapping.values()) {
            if (way instanceof  Highway highway) {
                List<Node> nodes = highway.getNodes();
                for (int i = 0; i < nodes.size() - 1; i++) {
                    int fromIdx = nodeIdToIndex.get(nodes.get(i).getId());
                    int toIdx = nodeIdToIndex.get(nodes.get(i + 1).getId());
                    edgeToHighwayMap.put(new Pair<>(fromIdx, toIdx), highway);
                    edgeToHighwayMap.put(new Pair<>(toIdx, fromIdx), highway); // bidirectional
                }
            }
        }
    }
}