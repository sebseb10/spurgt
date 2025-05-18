package com.example.osmparsing.algorithms;

import com.example.osmparsing.Routing.RoutingAlgorithms.NoSuchElementException;

import java.util.Iterator;
import java.util.TreeMap;

public class SymbolTable<Key extends Comparable<Key>, Value> implements Iterable<Key> {

    private TreeMap<Key, Value> st;

    /**
     * Returns a Set of Map.Entry objects (key-value pairs) in ascending key order.
     * This is analogous to the standard Java Map.entrySet() method.
     */
    public static class STEntry<K, V> {
        private K key;
        private V value;
        public STEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        public K getKey() { return key; }
        public V getValue() { return value; }
    }
    /**
     * Initializes an empty symbol table.
     */
    public SymbolTable() {
        st = new TreeMap<Key, Value>();
    }
    /**
     * Returns the value associated with the given key in this symbol table.
     *
     * @param  key the key
     * @return the value associated with the given key if the key is in this symbol table;
     *         {@code null} if the key is not in this symbol table
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Value get(Key key) {
        if (key == null) throw new IllegalArgumentException("called get() with null key");
        return st.get(key);
    }

    /**
     * Inserts the specified key-value pair into the symbol table, overwriting the old
     * value with the new value if the symbol table already contains the specified key.
     * Removes the specified key (and its associated value) from this symbol table
     * if the specified value is {@code null}.
     *
     * @param  key the key
     * @param  val the value
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void put(Key key, Value val) {
        if (key == null) throw new IllegalArgumentException("called put() with null key");
        if (val == null) st.remove(key);
        else             st.put(key, val);
    }

    /**
     * Removes the specified key and its associated value from this symbol table
     * (if the key is in this symbol table).
     *
     * @param  key the key
     * @throws IllegalArgumentException if {@code key} is {@code null}
     * @deprecated Replaced by {@link #remove(Comparable key)}.
     */
    @Deprecated
    public void delete(Key key) {
        if (key == null) throw new IllegalArgumentException("called delete() with null key");
        st.remove(key);
    }

    /**
     * Removes the specified key and its associated value from this symbol table
     * (if the key is in this symbol table).
     *
     * @param  key the key
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public void remove(Key key) {
        if (key == null) throw new IllegalArgumentException("called remove() with null key");
        st.remove(key);
    }

    /**
     * Returns true if this symbol table contain the given key.
     *
     * @param  key the key
     * @return {@code true} if this symbol table contains {@code key} and
     *         {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public boolean contains(Key key) {
        if (key == null) throw new IllegalArgumentException("called contains() with null key");
        return st.containsKey(key);
    }

    /**
     * Returns the number of key-value pairs in this symbol table.
     *
     * @return the number of key-value pairs in this symbol table
     */
    public int size() {
        return st.size();
    }

    /**
     * Clearing of the symbol table, removing all our entries in O(n) time. Same as Map.clear
     */
    public void clear() {
        st.clear();
    }

    /**
     * Clears the map in O(1) time, but leaves the old map then to Java Garbage Collection.
     * There might be some yuckies with access to old data, but hey it's fast!
     */
    public void clearFast() {
        st = new TreeMap<>();
    }

    /**
     * Returns true if this symbol table is empty.
     *
     * @return {@code true} if this symbol table is empty and {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns all keys in this symbol table in ascending order.
     * <p>
     * To iterate over all the keys in the symbol table named {@code st},
     * use the foreach notation: {@code for (Key key : st.keys())}.
     *
     * @return all keys in this symbol table
     */
    public Iterable<Key> keys() {
        return st.keySet();
    }

    /**
     * Returns all the keys in this symbol table in ascending order.
     * To iterate over all the keys in a symbol table named {@code st},
     * use the foreach notation: {@code for (Key key : st)}.
     *
     * @return an iterator to all the keys in this symbol table
     * @deprecated Replaced by {@link #keys()}.
     */
    @Deprecated
    public Iterator<Key> iterator() {
        return st.keySet().iterator();
    }
    /**
     * This is the homemade version of "entry set" .
     * It returns an iterable of (Key, Value) pairs, in ascending order of key.
     */
    public Iterable<STEntry<Key, Value>> entries() {
        return new Iterable<STEntry<Key, Value>>() {
            @Override
            public Iterator<STEntry<Key, Value>> iterator() {
                return new EntryIterator();
            }
        };
    }

    /**
     * Inner Iterator class that walks though keys in the TreeMap,
     * and constructs STEntry obj on the fly.
     */
    private class EntryIterator implements Iterator<STEntry<Key, Value>> {
        private final Iterator<Key> keyIterator = st.keySet().iterator();
        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }
        @Override
        public STEntry<Key, Value> next() {
            Key k = keyIterator.next();
            Value v = st.get(k);
            return new STEntry<>(k, v);
        }
    }
    /**
     * Returns the smallest key in this symbol table.
     *
     * @return the smallest key in this symbol table
     * @throws NoSuchElementException if this symbol table is empty
     */
    public Key min() {
        if (isEmpty()) throw new NoSuchElementException("called min() with empty symbol table");
        return st.firstKey();
    }

    /**
     * Returns the largest key in this symbol table.
     *
     * @return the largest key in this symbol table
     * @throws NoSuchElementException if this symbol table is empty
     */
    public Key max() {
        if (isEmpty()) throw new NoSuchElementException("called max() with empty symbol table");
        return st.lastKey();
    }

    /**
     * Returns the smallest key in this symbol table greater than or equal to {@code key}.
     *
     * @param  key the key
     * @return the smallest key in this symbol table greater than or equal to {@code key}
     * @throws NoSuchElementException if there is no such key
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Key ceiling(Key key) {
        if (key == null) throw new IllegalArgumentException("called ceiling() with null key");
        Key k = st.ceilingKey(key);
        if (k == null) throw new NoSuchElementException("all keys are less than " + key);
        return k;
    }

    /**
     * Returns the largest key in this symbol table less than or equal to {@code key}.
     *
     * @param  key the key
     * @return the largest key in this symbol table less than or equal to {@code key}
     * @throws NoSuchElementException if there is no such key
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public Key floor(Key key) {
        if (key == null) throw new IllegalArgumentException("called floor() with null key");
        Key k = st.floorKey(key);
        if (k == null) throw new NoSuchElementException("all keys are greater than " + key);
        return k;
    }
}

