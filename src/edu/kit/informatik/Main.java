package edu.kit.informatik;


import edu.kit.informatik.graph.Node;

import java.util.ArrayList;

/**
 * @author unyrg
 * @version 1.0
 */
public class Main {
    /**
     * entry point
     *
     * @param args console arguments
     */
    public static void main(String[] args) {
        try {

            Network net = new Network(
                "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))");
            net.allNodes.add(new Node(new IP("1.1.1.1"), new ArrayList<>()));
            System.out.println(net.connect(new IP("85.193.148.81"), new IP("1.1.1.1")));

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
