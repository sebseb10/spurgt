package com.example.osmparsing.Routing;

import com.example.osmparsing.Routing.RoutingAlgorithms.DirectedEdge;
import com.example.osmparsing.algorithms.Bag;

public interface ShortestPathRouter {
    boolean hasPathTo(int v);
    Iterable<DirectedEdge> pathTo(int v);
    Bag<DirectedEdge> getExploredEdges();
}
