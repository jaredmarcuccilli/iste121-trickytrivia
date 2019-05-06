import java.util.*;
/**
 * Compare Players based on score.
 *
 * @author Jared Marcuccilli
 *
 * @version 1.0.0
 */
public class PlayerComparator implements Comparator<Player> {
    public int compare(Player p1, Player p2) {
        return p2.getPlayerScore() - p1.getPlayerScore();
    }
}