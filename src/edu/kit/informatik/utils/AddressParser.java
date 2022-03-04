package edu.kit.informatik.utils;


import edu.kit.informatik.IP;
import edu.kit.informatik.ParseException;
import edu.kit.informatik.graph.Node;

import java.util.ArrayList;
import java.util.Arrays;
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
public class AddressParser {

    //private static final String REGEX = "\\s{2}|^\\s\\(|\\(\\s|^\\({2}";
    private static final String VALIDCHARACTERS = "^\\(([0-9.()]{7,15} ?)*\\)$";
    private static final String ChildrenWithoutChildren = "\\([0-9.\\[\\]\\s]*\\)";

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

        for (String chil : tempChildList) {
            if (chil.startsWith("[") && chil.endsWith("]")) {
                childList.add(bracketParserIps(children, Integer.parseInt(chil.substring(1, chil.length() - 1))));
            } else {
                childList.add(chil);
            }
        }

        return childList;
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

        System.out.println(!Pattern.compile(VALIDCHARACTERS).matcher(bracketNotation).find());
        if (!Pattern.compile(VALIDCHARACTERS).matcher(bracketNotation).find() || bracketNotation.startsWith("(("))
            throw new ParseException("Invalid bracket notation");
        if (!equalBracket(bracketNotation)) throw new ParseException("invalid bracket notation");

        Pattern pattern = Pattern.compile(ChildrenWithoutChildren);
        Dictionary<Integer, String[]> children = new Hashtable<>();
        int placeholder = 0;
        while (bracketNotation.length() > 6) {
            Matcher matcher = pattern.matcher(bracketNotation);
            while (matcher.find()) {
                String childString = matcher.group();
                String[] childList = childString.substring(1, childString.length() - 1).split(" ");
                children.put(placeholder, childList);
                bracketNotation = bracketNotation.replace(childString, "[" + placeholder + "]");
                ++placeholder;
            }
        }

        List<Object> ipTree = bracketParserIps(children, children.size() - 1);
        System.out.println(ipTree);

        return ipsCreateTree(ipTree);
    }

    /**
     *
     * @param string bracketnotation
     * @return true, false
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
