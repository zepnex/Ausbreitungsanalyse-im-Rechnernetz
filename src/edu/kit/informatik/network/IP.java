package edu.kit.informatik.network;


import edu.kit.informatik.exceptions.ParseException;

import java.util.regex.Pattern;

/**
 * Class which represents an IP
 *
 * @author unyrg
 * @version 1.0
 */
public class IP implements Comparable<IP> {
    private static final String VALID_ADDRESS
        = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final String CHECK_ZERO
        = "^([1-9][0-9]{0,2}|0)\\.([1-9][0-9]{0,2}|0)\\.([1-9][0-9]{0,2}|0)\\.([1-9][0-9]{0,2}|0)$";
    private static final int MAX_IP_LENGTH_BITS = 24;
    private static final int BIT_SHIFT_CONSTANT = 8;
    private static final int MAX_IP_LENGTH_BYTES = 3;
    private static final int BIT_MASK = 0xFF;
    private int ipAddress;


    /**
     * Initializes a new IP
     *
     * @param pointNotation IP-Address as String in point notation
     */
    public IP(final String pointNotation) throws ParseException {
        String[] parts = pointNotation.split("\\.");
        if (Pattern.matches(VALID_ADDRESS, pointNotation) && Pattern.matches(CHECK_ZERO, pointNotation)) {
            ipAddress = 0;
            for (int i = 0; i < parts.length; i++) {
                ipAddress += (Integer.parseInt(parts[i]) << (MAX_IP_LENGTH_BITS - (BIT_SHIFT_CONSTANT * i)));
            }
        } else {
            throw new ParseException("Error: not a valid IP address");
        }
    }

    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i <= MAX_IP_LENGTH_BYTES; i++) {
            temp.insert(0, ((ipAddress >> BIT_SHIFT_CONSTANT * i) & BIT_MASK) + ".");
        }
        return temp.substring(0, temp.length() - 1);
    }

    @Override
    public int compareTo(IP o) {

        return Integer.compareUnsigned(ipAddress, o.ipAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IP ip1 = (IP) o;

        return ipAddress == ip1.ipAddress;
    }

    @Override
    public int hashCode() {
        return ipAddress;
    }

}
