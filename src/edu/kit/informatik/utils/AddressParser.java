package edu.kit.informatik.utils;


import edu.kit.informatik.IP;
import edu.kit.informatik.ParseException;
import edu.kit.informatik.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Util class for all Parser operations
 *
 * @author unyrg
 * @version 1.0
 */
public class AddressParser {

   // private static final String REGEX = "\\(\\s|\\s\\)|(\\s){2}";


    /**
     * Converts bracket notation to list of sublist and IP-Addresses
     *
     * @param bracketNotation network in bracket notation
     * @return List of IP-Addresses as string and sublist
     */
    public static List<Object> bracketParserIps(String bracketNotation) {
        bracketNotation = bracketNotation.substring(1, bracketNotation.length() - 1);
        // Regex out of Hell
        String regexSearchChild = "(\\(([0-9.\\s]+(\\([0-9.\\s()]*\\)*[0-9().\\s]+)*)\\)|[0-9.]+)";
        Pattern pattern = Pattern.compile(regexSearchChild);
        Matcher matcher = pattern.matcher(bracketNotation);
        List<String> tempMatches = new ArrayList<>();
        while (matcher.find()) {
            tempMatches.add(matcher.group());
        }
        List<Object> ipTree = new ArrayList<>();

        for (int i = 0; i < tempMatches.size(); i++) {
            String match = tempMatches.get(i);
            if (match.startsWith("(")) ipTree.add(i, bracketParserIps(match));
            else ipTree.add(i, match);
        }
        return ipTree;
    }

    /**
     * Converts list to a Node-tree
     *
     * @param ipStringTree List of IP-Addresses as string and sublist
     * @return Node with its children
     * @throws ParseException s
     */
    public static Node ipsCreateTree(List<Object> ipStringTree) throws ParseException {
        List<Object> childrenString = ipStringTree.subList(1, ipStringTree.size());
        List<Node> children = new ArrayList<>();
        for (Object o : childrenString) {
            if (o.getClass().getSimpleName().equals("ArrayList")) {
                children.add(ipsCreateTree((List<Object>) o));
            } else {
                children.add(new Node(new IP((String) o), new ArrayList<>()));
            }
        }
        return new Node(new IP((String) ipStringTree.get(0)), children);
    }

    /**
     * @param bracketNotation
     * @return List of node which
     * @throws ParseException
     */
    public static Node bracketParser(String bracketNotation) throws ParseException {
//        if (Pattern.compile(REGEX).matcher(bracketNotation).find())
//            throw new ParseException("Invalid bracket notation");
        List<Object> ipTree = bracketParserIps(bracketNotation);

        return ipsCreateTree(ipTree);

    }
}
