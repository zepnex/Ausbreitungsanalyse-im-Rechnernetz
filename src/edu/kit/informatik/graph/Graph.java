package edu.kit.informatik.graph;

import edu.kit.informatik.IP;
import edu.kit.informatik.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author unyrg
 * @version 1.0
 */
public class Graph {
    /**
     * finding connection point between two trees
     *
     * @param rootLayers List of layers of the tree
     * @param sub        root note of the list we want to merge
     * @return IP if a merge-point was found
     */
    protected IP findConnection(List<List<IP>> rootLayers, Node sub) {
        List<IP> subIPs = getAsList(sub).stream().map(Node::getAddress).collect(Collectors.toList());
        for (List<IP> layer : rootLayers) {
            List<IP> tempSub = new ArrayList<>(List.copyOf(subIPs));
            tempSub.retainAll(layer);
            if (!tempSub.isEmpty()) {
                return tempSub.get(0);
            }
        }
        return null;
    }

    /**
     * Connecting the children of the connection points
     *
     * @param root       connecting point of the root tree
     * @param sub        connecting point of the subtree
     * @param subnetCopy copy of the network we want to merge
     */
    protected void connectChildrenNodes(Node root, Node sub, Network subnetCopy) {
        root.addChildren(
            sub.getChildren().stream().filter(
                x -> !root.getChildren().stream().map(Node::toString).collect(Collectors.toList())
                    .contains(x.toString())).collect(Collectors.toList()));
        List<Node> sameChildren
            = root.getChildren().stream().filter(
                x -> sub.getChildren().stream().map(Node::toString).collect(Collectors.toList()).contains(x.toString()))
            .collect(Collectors.toList());

        while (!sameChildren.isEmpty()) {
            List<Node> tempSame = new ArrayList<>();
            for (Node child : sameChildren) {
                Node tempSub = subnetCopy.getAsNode(child.getAddress(), -1);
                child.addChildren(
                    tempSub.getChildren().stream().filter(
                        x -> !child.getChildren().stream().map(Node::toString).collect(Collectors.toList())
                            .contains(x.toString())).collect(Collectors.toList()));
                tempSame.addAll(child.getChildren().stream().filter(
                        x -> tempSub.getChildren().stream().map(Node::toString).collect(Collectors.toList())
                            .contains(x.toString()))
                    .collect(Collectors.toList()));
            }
            sameChildren = tempSame;
        }
    }


    /**
     * Getting list of all nodes which are in the subnet of root
     *
     * @param root root node of the subnet
     * @return list of all nodes this subnet contains
     */
    protected SortedSet<Node> getAsList(Node root) {
        SortedSet<Node> list = new TreeSet<>();
        list.add(root);
        for (List<Node> cursor = new ArrayList<>(root.getChildren()); !cursor.isEmpty();) {
            list.addAll(cursor);
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
        }
        return list;
    }

    /**
     * Helper methode for toString in network
     *
     * @param root root IP but as Node
     * @return full bracket notation as string
     */
    protected StringBuilder buildBracketNotation(Node root) {
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
     * Adding new Nodes to a list of all Nodes
     *
     * @param network new network or addresses that has been created
     * @return returns list of all nodes which the network contains
     */
    protected SortedSet<Node> updateAllNodes(List<Node> network) {
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
     * getting the subnet root of a node
     *
     * @param node the node u want the root from
     * @return root node of the subnet
     */
    protected Node getSubnetRoot(Node node) {
        if (node.getParent() == null) return node;
        return getSubnetRoot(node.getParent());
    }

    /**
     * Changes the root of a subnet
     *
     * @param currentNode node we want to change
     * @param newParent new parent for current node
     * @param subnet subnet that gets changed
     * @return new List of subnets with changed root
     */
    protected List<Node> changeToRoot(Node currentNode, Node newParent, List<Node> subnet) {
        List<Node> sub = new ArrayList<>(subnet);
        Node nodeRoot = getSubnetRoot(currentNode);
        if (newParent == null) {
            sub.remove(nodeRoot);
            sub.add(currentNode);
        }
        if (currentNode.getParent() != null) {
            sub = changeToRoot(currentNode.getParent(), currentNode, sub);
            currentNode.getChildren().add(currentNode.getParent());
        }
        currentNode.getChildren().remove(newParent);
        currentNode.setParent(newParent);
        return sub;
    }


    /**
     * Methode that converts a list of IP's to a list of Node's
     *
     * @param children list of children of the root
     * @return list of children of the root but as Node objects
     */
    protected List<Node> convertToNode(List<IP> children) {
        //TODO: What happens when children have children?
        List<Node> list = new ArrayList<>();
        for (IP address : children) {
            list.add(new Node(address, new ArrayList<>()));
        }
        return list;
    }


}
