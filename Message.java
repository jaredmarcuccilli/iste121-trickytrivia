import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    
    public Message(String _m) {
        message = _m;
    }
    
    public String getMessage() {
        return message;
    }
}