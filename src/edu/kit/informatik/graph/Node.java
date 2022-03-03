package edu.kit.informatik.graph;

import edu.kit.informatik.IP;
import edu.kit.informatik.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author unyrg
 * @version 1.0
 */
public class Node implements Comparable<Node> {

    private final IP address;
    private List<Node> children;
    private Node parent;

    /**
     * @param address  a
     * @param children a
     */

    public Node(IP address, List<Node> children) {
        this.address = address;
        this.children = children;
        if (!children.isEmpty()) {
            for (Node child : children) {
                child.parent = this;
            }
            Collections.sort(children);
        }
    }

    /**
     * Deep copy a node structure without references
     *
     * @return A deep copy of a node with its children
     * @throws ParseException when creating a new IP fails
     */
    public Node copy() throws ParseException {
        if (!this.getChildren().isEmpty()) {
            List<Node> children = new ArrayList<>();
            for (Node child : this.getChildren()) {
                children.add(child.copy());
            }
            return new Node(new IP(this.getAddress().toString()), children);
        } else {
            return new Node(new IP(this.getAddress().toString()), new ArrayList<>());
        }
    }


    public void setChildren(List<Node> children) {
        for (Node child : children) {
            child.setParent(this);
        }
        this.children = children;
    }

    public List<Node> getChildren() {
        return children;
    }

    public IP getAddress() {
        return address;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(Node o) {
        return this.address.compareTo(o.address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!Objects.equals(address, node.address)) return false;
        if (!Objects.equals(children, node.children)) return false;
        return Objects.equals(parent, node.parent);
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

}
