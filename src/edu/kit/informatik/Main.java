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
            IP root = new IP("141.255.1.133");
            List<List<IP>> levels = List.of(List.of(root),
                List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
            final Network network = new Network(root, levels.get(1));
            System.out.println(network.toString(root));
            System.out.println((levels.size() - 1) == network.getHeight(root));
            System.out.println(List.of(List.of(root), levels.get(1)).equals(network.getLevels(root)));

            root = new IP("122.117.67.158");
            levels = List.of(List.of(root), List.of(new IP("141.255.1.133")),
                List.of(new IP("0.146.197.108")));


            System.out.println("(122.117.67.158 (141.255.1.133 0.146.197.108))"
                .equals(network.toString(root)));
            System.out.println((levels.size() - 1) == network.getHeight(root));
            System.out.println(levels.equals(network.getLevels(root)));




        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
