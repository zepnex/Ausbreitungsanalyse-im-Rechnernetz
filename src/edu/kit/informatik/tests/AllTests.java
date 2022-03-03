package edu.kit.informatik.tests;

import edu.kit.informatik.ParseException;
import edu.kit.informatik.IP;
import edu.kit.informatik.Network;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static edu.kit.informatik.tests.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;


public class AllTests {
    @Test
    void simpleIP() throws ParseException {
        assertEquals(new IP("0.0.0.0").toString(), "0.0.0.0");
        assertEquals(new IP("255.255.255.255").toString(), "255.255.255.255");
    }

    @Test
    void invalidIP() {
        assertThrows(ParseException.class, () ->
            new IP("256.256.256.256"));
        assertThrows(ParseException.class, () ->
            new IP("00.00.00.001"));
        assertThrows(ParseException.class, () ->
            new IP("255.255.255.255.255"));
        assertThrows(ParseException.class, () ->
            new IP("a.a.a.a"));
        assertThrows(ParseException.class, () ->
            new IP("0.0.0.0."));
        assertThrows(ParseException.class, () ->
            new IP("..."));
    }

    @Test
    void validNetworkCreation() throws ParseException {
        IP root = new IP("141.255.1.133");
        List<List<IP>> levels = List.of(List.of(root),
            List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
        Network network = new Network(root, levels.get(1));
        assertEquals("(141.255.1.133 0.146.197.108 122.117.67.158)", network.toString(root));

        root = new IP("141.255.1.133");
        network =
            new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("(141.255.1.133 255.0.0.234 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))))",
            network.toString(root));
    }

    @Test
    void changeRoot() throws ParseException {
        List<List<IP>> levels = List.of(List.of(new IP("141.255.1.133")),
            List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
        Network network = new Network(new IP("141.255.1.133"), levels.get(1));
        assertEquals("(141.255.1.133 0.146.197.108 122.117.67.158)", network.toString(new IP("141.255.1.133")));
        assertEquals("(0.146.197.108 (141.255.1.133 122.117.67.158))", network.toString(new IP("0.146.197.108")));
        network =
            new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("(141.255.1.133 255.0.0.234 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))))",
            network.toString(new IP("141.255.1.133")));
        assertEquals("(0.0.0.0 (255.255.255.255 (45.45.45.45 (34.34.34.34 1.1.1.1)) (141.255.1.133 255.0.0.234)))",
            network.toString(new IP("0.0.0.0")));
    }

    @Test
    void invalidNetworkCreation() {
        assertThrows(ParseException.class, () -> new Network("(244.244.244.244)"));

        assertThrows(ParseException.class, () -> new Network("(244.244.244.244 256.266.256.256)"));
        assertThrows(ParseException.class, () -> new Network("(244.244.244.244 (1.1.1.1))"));
        assertThrows(ParseException.class, () -> new Network("(244.244.244.244 (1.1.1.1 244.244.244.244 0.0.0.0))"));
        assertThrows(RuntimeException.class, () -> new Network(new IP("0.0.0.0"), null));
        assertThrows(RuntimeException.class, () -> new Network(null, List.of(new IP("0.0.0.0"))));
        assertThrows(RuntimeException.class, () -> new Network(new IP("0.0.0.0"), List.of(new IP("0.0.0.0"))));
        assertThrows(RuntimeException.class,
            () -> new Network(new IP("0.0.0.0"), List.of(new IP("1.1.1.1"), new IP("2.2.2.2"), new IP("1.1.1.1"))));
        assertThrows(ParseException.class, () -> new Network("244.244.244.244 (1.1.1.1 0.0.0.0)"));
        assertThrows(ParseException.class, () -> new Network("(244.244.244.244 (1.1.1.1 0.0.0.0)"));
    }

    @Test
    void checkRoute() throws ParseException {
        Network network =
            new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("[255.255.255.255, 45.45.45.45, 34.34.34.34, 1.1.1.1]",
            network.getRoute(new IP("255.255.255.255"), new IP("1.1.1.1")).toString());
        assertEquals("[0.0.0.0, 255.255.255.255, 141.255.1.133, 255.0.0.234]",
            network.getRoute(new IP("0.0.0.0"), new IP("255.0.0.234")).toString());
    }

    @Test
    void checkList() throws ParseException {
        Network network = new Network("(141.255.1.133 0.146.197.108 122.117.67.158)");
        assertEquals("[0.146.197.108, 122.117.67.158, 141.255.1.133]", network.list().toString());
        network = new Network(
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77)))");
        assertEquals(
            "[0.146.197.108, 34.49.145.239, 39.20.222.120, 77.135.84.171, 85.193.148.81, 116.132.83.77, 122.117.67.158, 141.255.1.133, 231.189.0.127, 252.29.23.0]",
            network.list().toString());
        network =
            new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals("[0.0.0.0, 1.1.1.1, 34.34.34.34, 45.45.45.45, 141.255.1.133, 255.0.0.234, 255.255.255.255]",
            network.list().toString());
    }

    @Test
    void checkHeight() throws ParseException {
        Network network = new Network("(141.255.1.133 0.146.197.108 122.117.67.158)");
        assertEquals(1, network.getHeight(new IP("141.255.1.133")));
        assertEquals(2, network.getHeight(new IP("0.146.197.108")));
        network = new Network(
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77)))");
        assertEquals(3, network.getHeight(new IP("85.193.148.81")));
        assertEquals(4, network.getHeight(new IP("77.135.84.171")));
        network =
            new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertEquals(4, network.getHeight(new IP("141.255.1.133")));
        assertEquals(4, network.getHeight(new IP("34.34.34.34")));
    }

    @Test
    void checkContains() throws ParseException {
        Network network = new Network("(141.255.1.133 0.146.197.108 122.117.67.158)");
        assertFalse(network.contains(new IP("142.255.1.133")));
        assertTrue(network.contains(new IP("0.146.197.108")));
        network = new Network(
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77)))");
        assertFalse(network.contains(new IP("80.193.148.81")));
        assertTrue(network.contains(new IP("77.135.84.171")));
        network =
            new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertFalse(network.contains(new IP("145.255.1.133")));
        assertTrue(network.contains(new IP("34.34.34.34")));
    }

    @Test
    void checkDisconnect() throws ParseException {
        Network network = new Network("(141.255.1.133 0.146.197.108 122.117.67.158)");
        assertTrue(network.disconnect(new IP("0.146.197.108"), new IP("141.255.1.133")));
        assertFalse(network.disconnect(new IP("0.146.197.108"), new IP("141.255.1.133")));
        assertTrue(network.contains(new IP("141.255.1.133")));
        assertFalse(network.contains(new IP("0.146.197.108")));
        assertEquals("[122.117.67.158, 141.255.1.133]", network.list().toString());
        assertEquals("[[141.255.1.133], [122.117.67.158]]", network.getLevels(new IP("141.255.1.133")).toString());

        network = new Network(
            "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 (231.189.0.127 77.135.84.171 (39.20.222.120 252.29.23.0 116.132.83.77)))");
        assertTrue(network.disconnect(new IP("231.189.0.127"), new IP("39.20.222.120")));
        assertFalse(network.disconnect(new IP("85.193.148.81"), new IP("122.117.67.158")));
        assertTrue(network.contains(new IP("231.189.0.127")));
        assertTrue(network.contains(new IP("39.20.222.120")));
        assertTrue(network.contains(new IP("122.117.67.158")));
        assertEquals(
            "[0.146.197.108, 34.49.145.239, 39.20.222.120, 77.135.84.171, 85.193.148.81, 116.132.83.77, 122.117.67.158, 141.255.1.133, 231.189.0.127, 252.29.23.0]",
            network.list().toString());
        assertEquals(
            "[[85.193.148.81], [34.49.145.239, 141.255.1.133, 231.189.0.127], [0.146.197.108, 77.135.84.171, 122.117.67.158]]",
            network.getLevels(new IP("85.193.148.81")).toString());
        assertEquals("[[39.20.222.120], [116.132.83.77, 252.29.23.0]]",
            network.getLevels(new IP("39.20.222.120")).toString());
        network =
            new Network("(141.255.1.133 (255.255.255.255 0.0.0.0 (45.45.45.45 (34.34.34.34 1.1.1.1))) 255.0.0.234)");
        assertTrue(network.disconnect(new IP("34.34.34.34"), new IP("1.1.1.1")));
        assertFalse(network.disconnect(new IP("1.1.1.1"), new IP("34.34.34.34")));
        assertTrue(network.contains(new IP("34.34.34.34")));
        assertFalse(network.contains(new IP("1.1.1.1")));
        assertEquals("[0.0.0.0, 34.34.34.34, 45.45.45.45, 141.255.1.133, 255.0.0.234, 255.255.255.255]",
            network.list().toString());
        assertEquals("[[141.255.1.133], [255.0.0.234, 255.255.255.255], [0.0.0.0, 45.45.45.45], [34.34.34.34]]",
            network.getLevels(new IP("141.255.1.133")).toString());
    }

    @Test
    void exampleInteraction() throws ParseException {
        // Construct initial network
        IP root = new IP("141.255.1.133");
        List<List<IP>> levels = List.of(List.of(root),
            List.of(new IP("0.146.197.108"), new IP("122.117.67.158")));
        final Network network = new Network(root, levels.get(1));
        assertEquals("(141.255.1.133 0.146.197.108 122.117.67.158)", network.toString(root));
        assertEquals((levels.size() - 1), network.getHeight(root));
        assertEquals(List.of(List.of(root), levels.get(1)), network.getLevels(root));

        // "Change" root and call toString, getHeight and getLevels again
        root = new IP("122.117.67.158");
        levels = List.of(List.of(root), List.of(new IP("141.255.1.133")),
            List.of(new IP("0.146.197.108")));

        assertEquals("(122.117.67.158 (141.255.1.133 0.146.197.108))", network.toString(root));
        assertEquals(levels.size() - 1, network.getHeight(root));
        assertEquals(levels, network.getLevels(root));

        // Try to add circular dependency
        assertFalse(network.add(new Network("(122.117.67.158 0.146.197.108)")));

        // Merge two subnets with initial network
        assertTrue(network.add(new Network(
            "(85.193.148.81 34.49.145.239 231.189.0.127 141.255.1.133)")));
        assertTrue(network.add(new Network("(231.189.0.127 252.29.23.0"
            + " 116.132.83.77 39.20.222.120 77.135.84.171)")));

        // "Change" root and call toString, getHeight and getLevels again
        root = new IP("85.193.148.81");
        levels = List.of(List.of(root),
            List.of(new IP("34.49.145.239"), new IP("141.255.1.133"),
                new IP("231.189.0.127")),
            List.of(new IP("0.146.197.108"), new IP("39.20.222.120"),
                new IP("77.135.84.171"), new IP("116.132.83.77"),
                new IP("122.117.67.158"), new IP("252.29.23.0")));

        assertEquals("(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108"
            + " 122.117.67.158) (231.189.0.127 39.20.222.120"
            + " 77.135.84.171 116.132.83.77 252.29.23.0))", network.toString(root));

        assertEquals(levels.size() - 1, network.getHeight(root));
        assertEquals(levels, network.getLevels(root));
        assertEquals(List
            .of(new IP("141.255.1.133"), new IP("85.193.148.81"),
                new IP("231.189.0.127")), network.getRoute(new IP("141.255.1.133"),
            new IP("231.189.0.127")));

        // "Change" root and call getHeight again
        root = new IP("34.49.145.239");
        levels = List.of(List.of(root), List.of(new IP("85.193.148.81")),
            List.of(new IP("141.255.1.133"), new IP("231.189.0.127")),
            List.of(new IP("0.146.197.108"), new IP("39.20.222.120"),
                new IP("77.135.84.171"), new IP("116.132.83.77"),
                new IP("122.117.67.158"), new IP("252.29.23.0")));
        assertEquals(levels.size() - 1, network.getHeight(root));
        assertTrue(network.disconnect(new IP("85.193.148.81"),
            new IP("34.49.145.239")));
        assertEquals(List.of(new IP("0.146.197.108"), new IP("39.20.222.120"),
            new IP("77.135.84.171"), new IP("85.193.148.81"),
            new IP("116.132.83.77"), new IP("122.117.67.158"),
            new IP("141.255.1.133"), new IP("231.189.0.127"),
            new IP("252.29.23.0")), network.list());
    }

    @Test
    public void testList() throws ParseException {
        Network net = new Network("(192.168.178.1 192.168.178.0 192.168.178.15 0.0.0.0 29.65.234.123)");

        List<IP> temp = net.list();
        temp.clear();
        assertNotEquals(net.list().size(), temp.size());
        net = new Network("(1.1.1.1 0.0.0.0)");
        assertIterableEquals(ips("0.0.0.0", "1.1.1.1"), net.list());
        net = new Network(
            "(9.9.9.9 (8.8.8.8 (7.7.7.7 (6.6.6.6 (5.5.5.5 (4.4.4.4 (3.3.3.3 (2.2.2.2 (1.1.1.1 0.0.0.0)))))))))");
        assertIterableEquals(
            ips("0.0.0.0", "1.1.1.1", "2.2.2.2", "3.3.3.3", "4.4.4.4", "5.5.5.5", "6.6.6.6", "7.7.7.7", "8.8.8.8",
                "9.9.9.9"),
            net.list());
    }

    @Test
    public void testHeight() throws ParseException {
        Network med = new Network(MEDIUM_NET);
        assertEquals(0, med.getHeight(null));
        assertEquals(0, med.getHeight(ip("0.0.0.0")));
        assertEquals(4, med.getHeight(ip("90.240.18.65")));
        assertEquals(5, med.getHeight(ip("118.255.66.35")));
        assertEquals(6, med.getHeight(ip("71.130.3.224")));
        assertEquals(7, med.getHeight(ip("211.36.119.36")));
        assertEquals(8, med.getHeight(ip("166.114.94.115")));

        Network net = new Network(SMALL_NET);
        assertEquals(0, net.getHeight(null));
        assertEquals(0, net.getHeight(ip("127.0.0.1")));
        assertEquals(2, net.getHeight(ip("85.193.148.81")));
        assertEquals(3, net.getHeight(ip("141.255.1.133")));
        assertEquals(4, net.getHeight(ip("0.146.197.108")));
    }

    @Test
    public void testAdd() throws ParseException {
        Network net = new Network(SMALL_NET);
      //  assertFalse(net.add(null));
        assertFalse(net.add(new Network(SMALL_NET_SORTED)));
        assertFalse(net.add(new Network("(141.255.1.133 122.117.67.158 0.146.197.108)")));
        assertTrue(net.add(new Network("(0.0.0.0 141.255.1.133)")));
        assertEquals(SMALL_NET_EXTENDED, net.toString(ip("85.193.148.81")));
        assertFalse(net.add(new Network("(122.117.67.158 0.146.197.108 141.255.1.133 1.1.1.1)")));
        assertTrue(net.add(new Network("(141.255.1.133 122.117.67.158 0.146.197.108 2.2.2.2)")));
        assertEquals(SMALL_NET_EXTENDED_2, net.toString(ip("85.193.148.81")));
        net = new Network(SMALL_NET);
        assertTrue(net.add(new Network("(0.0.0.0 1.1.1.1)")));
        assertEquals("(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158) "
                + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))",
            net.toString(ip("85.193.148.81")));
        assertTrue(net.add(net("(1.0.0.0 1.0.0.1 1.0.0.2)")));
        assertEquals("(0.0.0.0 1.1.1.1)", net.toString(ip("0.0.0.0")));
        assertEquals("(1.0.0.0 1.0.0.1 1.0.0.2)", net.toString(ip("1.0.0.0")));
        assertTrue(net.add(net("(1.1.1.1 2.2.2.2 1.0.0.1)")));
        assertEquals("(1.1.1.1 0.0.0.0 (1.0.0.1 (1.0.0.0 1.0.0.2)) 2.2.2.2)", net.toString(ip("1.1.1.1")));
    }

    @Test
    public void testConnect() throws ParseException {
        Network net = new Network(SMALL_NET);
        net.add(net("(0.0.0.0 1.1.1.1)"));
        net.add(net("(1.0.0.1 1.0.0.2)"));
        net.add(net("(0.0.0.1 0.0.0.2 0.0.0.3 0.0.0.4)"));
       // assertFalse(() -> net.connect(null, null));
       // assertFalse(() -> net.connect(ip("0.0.0.0"), null));
       // assertFalse(() -> net.connect(null, ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("0.0.0.0"), ip("9.9.9.9")));
        assertFalse(() -> net.connect(ip("9.9.9.9"), ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("0.0.0.0"), ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("0.0.0.0"), ip("1.1.1.1")));
        assertFalse(() -> net.connect(ip("1.1.1.1"), ip("0.0.0.0")));
        assertFalse(() -> net.connect(ip("231.189.0.127"), ip("77.135.84.171")));
        assertFalse(() -> net.connect(ip("77.135.84.171"), ip("231.189.0.127")));
        assertFalse(() -> net.connect(ip("122.117.67.158"), ip("77.135.84.171")));
        assertFalse(() -> net.connect(ip("77.135.84.171"), ip("122.117.67.158")));
        assertTrue(() -> net.connect(ip("1.1.1.1"), ip("1.0.0.2")));
        assertTrue(() -> net.connect(ip("1.0.0.2"), ip("0.0.0.3")));
        assertTrue(() -> net.connect(ip("231.189.0.127"), ip("1.0.0.1")));

        assertEquals(4, net.getHeight(ip("1.0.0.1")));
        assertEquals(
            "(1.0.0.1 (1.0.0.2 (0.0.0.3 (0.0.0.1 0.0.0.2 0.0.0.4)) (1.1.1.1 0.0.0.0)) (231.189.0.127 39.20.222.120 77.135.84.171 (85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158)) 116.132.83.77 252.29.23.0))",
            net.toString(ip("1.0.0.1")));
    }

    @Test
    public void testDisconnect() throws ParseException {
        Network net = new Network("(0.0.0.0 1.1.1.1)");
        assertFalse(net.disconnect(ip("0.0.0.0"), ip("1.1.1.1")));

        Network net2 = new Network("(0.0.0.0 (1.1.1.1 2.2.2.2) 3.3.3.3 (4.4.4.4 5.5.5.5))");

        assertFalse(() -> net2.disconnect(null, null));
        assertFalse(() -> net2.disconnect(ip("0.0.0.0"), null));
        assertFalse(() -> net2.disconnect(null, ip("0.0.0.0")));
        assertFalse(() -> net2.disconnect(ip("0.0.0.0"), ip("7.7.7.7")));
        assertFalse(() -> net2.disconnect(ip("7.7.7.7"), ip("0.0.0.0")));

        assertTrue(() -> net2.disconnect(ip("0.0.0.0"), ip("1.1.1.1")));
        assertEquals("(2.2.2.2 1.1.1.1)", net2.toString(ip("2.2.2.2")));
        assertEquals("(0.0.0.0 3.3.3.3 (4.4.4.4 5.5.5.5))", net2.toString(ip("0.0.0.0")));
        assertTrue(() -> net2.disconnect(ip("1.1.1.1"), ip("2.2.2.2")));
        assertFalse(() -> net2.contains(ip("1.1.1.1")));
        assertFalse(() -> net2.contains(ip("2.2.2.2")));
        assertTrue(() -> net2.disconnect(ip("4.4.4.4"), ip("5.5.5.5")));
        assertFalse(() -> net2.contains(ip("5.5.5.5")));
        assertTrue(() -> net2.contains(ip("4.4.4.4")));
        assertEquals("(0.0.0.0 3.3.3.3 4.4.4.4)", net2.toString(ip("0.0.0.0")));
    }

    @Test
    public void testGetLevels() throws ParseException {
        Network med = new Network(SMALL_NET);
        assertIterableEquals(List.of(), med.getLevels(null));
      //  assertIterableEquals(List.of(), med.getLevels(ip("0.0.0.0")));
        assertEquals(List.of(ips("85.193.148.81"), ips("34.49.145.239", "141.255.1.133", "231.189.0.127"),
                    ips("0.146.197.108", "39.20.222.120", "77.135.84.171", "116.132.83.77", "122.117.67.158", "252.29.23.0"))
                .toString(),
            med.getLevels(ip("85.193.148.81")).toString());
        assertIterableEquals(
            List.of(ips("77.135.84.171"), ips("231.189.0.127"),
                ips("39.20.222.120", "85.193.148.81", "116.132.83.77", "252.29.23.0"),
                ips("34.49.145.239", "141.255.1.133"), ips("0.146.197.108", "122.117.67.158")),
            med.getLevels(ip("77.135.84.171")));
        assertIterableEquals(
            List.of(ips("141.255.1.133"), ips("0.146.197.108", "85.193.148.81", "122.117.67.158"),
                ips("34.49.145.239", "231.189.0.127"),
                ips("39.20.222.120", "77.135.84.171", "116.132.83.77", "252.29.23.0")),
            med.getLevels(ip("141.255.1.133")));
    }

    @Test
    public void testGetPath() throws ParseException {
        Network net = net(SMALL_NET);
        assertEquals(List.of(), net.getRoute(null, null));
        assertEquals(List.of(), net.getRoute(ip("141.255.1.133"), null));
        assertEquals(List.of(), net.getRoute(null, ip("141.255.1.133")));
        assertEquals(List.of(), net.getRoute(ip("141.255.1.133"), ip("0.0.0.0")));
        assertEquals(List.of(), net.getRoute(ip("0.0.0.0"), ip("141.255.1.133")));

        assertEquals(ips("85.193.148.81", "141.255.1.133", "122.117.67.158"),
            net.getRoute(ip("85.193.148.81"), ip("122.117.67.158")));
        assertEquals(ips("122.117.67.158", "141.255.1.133", "85.193.148.81", "231.189.0.127", "39.20.222.120"),
            net.getRoute(ip("122.117.67.158"), ip("39.20.222.120")));

        assertTrue(() -> {
            try {
                return net.add(net("(0.0.0.0 1.1.1.1 2.2.2.2 (3.3.3.3 4.4.4.4))"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        });
        assertEquals(List.of(), net.getRoute(ip("39.20.222.120"), ip("4.4.4.4")));
    }

    @Test
    public void testToString() throws ParseException {
        Network small = new Network(SMALL_NET);
        assertEquals(SMALL_NET_SORTED, small.toString(ip("85.193.148.81")));
        assertEquals(SMALL_NET_ALTERNATIVE_SORTED_2, small.toString(ip("141.255.1.133")));
        assertEquals(SMALL_NET_ALTERNATIVE_SORTED, small.toString(ip("77.135.84.171")));
        assertEquals("", small.toString(ip("0.0.0.0")));
        assertEquals("", small.toString(null));

    }

    @Test
    public void testNoSideEffectsAfterAdd() throws ParseException {
        Network network = new Network(MEDIUM_NET);
        //no overlapping nodes
        assertNoDisconnectSideEffects(network, new Network(SMALL_NET), ip("85.193.148.81"), ip("141.255.1.133"));

        //overlapping nodes
        assertNoDisconnectSideEffects(
            network,
            new Network("(116.132.83.77 1.1.1.1 2.2.2.2 (5.5.5.5 3.3.3.3))"),
            ip("116.132.83.77"),
            ip("2.2.2.2")
        );
    }

    private void assertNoDisconnectSideEffects(Network network, Network subnet, IP root, IP childToDisconnect)
        throws ParseException {
        assertTrue(network.add(subnet));
        String beforeDisconnection = network.toString(root);
        assertTrue(subnet.disconnect(root, childToDisconnect));
        assertEquals(beforeDisconnection, network.toString(root));
    }

    private Network net(String net) {
        try {
            return new Network(net);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0.0.0.0",
        "192.0.2.235",
        "255.255.7.255",
        "103.161.159.60",
        "0.0.56.234",
        "37.158.35.176",
    })
    void testValidIpParsing(String validIP) {
        IP ip = assertDoesNotThrow(() -> new IP(validIP));
        assertEquals(validIP, ip.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "127.0.0.01",
        "127.00.0.1",
        "256.1.1.1",
        "-2.1.1.1",
        "-0.0.0.0",
        "0.-0.0.0",
        "+12.0.3.0",
        "12.0.+3.0",
        "0.0.0.0 ",
        " 1.1.1.1",
        "1.1 .1.1",
        "0.0.0.0.",
        "",
        ".",
        "....",
        "d32c:12a2:6a24:5034:26d3:61e5:a58c:3066",
        "7dca:a502:9410:e14b:223d:644e:975c:7648",
        "::1",
        "::",
        "localhost",
    })
    void testInvalidIpParsing(String invalidIP) {
        assertThrows(ParseException.class, () -> new IP(invalidIP));
    }

    @Test
    void testIpCompare() throws ParseException {
        assertIpOrdering(
            "0.0.0.0",
            "0.0.0.1",
            "0.0.0.187",
            "0.0.255.0",
            "0.1.0.0",
            "0.1.0.1",
            "0.255.0.1",
            "1.0.0.0",
            "5.0.148.204",
            "25.90.225.168",
            "70.126.1.23",
            "85.104.248.225",
            "97.123.104.250",
            "100.66.0.1",
            "123.65.17.0",
            "156.48.45.40",
            "185.123.173.123",
            "212.204.174.105",
            "228.233.182.60",
            "238.182.219.0",
            "255.0.24.98",
            "255.255.255.255"
        );
    }

    private void assertIpOrdering(String... ipStrings) throws ParseException {
        List<IP> ips = new ArrayList<>(ipStrings.length);
        for (String ipString : ipStrings) {
            IP ip = new IP(ipString);
            assertEquals(ipString, ip.toString());
            ips.add(ip);
        }
        for (int i = 0; i < ips.size(); ++i) {
            IP ip = ips.get(i);
            assertEquals(ip, ip);
            assertEquals(0, ip.compareTo(ip));
            for (IP lower : ips.subList(0, i)) {
                assertNotEquals(ip, lower);
                assertTrue(lower.compareTo(ip) < 0);
            }
            for (IP higher : ips.subList(i + 1, ips.size())) {
                assertNotEquals(ip, higher);
                assertTrue(higher.compareTo(ip) > 0);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("invalidArgsProvider")
    void testInvalidArgs(IP root, List<IP> children) {
        assertThrows(RuntimeException.class, () -> new Network(root, children));
    }

    @Test
    void testListSideEffects() throws ParseException {
        List<IP> children = new ArrayList<>(ips("12.246.77.82", "181.16.150.157"));
        Network network = new Network(ip("178.132.155.48"), children);
        children.add(ip("148.9.166.201"));
        assertEquals(3, network.list().size());
        children.clear();
        assertEquals(3, network.list().size());
    }

    static List<Arguments> invalidArgsProvider() {
        IP root = ip("192.168.178.65");
        IP other = ip("13.45.198.56");
        return List.of(
            arguments(null, null),
            arguments(root, null),
            arguments(null, List.of(root, other)),
            arguments(root, List.of(root, other)),
            arguments(root, List.of(other, other))
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ",
        "$%?!§%*ÄÜÖöäü:;-_",
        "(0.0.0.0)",
        "(0.0.0.0 (1.1.1.1)",
        "(0.0.0.0 (1.1.1.1 2.2.2.2 )3.3.3.3 4.4.4.4()",
        "(0.0.0.0  1.1.1.1)",
        " (0.0.0.0 1.1.1.1)",
        "( 0.0.0.0 1.1.1.1)",
        "(0 0.0.0 1.1.1.1)",
        "(0,0.0.0 1.1.1.1)",
        "(0:0.0.0 1.1.1.1)",
        "(0:0.0.0 1.1.1.1) ",
        "(0:0.0.0 1.1.1.1))",
        "(0.0.0.0,1.1.1.1)",
        "(0.0.0.0 0.0.0.0)",
        "(1.1.1.1 0.0.0.0 0.0.0.0)",
        "(1.1.1.1 (0.0.0.0 2.2.2.2) 0.0.0.0)",
        "(0.0.0.0 (0.0.0.0 1.1.1.1)",
        "(1.1.1.1 (2.2.2.2 0.0.0.0) (3.3.3.3 0.0.0.0)",
        "(0.0.0.0 (1.1.1.1 0.0.0.0)",
        "((0.0.0.0 1.1.1.1) 2.2.2.2)",
        "👨‍👩‍👦📽🗣🥯🋿🊬😕🙁🏍🮲🷾🷣📽🳋🌨📨🴜🚠🔉🐸🂛🫠🔇🅊🯿🎒🗷😹🉰🩱🕉🪵🬴🝌🤼🁳📸🙓🄭🦉🪌🖹🔣🌂🄦🵇🠭🎈🔳🙚🠙🶇🢱🺚🮱🎐🻁🇦🱴👇🡒🝡🺎🫩🺘🁮🙠🶇🪂🃶🊸🴒",
    })
    void testInvalidArgs(String bracketNotation) {
        assertThrows(ParseException.class, () -> new Network(bracketNotation));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "(0.0.0.0 1.1.1.1 2.2.2.2)",
        SMALL_NET,
        SMALL_NET_ALTERNATIVE,
        TestUtils.SMALL_NET_ALTERNATIVE_SORTED,
        SMALL_NET_ALTERNATIVE_SORTED_2,
        TestUtils.SMALL_NET_EXTENDED,
        SMALL_NET_EXTENDED_2,
        MEDIUM_NET,
    })
    void testValidArgs(String bracketNotation) {
        assertDoesNotThrow(() -> new Network(bracketNotation));
    }
}

class TestUtils {
    public static final String SMALL_NET
        = "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239 "
        + "(231.189.0.127 77.135.84.171 39.20.222.120 252.29.23.0 116.132.83.77))";

    public static final String SMALL_NET_ALTERNATIVE
        = "(77.135.84.171 (231.189.0.127 39.20.222.120 252.29.23.0 116.132.83.77 "
        + "(85.193.148.81 (141.255.1.133 122.117.67.158 0.146.197.108) 34.49.145.239)))";

    public static final String SMALL_NET_ALTERNATIVE_SORTED
        = "(77.135.84.171 (231.189.0.127 39.20.222.120 (85.193.148.81 34.49.145.239 " +
        "(141.255.1.133 0.146.197.108 122.117.67.158)) 116.132.83.77 252.29.23.0))";

    public static final String SMALL_NET_ALTERNATIVE_SORTED_2
        = "(141.255.1.133 0.146.197.108 (85.193.148.81 34.49.145.239 " +
        "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0)) 122.117.67.158)";

    public static final String SMALL_NET_SORTED
        = "(85.193.148.81 34.49.145.239 (141.255.1.133 0.146.197.108 122.117.67.158) "
        + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))";

    public static final String SMALL_NET_EXTENDED
        = "(85.193.148.81 34.49.145.239 (141.255.1.133 0.0.0.0 0.146.197.108 122.117.67.158) "
        + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))";

    public static final String SMALL_NET_EXTENDED_2
        = "(85.193.148.81 34.49.145.239 (141.255.1.133 0.0.0.0 0.146.197.108 2.2.2.2 122.117.67.158) "
        + "(231.189.0.127 39.20.222.120 77.135.84.171 116.132.83.77 252.29.23.0))";

    public static final String MEDIUM_NET =
        "(90.240.18.65 (97.22.140.27 (193.77.65.203 206.41.6.234 (137.57.11.178 53.79.153.118 151.175.20.133 72.204.103.14) (172.217.134.246 125.151.42.40 26.135.185.104 12.104.224.21 97.32.83.116)) 27.191.109.156 (207.93.69.7 221.203.203.33 (211.36.119.36 191.214.220.219 7.33.138.146) (126.171.183.35 0.166.201.82 166.114.94.115)) 25.28.90.184) 124.214.225.52 62.116.50.162 (118.255.66.35 228.203.204.177 (71.130.3.224 (131.230.153.36 39.231.53.70 1.77.201.101 13.163.16.235) 166.101.129.76)))";

    private TestUtils() {
    }

    public static IP ip(String ip) {
        return assertDoesNotThrow(() -> new IP(ip));
    }

    public static List<IP> ips(String... ips) {
        return Arrays.stream(ips).map(TestUtils::ip).collect(Collectors.toList());

    }
}
