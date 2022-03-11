package edu.kit.informatik.graph;

import edu.kit.informatik.IP;
import edu.kit.informatik.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author unyrg
 * @version 1.0
 */
public class Node implements Comparable<Node> {

    private final IP address;
    private final List<Node> children;
    private Node parent;

    /**
     * Constructor of a node
     *
     * @param address  IP-Address
     * @param children list of children
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
     * @return A deep copy of a node with a copy of its children and so on
     */
    public Node copy() {
        try {
            if (!this.getChildren().isEmpty()) {
                List<Node> children = new ArrayList<>();
                for (Node child : this.getChildren()) {
                    children.add(child.copy());
                }
                return new Node(new IP(this.getAddress().toString()), children);
            } else {
                return new Node(new IP(this.getAddress().toString()), new ArrayList<>());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * adding new Children to a node
     *
     * @param children list of children to add
     */
    public void addChildren(List<Node> children) {
        for (Node child : children) {
            child.setParent(this);
        }
        this.children.addAll(children);
    }

    /**
     * Getting the list of children
     *
     * @return list of children for a node
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Getting the IP-Address of a node
     *
     * @return IP-reference
     */
    public IP getAddress() {
        return address;
    }

    /**
     * Getting the Parent of a node
     *
     * @return parent
     */
    public Node getParent() {
        return parent;
    }

    /**
     * setting the parent of a node
     *
     * @param parent new Parent
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Gets the number of degrees this node has
     *
     * @return number of connections
     */
    public int getDegree() {
        return getChildren().size() + (parent != null ? 1 : 0);
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
        if (address.compareTo(node.getAddress()) != 0) return false;
        if (parent != null && parent.getAddress().compareTo(node.getParent().getAddress()) != 0) return false;
        if (children != null && children.size() != node.getChildren().size()) return false;

        List<String> childAddresses = node.getChildren().stream().map(Node::toString).collect(Collectors.toList());
        for (Node child : children) {
            if (!childAddresses.contains(child.toString())) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

    /**
     * to String methode for a Node
     *
     * @return address but in point notation
     */
    public String toString() {
        return address.toString();
    }

}
