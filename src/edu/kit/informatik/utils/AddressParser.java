package edu.kit.informatik.utils;


import edu.kit.informatik.network.IP;
import edu.kit.informatik.network.ParseException;
import edu.kit.informatik.graph.Node;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Dictionary;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Util class for all Parser operations
 *
 * @author unyrg
 * @version 1.0
 */
public final class AddressParser {

    private static final String VALID_CHARACTERS = "^\\(([0-9.()]{7,15} ?)*\\)$";
    private static final String CHILDREN_WITHOUT_CHILDREN = "\\([0-9.\\[\\]\\s]*\\)";
    private static final int MINIMUM_IP_LENGHT = 6;

    private AddressParser() {

    }

    /**
     * Converts bracket notation to list of sublist and IP-Addresses
     *
     * @param children Dictionary with placeholder and their IP String Arrays
     * @param child    placeholder ID
     * @return List of IP-Addresses as string and sublist
     * @throws ParseException s
     */
    public static List<Object> bracketParserIps(Dictionary<Integer, String[]> children, int child)
        throws ParseException {
        String[] tempChildList = children.get(child);
        if (tempChildList.length < 2) throw new ParseException("invalid bracket notation");
        List<Object> childList = new ArrayList<>();

        for (String tempChild : tempChildList) {
            if (tempChild.startsWith("[") && tempChild.endsWith("]")) {
                childList.add(
                    bracketParserIps(children, Integer.parseInt(tempChild.substring(1, tempChild.length() - 1))));
            } else {
                childList.add(tempChild);
            }
        }

        return childList;
    }

    /**
     * Converts list to a Node-tree
     *
     * @param ipStringTree List of IP-Addresses as string and sublist
     * @return Node with its children
     * @throws ParseException throws exception when parsing fails
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
     * Methode to parse a String to a network
     *
     * @param bracketNotation network in bracket notation
     * @return List of node which
     * @throws ParseException if something is wrong in the bracket notation
     */
    public static Node bracketParser(String bracketNotation) throws ParseException {
        String notation = bracketNotation;
        if (!Pattern.compile(VALID_CHARACTERS).matcher(notation).find() || notation.startsWith("(("))
            throw new ParseException("Invalid bracket notation");
        if (!equalBracket(notation)) throw new ParseException("invalid bracket notation");

        Pattern pattern = Pattern.compile(CHILDREN_WITHOUT_CHILDREN);
        Dictionary<Integer, String[]> children = new Hashtable<>();
        int placeholder = 0;
        while (notation.length() > MINIMUM_IP_LENGHT) {
            Matcher matcher = pattern.matcher(notation);
            while (matcher.find()) {
                String childString = matcher.group();
                String[] childList = childString.substring(1, childString.length() - 1).split(" ");
                children.put(placeholder, childList);
                notation = notation.replace(childString, "[" + placeholder + "]");
                ++placeholder;
            }
        }

        List<Object> ipTree = bracketParserIps(children, children.size() - 1);

        return ipsCreateTree(ipTree);
    }

    /**
     * Check if there is and equal amount of opening and cling bracket
     *
     * @param string bracket notation
     * @return boolean depending on if there is an equal amount of closing and opening brackets
     */
    public static boolean equalBracket(String string) {
        String brackets = string.replaceAll("[0-9. ]", "");
        int cnt = 0;
        for (char character : brackets.toCharArray()) {
            if (character == '(') cnt++;
            if (character == ')') cnt--;

        }
        return cnt == 0;
    }
}
