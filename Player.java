import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private int score;
    
    public Player(String _name) {
        name = _name;
    }
     
    public String getPlayerName() {
        return name;
    }
    
    public int getPlayerScore() {
        return score;
    }
    
    public void addPlayerScore(int x) {
        score += x;
    }
    
    public void subtractPlayerScore(int x) {
        score -= x;    
    }
}//