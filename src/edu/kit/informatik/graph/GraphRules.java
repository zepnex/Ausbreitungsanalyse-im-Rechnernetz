package edu.kit.informatik.graph;


import edu.kit.informatik.IP;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class GraphRules {

    //TODO: implement a variety of checks to make sure the graph is correctly

    /**
     * Checking if tree is circular
     *
     * @param root     root address
     * @param allNodes list of all nodes
     * @return true if tree is circular, false if not
     */
//    public static boolean isCircular(Node root, Set<Node> allNodes) {
//        long edges = 0;
//        for (List<Node> cursor = new ArrayList<>(root.getChildren()); !cursor.isEmpty(); ) {
//            //   System.out.println(cursor.stream().map(Node::getAddress).collect(Collectors.toList()));
//            edges += (cursor.stream().map(Node::getAddress).count());
//            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
//            if (edges > allNodes.size())
//                return true;
//        }
//        //  System.out.println(edges + " | " + allNodes.size());
//
//        return edges == allNodes.size();
//    }

//    public static boolean idk(Node root){
//
//        return true;
//    }
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
