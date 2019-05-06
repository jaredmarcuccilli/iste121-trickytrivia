import java.io.Serializable;
/**
 * Store information about an Answer.
 *
 * @author Jared Marcuccilli
 *
 * @version 1.0.0
 */
public class Answer implements Serializable {
    // Correct answer, 1-4
    private int number;
    
    public Answer(int _n) {
        number = _n;
    }
    
    public int getPlayerAnswerNum() {
        return number;
    }
    
    public void setPlayerAnswerNum(int _n) {
        number = _n;
    }
}