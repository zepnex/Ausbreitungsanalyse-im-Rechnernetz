package edu.kit.informatik;

import java.util.List;


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
//            IP root = new IP("141.255.1.133");
//            List<List<IP>> levels = List.of(List.of(root),
//                List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
//            final Network network = new Network(root, levels.get(1));
//            System.out.println(network.toString(root));
//            System.out.println((levels.size() - 1) == network.getHeight(root));
//            System.out.println(List.of(List.of(root), levels.get(1)).equals(network.getLevels(root)));
//
//            root = new IP("122.117.67.158");
//            levels = List.of(List.of(root), List.of(new IP("141.255.1.133")),
//                List.of(new IP("0.146.197.108")));
//
//
//            System.out.println("(122.117.67.158 (141.255.1.133 0.146.197.108))"
//                .equals(network.toString(root)));
//            System.out.println((levels.size() - 1) == network.getHeight(root));
//            System.out.println(levels.equals(network.getLevels(root)));

            Network network = new Network(
                "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))");

            System.out.println(network.add(new Network("(141.255.1.133 0.0.0.0)")));
            System.out.println("jesus");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
