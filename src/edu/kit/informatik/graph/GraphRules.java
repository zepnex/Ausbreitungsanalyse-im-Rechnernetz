package edu.kit.informatik.graph;



import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author unyrg
 * @version 1.0
 */
public class GraphRules {

    //TODO: implement a variety of checks to make sure the graph is correctly

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
}
