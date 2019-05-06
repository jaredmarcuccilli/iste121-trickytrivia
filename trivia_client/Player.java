import java.io.Serializable;
/**
 * Store information about a Player.
 *
 * @author Jared Marcuccilli
 *
 * @version 1.0.0
 */
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
}