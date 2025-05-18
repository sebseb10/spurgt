package com.example.osmparsing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//From the algorithms book
public class TST<Value> implements Serializable {
    private Node root; // root of trie

    private class Node implements Serializable {
        char c; // character
        Node left, mid, right; // left, middle, and right subtries
        Value val; // value associated with string
    }

    public Value get(String key) {
        if(key == null || key.isEmpty()) return null; // added || key.isEmpty()
        Node x = get(root, key, 0);
        if (x == null) return null;
        return (Value) x.val;
    }

    private Node get(Node x, String key, int d) {
        if (x == null || d >= key.length()) return null; // added || d >= key.length()
        if (d < 0) return null;
        char c = key.charAt(d);
        if (c < x.c) return get(x.left, key, d);
        else if (c > x.c) return get(x.right, key, d);
        else if (d < key.length() - 1)
            return get(x.mid, key, d+1);
        else return x;
    }

    public void put(String key, Value val) {
        root = put(root, key, val, 0);
    }

    private Node put(Node x, String key, Value val, int d) {
        char c = key.charAt(d);
        if (x == null) { x = new Node(); x.c = c; }
        if (c < x.c) x.left = put(x.left, key, val, d);
        else if (c > x.c) x.right = put(x.right, key, val, d);
        else if (d < key.length() - 1)
            x.mid = put(x.mid, key, val, d+1);
        else x.val = val;
        return x;
    }

    // New method: Find all keys matching a given prefix
    public List<String> keysWithPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        Node x = get(root, prefix, 0);
        if (x == null) return results;
        if (x.val != null) results.add(prefix); // If prefix itself is a key, add it
        collect(x.mid, new StringBuilder(prefix), results);
        return results;
    }

    // Helper function: Recursively collect keys
    private void collect(Node x, StringBuilder prefix, List<String> results) {
        if (x == null) return;
        collect(x.left, prefix, results);
        if (x.val != null) results.add(prefix.toString() + x.c);
        collect(x.mid, prefix.append(x.c), results);
        prefix.setLength(prefix.length() - 1); // Backtrack
        collect(x.right, prefix, results);
    }

    public String nodeString(Node x) {
        String nameOfAddress = null;
        if (x.val instanceof Address) {
            nameOfAddress = ((Address) x.val).getAddress();
        }
        return nameOfAddress;
    }
}