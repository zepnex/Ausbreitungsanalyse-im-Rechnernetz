package edu.kit.informatik.network;


import edu.kit.informatik.exceptions.ParseException;
import edu.kit.informatik.graph.Node;
import edu.kit.informatik.utils.AddressParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;


import static edu.kit.informatik.utils.Graph.buildBracketNotation;
import static edu.kit.informatik.utils.Graph.changeToRoot;
import static edu.kit.informatik.utils.Graph.connectChildrenNodes;
import static edu.kit.informatik.utils.Graph.convertToNode;
import static edu.kit.informatik.utils.Graph.findConnection;
import static edu.kit.informatik.utils.Graph.getAsList;
import static edu.kit.informatik.utils.Graph.getSubnetRoot;
import static edu.kit.informatik.utils.Graph.updateAllNodes;
import static edu.kit.informatik.utils.GraphRules.betterIsCircular;
import static edu.kit.informatik.utils.GraphRules.checkIP;

/**
 * Class which represents a Network
 *
 * @author unyrg
 * @version 1.0
 */
public class Network {

    private List<Node> subnets = new ArrayList<>();
    private SortedSet<Node> allNodes = new TreeSet<>();

    private final Node networkRoot;

    /**
     * Creates a new NodeTree
     *
     * @param root     root address
     * @param children child which is connected to root
     */
    public Network(final IP root, final List<IP> children) {
        if (children.isEmpty()) throw new RuntimeException();
        this.networkRoot = new Node(root, convertToNode(children));
        this.subnets.add(this.networkRoot);
        allNodes.addAll(updateAllNodes(this.subnets));
        if (betterIsCircular(this.networkRoot)) throw new IllegalArgumentException("ERROR: Circular Tree");
    }

    /**
     * Creates a new NodeTree from bracket notation
     *
     * @param bracketNotation input string to create a new network
     * @throws ParseException invalid bracket notation
     */
    public Network(final String bracketNotation) throws ParseException {
        if (bracketNotation == null || bracketNotation.split(" ").length <= 1)
            throw new ParseException("Invalid bracket notation");
        networkRoot = AddressParser.bracketParser(bracketNotation);
        this.subnets.add(networkRoot);
        allNodes.addAll(updateAllNodes(subnets));
        if (betterIsCircular(networkRoot)) throw new ParseException("ERROR: Circular Tree");
    }

    private Network(List<Node> subNets) throws ParseException {
        for (Node subnet : subNets) {
            this.subnets.add(subnet.copy());
        }
        networkRoot = subnets.get(0);
        allNodes = updateAllNodes(this.subnets);
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

            List<Node> independentTrees = new ArrayList<>();
            List<Node> dependentTrees = new ArrayList<>();

            List<Node> subnets = subnetCopy.subnets;
            List<Node> subnetsUsed = new ArrayList<>();

            for (Node root : this.subnets) {
                subnets.removeAll(subnetsUsed);
                for (Node subnetNode : subnets) {
                    if (!this.subnets.contains(subnetNode)) {
                        boolean thisChanged
                            = this.connectNodes(root, subnetNode, subnetCopy, independentTrees, dependentTrees);
                        if (thisChanged) subnetsUsed.add(subnetNode);
                        changed = changed || thisChanged;
                    }
                }
            }

            this.subnets.addAll(independentTrees);
            for (Node independent : independentTrees) {
                allNodes.addAll(getAsList(independent));
                changed = true;
            }

            List<Node> used = new ArrayList<>();
            List<Node> rootUsed = new ArrayList<>();
            for (Node root : this.subnets) {
                if (!used.contains(root) && !rootUsed.contains(root)) {
                    this.subnets.forEach(
                        other -> mergeInternSubnets(root, other, rootUsed, used));
                }
            }

            this.subnets.removeAll(used);


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return changed;
    }

    /**
     * Merge subnets after extern subnets have been merged
     *
     * @param root     MainNetwork root
     * @param other    other network in main network subnets
     * @param rootUsed list of already tried merged subnets
     * @param used     list of successfully merged subnets
     */
    void mergeInternSubnets(Node root, Node other, List<Node> rootUsed, List<Node> used) {

        if (root != other && !used.contains(other) && !rootUsed.contains(other)) {
            boolean thisChanged
                = this.connectNodes(root, other, this, new ArrayList<>(), new ArrayList<>());
            if (thisChanged) used.add(other);
            if (thisChanged && !rootUsed.contains(root)) rootUsed.add(root);
        }
    }


    private boolean connectNodes(Node root, Node sub, Network subnet, List<Node> toAdd, List<Node> added) {
        IP connection = findConnection(getLevels(root.getAddress()), sub);
        if (connection != null) {
            try {
                Network subnetCopy = new Network(subnet.getSubnets());
                Node connectionSub;
                Node connectionRoot;
                if (subnet == this) {
                    connectionRoot = this.getAsNode(connection, this.subnets.indexOf(root));
                    connectionSub = subnetCopy.getAsNode(connection, this.subnets.indexOf(sub));
                    subnetCopy.betterChangeRoot(connection, null, this.subnets.indexOf(sub));
                } else {
                    connectionRoot = this.getAsNode(connection, -1);
                    connectionSub = subnetCopy.getAsNode(connection, -1);
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
     * returns a list of all IP-Addresses
     *
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
        if (ip1 == null || ip2 == null || checkIP(ip1, allNodes) || checkIP(ip2, allNodes) || ip1.compareTo(ip2) == 0)
            return false;
        Node node1 = getAsNode(ip1, -1);
        Node node2 = getAsNode(ip2, -1);
        Node netRoot = getSubnetRoot(getAsNode(ip1, -1));
        Node netCopy = netRoot.copy();
        SortedSet<Node> netNodes = getAsList(netCopy);

        if (netNodes.stream().anyMatch(x -> x.getAddress().compareTo(ip2) == 0)) return false;
        Node mergePoint = netNodes.stream().filter(x -> x.getAddress().compareTo(ip1) == 0).findFirst().get();

        if (ip2.compareTo(getSubnetRoot(node2).getAddress()) != 0) betterChangeRoot(ip2, null);
        Node oldSubnet = getSubnetRoot(node1);
        mergePoint.addChildren(List.of(node2));

        if (betterIsCircular(netCopy)) {
            return false;
        }
        node1.addChildren(List.of(node2));
        this.subnets.remove(oldSubnet);
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
        if (ip1 == null || ip2 == null || checkIP(ip1, allNodes) || checkIP(ip2, allNodes)) return false;
        Node node1 = getAsNode(ip1, -1);
        Node node2 = getAsNode(ip2, -1);

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
            this.allNodes.remove(node2);
            if (node1.getChildren().isEmpty() && node1.getParent() == null) {
                this.allNodes.remove(node1);
            }

        } else {
            node1.getChildren().remove(node2);
            node2.setParent(null);
            this.subnets.add(node2);
            if (node1.getChildren().isEmpty()) {
                this.subnets.remove(node1);
                this.allNodes.remove(node1);
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
        if (ip == null) return false;
        return !checkIP(ip, allNodes);
    }

    /**
     * Searching for the maximum depth of the tree
     *
     * @param root root ip for the network/starting point
     * @return returning the height/depth of the tree
     */
    public int getHeight(final IP root) {

        if (root == null || checkIP(root, allNodes)) return 0;
        Node rootNode = getSubnetRoot(getAsNode(root, -1));
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
        if (root == null || checkIP(root, allNodes)) return new ArrayList<>();
        Node rootNode = getSubnetRoot(getAsNode(root, -1));
        if (root.compareTo(rootNode.getAddress()) != 0) {
            betterChangeRoot(root, null);
        }
        List<List<IP>> layers = new ArrayList<>();
        layers.add(List.of(getSubnetRoot(rootNode).getAddress()));
        for (List<Node> cursor = new ArrayList<>(getSubnetRoot(rootNode).getChildren()); !cursor.isEmpty();) {
            layers.add(cursor.stream().map(Node::getAddress).sorted().collect(Collectors.toList()));
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
        }
        return layers;
    }


    /**
     * Getting a rout from one ip to another
     *
     * @param start starting point
     * @param end   destination
     * @return list of IPs which represents the rout between two IPs
     */
    public List<IP> getRoute(final IP start, final IP end) {
        if (start == null || end == null || checkIP(start, allNodes) || checkIP(end, allNodes))
            return new ArrayList<>();
        betterChangeRoot(start, null);
        List<IP> path = new ArrayList<>();
        Node destination = getAsNode(end, -1);
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
        if (root == null || checkIP(root, allNodes)) return "";
        Node rootNode = getSubnetRoot(getAsNode(root, -1));
        if (root.compareTo(rootNode.getAddress()) != 0) {
            betterChangeRoot(root, null);
        }
        return buildBracketNotation(getSubnetRoot(rootNode)).substring(1);
    }


    /**
     * Changing the root address of a subnet
     *
     * @param newRoot   new root for the network
     * @param newParent new parent for each node
     */
    public void betterChangeRoot(IP newRoot, Node newParent) {
        Node currentNode = getAsNode(newRoot, -1);
        subnets = new ArrayList<>(changeToRoot(currentNode, newParent, subnets));

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
        subnets = new ArrayList<>(changeToRoot(currentNode, newParent, List.copyOf(this.subnets)));
    }

    /**
     * Returning the associated node in a specific subnet
     *
     * @param node   an IP-Address
     * @param subnet index of subnet in subnets if index = -1 check in allNodes which contains every node in all subnets
     * @return IP-Address as its node
     */
    public Node getAsNode(IP node, int subnet) {
        if (subnet == -1)
            return allNodes.stream().filter(x -> x.getAddress().compareTo(node) == 0).findFirst().orElse(null);
        return getAsList(this.subnets.get(subnet)).stream().filter(x -> x.getAddress().compareTo(node) == 0).findFirst()
            .orElse(null);
    }


    /**
     * getting the list of subnets from a network
     *
     * @return list of subnets
     */
    public List<Node> getSubnets() {
        return List.copyOf(this.subnets);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Network net = (Network) o;
        if (this.allNodes.size() != net.allNodes.size() || this.subnets.size() != net.subnets.size()) return false;
        List<List<Integer>> listOfDegrees
            = this.subnets.stream().map(x -> getAsList(x).stream().map(Node::getDegree).sorted().collect(
            Collectors.toList())).collect(Collectors.toList());


        List<List<Integer>> listOfDegreesSecondNet
            = net.subnets.stream().map(x -> getAsList(x).stream().map(Node::getDegree).sorted().collect(
            Collectors.toList())).collect(Collectors.toList());

        listOfDegrees.retainAll(listOfDegreesSecondNet);

        return listOfDegrees.size() == listOfDegreesSecondNet.size();
    }
}
