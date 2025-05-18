package com.example.osmparsing.kdTree;

import com.example.osmparsing.Node;
import java.io.Serializable;
import java.util.Stack;

public class KdTree implements Serializable {
    private Node root;
    private int size;

    public KdTree() {
        this.root = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public void insert(KdPoint2D p) {
        if (!contains(p)) {
            root = insertRec(root, p, 0);
        }
        //root = insertRec(root, p, 0);
    }

    public Node insertRec(Node root, KdPoint2D p, int depth) {
        if (root == null) {
            size++;
            return new Node(p.id(), p.x(), p.y());
        }

        int currentDimension = depth % 2;

        if (currentDimension == 0) {
            if (p.x() < root.p.x()) {
                root.left = insertRec(root.left, p, depth + 1);
            } else {
                root.right = insertRec(root.right, p, depth + 1);
            }

        } else {
            if (p.y() < root.p.y()) {
                root.left = insertRec(root.left, p, depth + 1);
            } else {
                root.right = insertRec(root.right, p, depth + 1);
            }
        }
        return root;
    }

    public boolean contains(KdPoint2D p) {
        if (p == null) {
            throw new NullPointerException("Point is null");
        }
        return contains(root, p, 0);
    }

    public boolean contains(Node root, KdPoint2D p, int depth) {
        if (root == null) {
            return false;
        }

        if(root.p.equals(p)) {
            return true;
        }

        int currentDimension = depth % 2;
        if (currentDimension == 0) {
            // compare lat
            if (p.x() < root.p.x()) {
                return contains(root.left, p, depth + 1);
            } else {
                return contains(root.right, p, depth + 1);
            }
        } else {
            // compare lon
            if (p.y() < root.p.y()) {
                return contains(root.left, p, depth + 1);
            } else {
                return contains(root.right, p, depth + 1);
            }
        }
    }

    public int size() { return size; }

    private double comparePoints(KdPoint2D p, Node n, int currentDimension) {
        if(currentDimension == 0) {
            return p.x() - n.p.x();
        } else {
            return p.y() - n.p.y();
        }
    }

    public Iterable<KdPoint2D> range(RectHV rect) {
        if (rect == null) {
            throw new NullPointerException("Called range() with a null RectHV");
        }

        Stack<KdPoint2D> points = new Stack<>();
        if(root == null) { return points; }

        Stack<Node> nodes = new Stack<>();
        nodes.push(root);
        while(!nodes.isEmpty()) {
            Node n = nodes.pop();

            if(rect.contains(n.p)) {
                points.push(n.p);
            }

            if(n.left != null && rect.intersects(n.left.rect)) {
                nodes.push(n.left);
            }
            if(n.right != null && rect.intersects(n.right.rect)) {
                nodes.push(n.right);
            }
        }
        return points;
    }

    public KdPoint2D nearestNeighbour(KdPoint2D p) {
        if(p == null) { throw new NullPointerException("Called nearestNeighbour() with a null Point2D"); }
        if(isEmpty()){ return null; }

        return nearestNeighbour(root, p, root.p, 0); //
    }

    private KdPoint2D nearestNeighbour(Node n, KdPoint2D p, KdPoint2D champion, int currentDimension) {
        // Base case: end of tree
        if (n == null) return champion;

        // If exact match
        if (n.p.equals(p)) return p;

        // Check if current node's point is closer than current champion
        if (n.p.distanceSquaredTo(p) < champion.distanceSquaredTo(p)) {
            champion = n.p;
        }

        // Distance from point to the splitting line
        double toPartitionLine = comparePoints(p, n, currentDimension);

        // Search the side of the tree that the point is on
        if (toPartitionLine < 0) {
            champion = nearestNeighbour(n.left, p, champion, (currentDimension + 1) % 2);

            // Check if we need to explore the other side
            if (champion.distanceSquaredTo(p) >= toPartitionLine * toPartitionLine) {
                champion = nearestNeighbour(n.right, p, champion, (currentDimension + 1) % 2);
            }
        } else {
            champion = nearestNeighbour(n.right, p, champion, (currentDimension + 1) % 2);

            // Check if we need to explore the other side
            if (champion.distanceSquaredTo(p) >= toPartitionLine * toPartitionLine) {
                champion = nearestNeighbour(n.left, p, champion, (currentDimension + 1) % 2);
            }
        }
        return champion;
    }
}
