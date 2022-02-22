import java.util.List;

public class Network {
    public Network(final IP root, final List<IP> children) {
    }

    public Network(final String bracketNotation) throws ParseException {
    }

    public boolean add(final Network subnet) {
        return false;
    }

    public List<IP> list() {
        return null;
    }

    public boolean connect(final IP ip1, final IP ip2) {
        return false;
    }

    public boolean disconnect(final IP ip1, final IP ip2) {
        return false;
    }

    public boolean contains(final IP ip) {
        return false;
    }

    public int getHeight(final IP root) {
        return 0;
    }

    public List<List<IP>> getLevels(final IP root) {
        return null;
    }

    public List<IP> getRoute(final IP start, final IP end) {
        return null;
    }

    public String toString(IP root) {
        return null;
    }
}
