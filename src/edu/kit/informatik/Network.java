package edu.kit.informatik;

import edu.kit.informatik.graph.Node;
import edu.kit.informatik.utils.AddressParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static edu.kit.informatik.graph.GraphRules.betterIsCircular;

/**
 * @author unyrg
 * @version 1.0
 */
public class Network implements Cloneable {

    /**
     *
     */
    List<Node> subnets = new ArrayList<>();
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
        subnets.add(this.networkRoot);
        allNodes.addAll(updateAllNodes(subnets));
        if (betterIsCircular(this.networkRoot)) throw new IllegalArgumentException("ERROR: Circular Tree");
    }


    /**
     * @param bracketNotation input string to create a new network
     * @throws ParseException invalid bracket notation
     */
    public Network(final String bracketNotation) throws ParseException {
        if (bracketNotation == null || bracketNotation.split(" ").length <= 1)
            throw new ParseException("Invalid bracket notation");
        networkRoot = AddressParser.bracketParser(bracketNotation);
        subnets.add(networkRoot);
        allNodes.addAll(updateAllNodes(subnets));
        if (betterIsCircular(networkRoot)) throw new ParseException("ERROR: Circular Tree");
    }

    private Network(List<Node> subNets) throws ParseException {
        for (Node subnet : subNets) {
            subnets.add(subnet.copy());
        }
        networkRoot = subnets.get(0);
        allNodes = updateAllNodes(subnets);
        if (betterIsCircular(networkRoot)) throw new ParseException("ERROR: Circular Tree");
    }


    /**
     * Adding a subnet to the network
     *
     * @param subnet subnetwork
     * @return true or false depending on if subnet gets actually connected to main-net
     */
    public boolean add(final Network subnet) {
        if (subnet == null) return false;
        boolean changed = false;
        try {
            Network subnetCopy = new Network(subnet.getSubnets());

            List<Node> independentTrees = new ArrayList<Node>();
            List<Node> dependentTrees = new ArrayList<Node>();
            List<Node> subNetUsed = new ArrayList<>();
            for (Node root : this.subnets) {
                for (Node subnetNode : subnetCopy.subnets) {
                    if (!subNetUsed.contains(subnetNode)) {
                        boolean thisChanged
                            = this.connectNodes(root, subnetNode, subnetCopy, independentTrees, dependentTrees);
                        if (thisChanged) subNetUsed.add(subnetNode);
                        changed = changed || thisChanged;
                    }
                }
            }
//            TODO connect same Network

            this.subnets.addAll(independentTrees);
            for (Node independent : independentTrees) {
                allNodes.addAll(subnetCopy.getAsList(independent));
                changed = true;
            }

            List<Node> used = new ArrayList<>();
            List<Node> rootUsed = new ArrayList<>();
            independentTrees = new ArrayList<>();
            dependentTrees = new ArrayList<>();

            for (Node root : this.subnets) {
                if (!used.contains(root) && !rootUsed.contains(root)) {
                    for (Node other : this.subnets) {
                        if (root != other && !used.contains(other) && !rootUsed.contains(other)) {
                            boolean thisChanged
                                = this.connectNodes(root, other, this, independentTrees, dependentTrees);
                            if (thisChanged) used.add(other);
                            if (thisChanged && !rootUsed.contains(root)) rootUsed.add(root);
                        }
                    }
                }
            }

            this.subnets.removeAll(used);


        } catch (ParseException e) {
        }

        return changed;
    }

    // Search after connection Node
    private IP findConnection(Node root, Node sub) {
        List<List<IP>> rootLayers = getLevels(root.getAddress());
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

    private void connectChildrenNodes(Node root, Node sub, Network subnetCopy) {
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
                Node tempSub = subnetCopy.getAsNode(child.getAddress());
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

    private boolean connectNodes(Node root, Node sub, Network subnet, List<Node> toAdd, List<Node> added) {
        IP connection = findConnection(root, sub);
        if (connection != null) {
            try {
                Network subnetCopy = new Network(subnet.getSubnets());
                subnetCopy.subnets.removeAll(added);
                Node connectionSub;
                Node connectionRoot;
                if (subnet == this) {
                    connectionRoot = this.getAsNode(connection, this.subnets.indexOf(root));
                    connectionSub = subnetCopy.getAsNode(connection, this.subnets.indexOf(sub));
                    subnetCopy.betterChangeRoot(connection, null, this.subnets.indexOf(sub));
                } else {
                    connectionRoot = this.getAsNode(connection);
                    connectionSub = subnetCopy.getAsNode(connection);
                    subnetCopy.betterChangeRoot(connection, null);
                }
                Node tempRoot = connectionRoot.copy();
                connectChildrenNodes(tempRoot, connectionSub, subnetCopy);
                if (getAsList(connectionRoot).size() == getAsList(tempRoot).size()) return false;
                if (betterIsCircular(tempRoot)) {
                    return false;
                } else {
                    connectChildrenNodes(connectionRoot, connectionSub, subnetCopy);
                    allNodes.addAll(updateAllNodes(List.of(connectionRoot)));
                }

                if (!added.contains(sub)) added.add(sub);
                toAdd.remove(sub);
                return true;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            if (!added.contains(sub) && !toAdd.contains(sub)) toAdd.add(sub);
        }
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

    /**
     * Connecting two existing IP-Addresses
     *
     * @param ip1 first IP
     * @param ip2 second IP
     * @return boolean if the connection was successful
     */
    public boolean connect(final IP ip1, final IP ip2) {
        if (ip1 == null || ip2 == null || checkIP(ip1) || checkIP(ip2) || ip1.compareTo(ip2) == 0) return false;
        Node node1 = getAsNode(ip1);
        Node node2 = getAsNode(ip2);
        Node netRoot = getSubnetRoot(getAsNode(ip1));
        Node netCopy = netRoot.copy();
        SortedSet<Node> netNodes = getAsList(netCopy);

        if (netNodes.stream().anyMatch(x -> x.getAddress().compareTo(ip2) == 0)) return false;
        Node mergePoint = netNodes.stream().filter(x -> x.getAddress().compareTo(ip1) == 0).findFirst().get();

        if (ip2.compareTo(getSubnetRoot(node2).getAddress()) != 0) betterChangeRoot(ip2, null);
        Node oldSubnet = getSubnetRoot(node1);
        mergePoint.addChildren(List.of(node2));

        if (betterIsCircular(netCopy)) {
            System.err.println("Tree is circular");
            return false;
        }
        node1.addChildren(List.of(node2));
        subnets.remove(oldSubnet);
        return true;
    }

    /**
     * Disconnecting two IP-Addresses
     *
     * @param ip1 first IP
     * @param ip2 second IP
     * @return boolean if the disconnection was successful
     */
    public boolean disconnect(final IP ip1, final IP ip2) {
        if (ip1 == null || ip2 == null || checkIP(ip1) || checkIP(ip2)) return false;
        Node node1 = getAsNode(ip1);
        Node node2 = getAsNode(ip2);

        if (allNodes.size() > 2) {
            //able to disconnect
            if (node1.getChildren().contains(node2)) {
                //IP2 is children of IP1
                removeConnection(node1, node2);
                return true;
            } else if (node2.getChildren().contains(node1)) {
                //IP1 is children of IP2

                betterChangeRoot(node1.getAddress(), null);
                removeConnection(node1, node2);
                return true;
            }
        }
        return false;
    }

    /**
     * removing a connection between two IPs
     *
     * @param node1 First IP
     * @param node2 Second IP
     */
    public void removeConnection(Node node1, Node node2) {

        if (node2.getChildren().isEmpty()) {
            node1.getChildren().remove(node2);
            allNodes.remove(node2);
            if (node1.getChildren().isEmpty() && node1.getParent() == null) {
                allNodes.remove(node1);
            }

        } else {
            node1.getChildren().remove(node2);
            node2.setParent(null);
            subnets.add(node2);
            if (node1.getChildren().isEmpty()) {
                subnets.remove(node1);
                allNodes.remove(node1);
            }
        }

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

        if (root == null || checkIP(root)) return 0;
        Node rootNode = getSubnetRoot(getAsNode(root));
        if (root.compareTo(rootNode.getAddress()) != 0) {
            betterChangeRoot(root, null);
        }
        return getLevels(getSubnetRoot(rootNode).getAddress()).size() - 1;
    }

    /**
     * Storing all nodes from the same layer into a list
     *
     * @param root root of the network
     * @return list of lists where every list contains every node of each layer
     */
    public List<List<IP>> getLevels(final IP root) {
        if (root == null || checkIP(root)) return new ArrayList<>();
        Node rootNode = getSubnetRoot(getAsNode(root));
        if (root.compareTo(rootNode.getAddress()) != 0) {
            betterChangeRoot(root, null);
        }
        List<List<IP>> layers = new ArrayList<>();
        layers.add(List.of(getSubnetRoot(rootNode).getAddress()));
        for (List<Node> cursor = new ArrayList<>(getSubnetRoot(rootNode).getChildren()); !cursor.isEmpty(); ) {
            layers.add(cursor.stream().map(Node::getAddress).sorted().collect(Collectors.toList()));
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
        }
        return layers;
    }


    /**
     * Getting list of all nodes which are in the subnet of root
     *
     * @param root root node of the subnet
     * @return list of all nodes this subnet contains
     */
    SortedSet<Node> getAsList(Node root) {
        SortedSet<Node> list = new TreeSet<>();
        list.add(root);
        for (List<Node> cursor = new ArrayList<>(root.getChildren()); !cursor.isEmpty(); ) {
            list.addAll(cursor);
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
        }
        return list;
    }

    /**
     * Getting a rout from one ip to another
     *
     * @param start starting point
     * @param end   destination
     * @return list of IPs which represents the rout between two IPs
     */
    public List<IP> getRoute(final IP start, final IP end) {
        if (start == null || end == null || checkIP(start) || checkIP(end)) return new ArrayList<>();
        betterChangeRoot(start, null);
        List<IP> path = new ArrayList<>();
        Node destination = getAsNode(end);
        while (destination.getParent() != null) {
            path.add(destination.getAddress());
            destination = destination.getParent();
        }
        path.add(destination.getAddress());
        Collections.reverse(path);
        if (!path.contains(start)) return new ArrayList<>();
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
        Node rootNode = getSubnetRoot(getAsNode(root));
        if (root.compareTo(rootNode.getAddress()) != 0) {
            betterChangeRoot(root, null);
        }
        return buildBracketNotation(getSubnetRoot(rootNode)).substring(1);
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
     * Changing the root address of a subnet
     *
     * @param newRoot   new root for the network
     * @param newParent new parent for each node
     */
    public void betterChangeRoot(IP newRoot, Node newParent) {
        Node currentNode = getAsNode(newRoot);
        changeToRoot(currentNode, newParent);
    }

    /**
     * Changing the root address of a specific subnet
     *
     * @param newRoot   new root for the network
     * @param newParent new parent for each node
     * @param subnet    index of subnet
     */
    public void betterChangeRoot(IP newRoot, Node newParent, int subnet) {
        Node currentNode = getAsNode(newRoot, subnet);
        changeToRoot(currentNode, newParent);
    }

    private void changeToRoot(Node currentNode, Node newParent) {
        Node nodeRoot = getSubnetRoot(currentNode);
        if (newParent == null) {
            subnets.remove(nodeRoot);
            subnets.add(currentNode);
        }
        if (currentNode.getParent() != null) {
            changeToRoot(currentNode.getParent(), currentNode);
            currentNode.getChildren().add(currentNode.getParent());
        }
        currentNode.getChildren().remove(newParent);
        currentNode.setParent(newParent);
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

    /**
     * Returning the associated node in a specific subnet
     *
     * @param node   an IP-Address
     * @param subnet index of subnet in subnets
     * @return IP-Address as its node
     */
    public Node getAsNode(IP node, int subnet) {
        return getAsList(subnets.get(subnet)).stream().filter(x -> x.getAddress().compareTo(node) == 0).findFirst()
            .orElse(null);
    }


    /**
     * getting the list of subnets from a network
     *
     * @return list of subnets
     */
    public List<Node> getSubnets() {
        return List.copyOf(subnets);
    }


    /**
     * checking if an IP is existing
     *
     * @param root the Ip you want to check
     * @return boolean, depending if IP exists or not
     */
    boolean checkIP(IP root) {
        return allNodes.stream().noneMatch(x -> x.getAddress().compareTo(root) == 0);
    }

    /**
     * getting the subnet root of a node
     *
     * @param node the node u want the root from
     * @return root node of the subnet
     */
    private Node getSubnetRoot(Node node) {
        if (node.getParent() == null) return node;
        return getSubnetRoot(node.getParent());
    }


}
