package edu.kit.informatik;

import edu.kit.informatik.graph.Node;
import edu.kit.informatik.utils.AddressParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static edu.kit.informatik.graph.GraphRules.isCircular;

/**
 * @author unyrg
 * @version 1.0
 */
public class Network {

    /**
     *
     */
    List<Node> network = new ArrayList<>();
    /**
     *
     */
    SortedSet<Node> allNodes = new TreeSet<>();

    private Node networkRoot;


    /**
     * Creates a new graph
     *
     * @param root     root address
     * @param children child which is connected to root
     */
    public Network(final IP root, final List<IP> children) throws ParseException {
        if (children.isEmpty()) throw new RuntimeException();
        this.networkRoot = new Node(root, convertToNode(children));
        network.add(this.networkRoot);
        updateAllNodes(network);
        if (isCircular(this.networkRoot, allNodes))
            throw new ParseException("ERROR: Circular Tree");
    }


    /**
     * @param bracketNotation input string to create a new network
     * @throws ParseException invalid bracket notation
     */
    public Network(final String bracketNotation) throws ParseException {
        if (bracketNotation == null || bracketNotation.split(" ").length <= 1)
            throw new ParseException("Invalid bracket notation");
        networkRoot = AddressParser.bracketParser(bracketNotation);
        network.add(networkRoot);
        updateAllNodes(network);
        if (isCircular(networkRoot, allNodes))
            throw new ParseException("ERROR: Circular Tree");
    }

    /**
     * Adding a subnet to the network
     *
     * @param subnet subnetwork
     *
     * @return true or false depending on if subnet gets actually connected to main-net
     * @throws ParseException fails if creation of IP in .copy() fails
     */
    public boolean add(final Network subnet) throws ParseException {
        Node copy = subnet.getNetworkRoot().copy();
        network.add(copy);
        for (Node node : allNodes) {
            if (node.compareTo(copy) == 0) {
                List<Node> updatedChildren
                    = Stream.concat(node.getChildren().stream(), copy.getChildren().stream()).collect(
                    Collectors.toList());
                node.setChildren(updatedChildren);
                return true;
            }
        }
        network.add(copy);
        return false;
    }

    /**
     * @return list of all IP addresses
     */
    public List<IP> list() {
        List<IP> allAddresses = new ArrayList<>();
        for (Node node : allNodes) {
            allAddresses.add(node.getAddress());
        }
        return allAddresses;
    }

    public boolean connect(final IP ip1, final IP ip2) {

        if (!allNodes.contains(getAsNode(ip1)) || !allNodes.contains(getAsNode(ip2)))
            return false;
        if (ip1.compareTo(networkRoot.getAddress()) != 0)
            betterChangeRoot(ip1, null);
        Node parent = getAsNode(ip1);
        parent.getChildren().add(getAsNode(ip2));
        if (isCircular(networkRoot, allNodes)) {
            parent.getChildren().remove(getAsNode(ip2));
            return false;
        }
        return true;
    }

    public boolean disconnect(final IP ip1, final IP ip2) {
        return false;
    }


    /**
     * checks if a specific IP is connected to the network
     *
     * @param ip IP-Address you want to check
     * @return true of false
     */
    public boolean contains(final IP ip) {
        return allNodes.contains(getAsNode(ip));
    }

    /**
     * Searching for the maximum depth of the tree
     *
     * @param root root ip for the network/starting point
     * @return returning the height/depth of the tree
     */
    public int getHeight(final IP root) {
        if (root.compareTo(networkRoot.getAddress()) != 0)
            betterChangeRoot(root, null);
        return getLevels(root).size() - 1;
    }

    /**
     * Storing all nodes from the same layer into a list
     *
     * @param root root of the network
     * @return list of lists where every list contains every node of each layer
     */
    public List<List<IP>> getLevels(final IP root) {
        if (root == null) return new ArrayList<>();
        if (root.compareTo(networkRoot.getAddress()) != 0)
            betterChangeRoot(root, null);
        List<List<IP>> layers = new ArrayList<>();
        layers.add(List.of(networkRoot.getAddress()));
        for (List<Node> cursor = new ArrayList<>(networkRoot.getChildren()); !cursor.isEmpty(); ) {
            layers.add(cursor.stream().map(Node::getAddress).sorted().collect(Collectors.toList()));
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
        }
        return layers;
    }

    public List<IP> getRoute(final IP start, final IP end) {
        betterChangeRoot(start, null);
        List<IP> path = new ArrayList<>();
        Node destination = getAsNode(end);
        while (destination.getParent() != null) {
            path.add(destination.getAddress());
            destination = destination.getParent();
        }
        path.add(destination.getAddress());
        Collections.reverse(path);

        return path;
    }

    /**
     * converts a graph to bracket notation
     *
     * @param root root of the graph
     * @return tree in bracket notation
     */
    public String toString(IP root) {
        if (root.compareTo(networkRoot.getAddress()) != 0)
            betterChangeRoot(root, null);
        return buildBracketNotation(networkRoot).substring(1);
    }

    /**
     * Helper methode for {@link #toString(IP)}
     *
     * @param root root IP but as Node
     * @return full bracket notation as string
     */
    public StringBuilder buildBracketNotation(Node root) {
        StringBuilder bracketNotation = new StringBuilder();
        if (!root.getChildren().isEmpty()) {
            for (Node child : root.getChildren()) {
                bracketNotation.append(buildBracketNotation(child));
            }
            bracketNotation.insert(0, " (" + root.getAddress().toString()).append(")");
        } else {
            bracketNotation.append(" ").append(root.getAddress().toString());
        }
        return bracketNotation;
    }


    /**
     * Methode that converts a list of IP's to a list of Node's
     *
     * @param children list of children of the root
     * @return list of children of the root but as Node objects
     */
    private List<Node> convertToNode(List<IP> children) {
        //TODO: What happens when children have children?
        List<Node> list = new ArrayList<>();
        for (IP address : children) {
            list.add(new Node(address, new ArrayList<>()));
        }
        return list;
    }


    /**
     * Adding new Nodes to a list of all Nodes
     *
     * @param network new network or addresses that has been created
     */
    private void updateAllNodes(List<Node> network) throws ParseException {
        for (Node root : network) {
            if (!root.getChildren().isEmpty()) {
                allNodes.add(root);
                updateAllNodes(root.getChildren());
            } else {
                allNodes.add(root);
            }
        }
    }

    /**
     * Changing the root address of the network
     *
     * @param newRoot   new root for the network
     * @param newParent new parent for each node
     */
    public void betterChangeRoot(IP newRoot, Node newParent) {
        Node currentNode = getAsNode(newRoot);
        if (currentNode.getParent() != null) {
            betterChangeRoot(currentNode.getParent().getAddress(), currentNode);
            currentNode.getChildren().add(currentNode.getParent());
        }
        currentNode.getChildren().remove(newParent);
        currentNode.setParent(newParent);
        networkRoot = currentNode;
    }

    /**
     * Returning the associated node
     *
     * @param node an IP-Address
     * @return IP-Address as its node
     */
    public Node getAsNode(IP node) {
        return allNodes.stream().filter(x -> x.getAddress().compareTo(node) == 0).findFirst().orElse(null);
    }


    public Node getNetworkRoot() {
        return networkRoot;
    }
}
