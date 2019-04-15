import java.io.Serializable;

public class Question implements Serializable {
    private String question;
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;
    private int correctAnswer;
    
    public Question(String _q, String _a1, String _a2, String _a3, String _a4, String _cA) {
        question = _q;
        answer1 = _a1;
        answer2 = _a2;
        answer3 = _a3;
        answer4 = _a4;
        correctAnswer = Integer.parseInt(_cA);
    }
    
    public String getQuestion() {
        return question;
    }
    
    public String getAnswer1() {
        return answer1;
    }
    
    public String getAnswer2() {
        return answer2;
    }
    
    public String getAnswer3() {
        return answer3;
    }
    
    public String getAnswer4() {
        return answer4;
    }
    
    public int getCorrectAnswerNum() {
        return correctAnswer;
    }
    
    public String toString() {
        return question + "\n" + answer1 + "\n" + answer2 + "\n" + answer3 + "\n" + answer4 + "\n" + correctAnswer;
    }
}