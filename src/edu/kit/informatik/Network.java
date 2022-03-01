package edu.kit.informatik;

import edu.kit.informatik.graph.Node;
import edu.kit.informatik.utils.AddressParser;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;


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

    /**
     * Creates a new graph
     *
     * @param root     root address
     * @param children child which is connected to root
     */
    public Network(final IP root, final List<IP> children) throws ParseException {
        if (children.isEmpty()) throw new RuntimeException();
        Node parent = new Node(root, convertToNode(children));
        network.add(parent);
        updateAllNodes(network);
    }


    /**
     * @param bracketNotation input string to create a new network
     * @throws ParseException invalid bracket notation
     */
    public Network(final String bracketNotation) throws ParseException {
        if (bracketNotation == null || bracketNotation.split(" ").length <= 1)
            throw new ParseException("Invalid bracket notation");
        network.add(AddressParser.bracketParser(bracketNotation));
        updateAllNodes(network);
        if (isCircular(network.get(0), allNodes))
            throw new ParseException("ERROR: Circular Tree");
    }

    public boolean add(final Network subnet) {
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
        return false;
    }

    public boolean disconnect(final IP ip1, final IP ip2) {
        return false;
    }

    public boolean contains(final IP ip) {
        return false;
    }

    public int getHeight(final IP root) {
        return 0;
    }

    public List<List<IP>> getLevels(final IP root) {
        Node networkRoot = getRoot(root);
        List<List<IP>> layers = new ArrayList<>();
        layers.add(List.of(networkRoot.getAddress()));
        for (List<Node> cursor = new ArrayList<>(networkRoot.getChildren()); !cursor.isEmpty(); ) {
            layers.add(cursor.stream().map(Node::getAddress).sorted().collect(Collectors.toList()));
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
        }
        return layers;
    }

    public List<IP> getRoute(final IP start, final IP end) {
        return null;
    }

    /**
     * converts a graph to bracket notation
     *
     * @param root root of the graph
     * @return tree in bracket notation
     */
    public String toString(IP root) {
        Node networkRoot = getRoot(root);
        String bracketNotation = buildBracketNotation(networkRoot).toString();
        return bracketNotation.substring(1);
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
            //     System.out.println(root.getAddress().toString());
            if (!root.getChildren().isEmpty()) {
                allNodes.add(root);
                updateAllNodes(root.getChildren());
            } else {
                // if (allNodes.contains(root)) throw new ParseException("Tree is circular");
                allNodes.add(root);
            }
        }
    }


    private Node getRoot(IP root) {
        for (Node node : network) {
            if (node.getAddress().compareTo(root) == 0) {
                return node;
            }
        }
        //TODO: idk what to throw here
        throw new RuntimeException();
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
}
