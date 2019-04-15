import java.io.Serializable;

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
}