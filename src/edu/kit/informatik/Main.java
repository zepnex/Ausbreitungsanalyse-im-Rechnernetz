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
            Network test;
//            System.out.println(new IP("123.1.254.123").toString());
//            // TODO: should throw error when space before closing bracket\
//            System.out.println("==========check circularity==========");
//            test = new Network(
//                "(1.1.1.1 (0.0.0.0 1.1.1.1))");
//            System.out.println(test.list());
//            System.out.println(test.toString(new IP("1.1.1.1")));
//            System.out.println();

            System.out.println("==========check circularity==========");
            test = new Network(
                "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77) 1.1.1.1))");
            System.out.println(test.toString(new IP("85.193.148.81")));
            //Change root
            test.changeRoot(new IP("39.20.222.120"), null);
            System.out.println("==========check network to string==========");
            System.out.println(test.toString(new IP("34.49.145.239")));
            System.out.println();

            System.out.println("==========getLevels methode==========");
            List<List<IP>> list = test.getLevels(new IP("85.193.148.81"));
            for (List<IP> listInsideList : list) {
                for (IP address : listInsideList) {
                    System.out.println(address.toString());
                }
                System.out.println("------------");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
