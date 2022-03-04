package edu.kit.informatik;

import edu.kit.informatik.graph.Node;
import edu.kit.informatik.utils.AddressParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;


import static edu.kit.informatik.graph.GraphRules.isCircular;

/**
 * @author unyrg
 * @version 1.0
 */
public class Network implements Cloneable {

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
    public Network(final IP root, final List<IP> children) {
        if (children.isEmpty()) throw new RuntimeException();
        this.networkRoot = new Node(root, convertToNode(children));
        network.add(this.networkRoot);
        allNodes.addAll(updateAllNodes(network));
        if (isCircular(this.networkRoot, allNodes))
            throw new IllegalArgumentException("ERROR: Circular Tree");
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
        allNodes.addAll(updateAllNodes(network));
        if (isCircular(networkRoot, allNodes))
            throw new ParseException("ERROR: Circular Tree");
    }

    private Network(Node node, List<Node> subNets) throws ParseException {
        network.add(node);
        for (Node subnet : subNets) {
            allNodes.addAll(getAsList(subnet.copy()));
        }
        networkRoot = node;
        allNodes.addAll(updateAllNodes(network));
        if (isCircular(networkRoot, allNodes))
            throw new ParseException("ERROR: Circular Tree");
    }

    /**
     * Adding a subnet to the network
     *
     * @param subnet subnetwork
     * @return true or false depending on if subnet gets actually connected to main-net
     */
    public boolean add(final Network subnet) {
        try {
            if (subnet == null) return false;
            List<Node> subnetNetwork = subnet.getNetwork();
            Network subnetCopy = new Network(subnet.networkRoot.copy(), subnetNetwork);
            SortedSet<Node> allNodesCopy = getAsList(networkRoot.copy());

            for (Node net : subnetCopy.getNetwork()) {
                boolean foundSubNode = false;
                for (Node child : getAsList(net)) {
                    if (allNodesCopy.contains(child)) {
                        foundSubNode = true;
                        subnetCopy.betterChangeRoot(child.getAddress(), null);
                        updateAllNodes(List.of(net));
                        Node mergePoint = allNodesCopy.stream().filter(x -> x.compareTo(child) == 0).findFirst().get();
                        Node updatedNet
                            = subnetCopy.getNetwork().stream().filter(x -> x.compareTo(child) == 0).findFirst().get();
                        mergePoint.addChildren(updatedNet.getChildren());
                    }
                }
                if (!foundSubNode) {
                    network.add(net);
                }
            }

            Node prob = allNodesCopy.stream().filter(x -> x.getParent() == null).findFirst().orElse(null);
            if (prob == null) {
                network.addAll(subnetCopy.getNetwork());
                return false;
            }
            SortedSet<Node> test = new TreeSet<>();
            test.addAll(allNodes);
            test.addAll(subnetCopy.allNodes);

            if (isCircular(prob, test)) {
                System.err.println("Tree is circular");
                network.addAll(subnetCopy.getNetwork());
                return false;
            } else {
                allNodes.addAll(test);
                network.remove(networkRoot);
                networkRoot = prob;
                network.add(networkRoot);
            }

        } catch (ParseException ignored) {
        }
        return true;
    }


    public SortedSet<Node> getDuplicate(Node subnet, Node mainNet) {
        SortedSet<Node> sub = getAsList(subnet);
        SortedSet<Node> main = getAsList(mainNet);
        sub.retainAll(main);
        return sub;
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
            System.out.println("test");
            parent.getChildren().remove(getAsNode(ip2));
            return false;
        }
        return true;
    }


    public boolean disconnect(final IP ip1, final IP ip2) {
        if (!allNodes.contains(getAsNode(ip1)) || !allNodes.contains(getAsNode(ip2)))
            return false;
        Node parent = getAsNode(ip1);
        return parent.getChildren().contains(getAsNode(ip2));
    }


    /**
     * checks if a specific IP is connected to the network
     *
     * @param ip IP-Address you want to check
     * @return true of false
     */
    public boolean contains(final IP ip) {
        return !checkIP(ip);
    }

    /**
     * Searching for the maximum depth of the tree
     *
     * @param root root ip for the network/starting point
     * @return returning the height/depth of the tree
     */
    public int getHeight(final IP root) {

        if (root == null || !allNodes.stream().anyMatch(x -> x.getAddress().compareTo(root) == 0)) return 0;
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
        if (root == null || checkIP(root)) return new ArrayList<>();
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


    SortedSet<Node> getAsList(Node root) {
        SortedSet<Node> list = new TreeSet<>();
        list.add(root);
        for (List<Node> cursor = new ArrayList<>(root.getChildren()); !cursor.isEmpty(); ) {
            list.addAll(cursor);
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
        }
        return list;
    }

    public List<IP> getRoute(final IP start, final IP end) {
        if (start == null || end == null || checkIP(start) || checkIP(end))
            return new ArrayList<>();
        betterChangeRoot(start, null);
        List<IP> path = new ArrayList<>();
        Node destination = getAsNode(end);
        while (destination.getParent() != null) {
            path.add(destination.getAddress());
            destination = destination.getParent();
        }
        path.add(destination.getAddress());
        Collections.reverse(path);
        if (!path.contains(start))
            return new ArrayList<>();
        return path;
    }

    /**
     * converts a graph to bracket notation
     *
     * @param root root of the graph
     * @return tree in bracket notation
     */
    public String toString(IP root) {
        if (root == null || checkIP(root)) return "";
        if (root.compareTo(networkRoot.getAddress()) != 0) {
            betterChangeRoot(root, null);
        }
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
            Collections.sort(root.getChildren());
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
    private SortedSet<Node> updateAllNodes(List<Node> network) {
        SortedSet<Node> nodes = new TreeSet<>();
        for (Node root : network) {
            if (!root.getChildren().isEmpty()) {
                nodes.add(root);
                nodes.addAll(updateAllNodes(root.getChildren()));
            } else {
                nodes.add(root);
            }
        }
        return nodes;
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
        network.remove(networkRoot);
        networkRoot = currentNode;
        network.add(networkRoot);
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


    public List<Node> getNetwork() {
        return List.copyOf(network);
    }

    public Node getNetworkRoot() {
        return networkRoot;
    }

    boolean checkIP(IP root) {
        return allNodes.stream().noneMatch(x -> x.getAddress().compareTo(root) == 0);
    }

}
