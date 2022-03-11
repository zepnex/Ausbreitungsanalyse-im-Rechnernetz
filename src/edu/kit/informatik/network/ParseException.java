package edu.kit.informatik.network;

/**
 * Custom exception when parsing a Network fails
 *
 * @author unyrg
 * @version 1.0
 */
public class ParseException extends Exception {
    /**
     * Constructor for ParsingException
     *
     * @param message error reason
     */
    public ParseException(String message) {
        super(message);
    }
}
