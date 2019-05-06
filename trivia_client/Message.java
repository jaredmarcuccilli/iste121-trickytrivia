import java.io.Serializable;
/**
 * Store information about a chat message.
 *
 * @author Jared Marcuccilli
 *
 * @version 1.0.0
 */
public class Message implements Serializable {
    private String source;
    private String message;
    
    public Message(String _s, String _m) {
        source = _s;
        message = _m;
    }
    
    public String getSource() {
        return source;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String toString() {
        return source + ": " + message;
    }
}