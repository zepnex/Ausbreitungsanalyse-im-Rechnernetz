package edu.kit.informatik.graph;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphRules {

    //TODO: implement a variety of checks to make sure the graph is correctly

    public static boolean isCircular(Node root, Set<Node> allNodes) {
        long edges = 0;
        for (List<Node> cursor = new ArrayList<>(root.getChildren()); !cursor.isEmpty(); ) {
            edges += (cursor.stream().map(Node::getAddress).count());
            cursor = cursor.stream().map(Node::getChildren).flatMap(List::stream).collect(Collectors.toList());
            if (edges > allNodes.size())
                return true;
        }
      //  System.out.println(edges + " | " + allNodes.size());

        return edges == allNodes.size();
    }
}
