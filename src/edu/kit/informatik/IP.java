package edu.kit.informatik;


import java.util.regex.Pattern;

/**
 * @author unyrg
 * @version 1.0
 */
public class IP implements Comparable<IP> {
    private static final String VALIDADDRESS
        = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final String CHECKZERO = "0{2}\\.";
    private int ipAddress;


    /**
     * Initializes a new IP
     *
     * @param pointNotation IP-Address as String in point notation
     */
    public IP(final String pointNotation) throws ParseException {
        String[] parts = pointNotation.split("\\.");
        if (Pattern.matches(VALIDADDRESS, pointNotation) && !Pattern.compile(CHECKZERO).matcher(pointNotation).find()) {
            ipAddress = 0;
            for (int i = 0; i < parts.length; i++) {
                ipAddress += (Integer.parseInt(parts[i]) << (24 - (8 * i)));
            }
        } else {
            throw new ParseException("Error: not a valid IP address");
        }
        //System.out.println(ipAddress);
    }

    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            temp.insert(0, ((ipAddress >> 8 * i) & 0xFF) + ".");
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
