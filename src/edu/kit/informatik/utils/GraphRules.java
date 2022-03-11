package edu.kit.informatik.utils;


import edu.kit.informatik.network.IP;
import edu.kit.informatik.graph.Node;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * Checking if a graph is correctly implemented
 *
 * @author unyrg
 * @version 1.0
 */
public final class GraphRules {

    private GraphRules() {

    }

    /**
     * Checking if tree is circular
     *
     * @param root root address
     * @return true if tree is circular, false if not
     */

    public static boolean betterIsCircular(Node root) {
        Set<Node> allNodes = new TreeSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (allNodes.contains(node)) {
                return true;
            }
            queue.addAll(node.getChildren());
            allNodes.add(node);
        }
        return false;
    }

    /**
     * checking if an IP is existing
     *
     * @param root     the Ip you want to check
     * @param allNodes all nodes of the network
     * @return boolean, depending on if IP exists or not
     */
    public static boolean checkIP(IP root, Set<Node> allNodes) {
        return allNodes.stream().noneMatch(x -> x.getAddress().compareTo(root) == 0);
    }


}
