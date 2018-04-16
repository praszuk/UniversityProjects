import java.io.Serializable;

/**
 * Responsible for keeping Agent data.
 */
public class Contact implements Serializable{
    private String ip;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    private int port;

    public Contact(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        if (port != contact.port) return false;
        return ip != null ? ip.equals(contact.ip) : contact.ip == null;
    }

    @Override
    public int hashCode() {
        int result = ip != null ? ip.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}
