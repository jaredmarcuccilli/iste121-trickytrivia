import java.io.Serializable;

public class Answer implements Serializable {
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