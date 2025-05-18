package com.example.osmparsing.Routing.RoutingAlgorithms;

public class NoSuchElementException extends RuntimeException {
    public NoSuchElementException() {
        super();
    }
    public NoSuchElementException(String s) {
        super("No such element");
    }
}
