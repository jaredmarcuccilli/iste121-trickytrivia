import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class Questions implements ActionListener{
	JProgressBar jpbRemaining = new JProgressBar();
	String question = "TEST";
	String answer1 = "";
	String answer2 = "";
	String answer3 = "";
	String answer4 = "";
	JButton jbAnswer1 = new JButton("Answer 1");
	JButton jbAnswer2 = new JButton("Answer 2");
	JButton jbAnswer3 = new JButton("Answer 3");
	JButton jbAnswer4 = new JButton("Answer 4");
	int correctAnswer = -1;
	int userAnswer = -1;

	/**
	 * Constructor
	 */
	public Questions() {		

		JFrame frame = new JFrame("Jackbox Trivia");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setSize(500,500);


		//-North- Question
		JPanel jpQuestion = new JPanel();
		JLabel jlQuestionTitle = new JLabel("Question:");
		jlQuestionTitle.setFont(new Font("Serif", Font.BOLD, 36));
		JLabel jlQuestion = new JLabel("TEST QUESTION");
		jpQuestion.add(jlQuestionTitle);
		jpQuestion.add(jlQuestion);
		Dimension questionSize = new Dimension();
		questionSize.setSize(400, 50);
		//jlQuestion.setFont(new Font(24));
		jlQuestion.setPreferredSize(questionSize);
		frame.setLayout(new BorderLayout(10,10));
		frame.add(jpQuestion, BorderLayout.NORTH);		

		//-Center- Answers
		JPanel jpAnswers = new JPanel();
		jpAnswers.setLayout(new GridLayout(4,0));
		frame.add(jpAnswers, BorderLayout.CENTER);

		jpAnswers.add(jbAnswer1);
		jpAnswers.add(jbAnswer2);
		jpAnswers.add(jbAnswer3);
		jpAnswers.add(jbAnswer4);
		jbAnswer1.addActionListener(this);
		jbAnswer2.addActionListener(this);
		jbAnswer3.addActionListener(this);
		jbAnswer4.addActionListener(this);

		//-South- Time Remaining
		JPanel jpTimeRemaining = new JPanel();
		JLabel jlTimeRemainingLabel = new JLabel("Time Remaining");		

		Dimension jpbSize = new Dimension();
		jpbSize.setSize(500, 50);
		jpbRemaining.setPreferredSize(jpbSize);
		jpbRemaining.setMaximum(10000); //TIME TO ANSWER IN MILISECONDS
		jpbRemaining.setMinimum(0);
		jpbRemaining.setValue(10000);
		jpbRemaining.setStringPainted(true);
		jpTimeRemaining.setLayout(new FlowLayout());
		jpTimeRemaining.add(jlTimeRemainingLabel);
		jpTimeRemaining.add(jpbRemaining);
		frame.add(jpTimeRemaining, BorderLayout.SOUTH);
		
		//-East- Leaderboard
		JPanel jpLeaderboard = new JPanel();
		
		jpLeaderboard.setLayout(new GridLayout(4,0));
		frame.add(jpLeaderboard, BorderLayout.EAST);
		//replace this label with actual players
		JLabel jlTitle = new JLabel("Leaderboard");
		jlTitle.setFont(new Font("Serif", Font.BOLD, 36));
		JLabel jlPlayer = new JLabel("Sample Player Score: 0");
		jlPlayer.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		
		jpLeaderboard.add(jlTitle);
		jpLeaderboard.add(jlPlayer);
		
		//-West- Chat
		JPanel jpChat = new JPanel();
		JTextArea jtaChatbox = new JTextArea("sample chat messagebox");
		JLabel jlChatTitle = new JLabel("Chat");
		jlChatTitle.setFont(new Font("Serif", Font.BOLD, 36));
		JButton jbSend = new JButton("Send");
		jpChat.setLayout(new BorderLayout(5,10));
		jpChat.add(jlChatTitle, BorderLayout.NORTH);
		jpChat.add(jtaChatbox, BorderLayout.CENTER);
		jpChat.add(jbSend, BorderLayout.SOUTH);
		frame.add(jpChat, BorderLayout.WEST);
		frame.pack();
		frame.setVisible(true);


		//TEST CODE - WILL BE REMOVED ONCE CLIENT SERVER IS WORKING
		setCorrectAnswer(2);
		setAnswer1("test1");
		setAnswer2("test2");
		setAnswer3("test3");
		setAnswer4("test4");
		startTimer();
		//reset();

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Questions frame = new Questions();
	}

	class UpdateBar extends TimerTask {
		public void run() {
			if(jpbRemaining.getValue() > 0) {
				jpbRemaining.setValue(jpbRemaining.getValue() - 10);
				//changing printed value of progress bar
				jpbRemaining.setString(jpbRemaining.getValue() / 1000 + "." + (jpbRemaining.getValue() % 1000)/10 + " Seconds Remaining");
			} else {
				System.out.println("OUT OF TIME");
				jpbRemaining.setValue(0);
				jpbRemaining.setString("OUT OF TIME");
				this.cancel();
			}
		}
	}


	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jbAnswer1) {
			System.out.println("answer 1 chosen");
			disableButtons();
			jbAnswer1.setBackground(new Color(163,184, 204));
			setUserAnswer(1);
			System.out.println(checkAnswer());
		} else if (e.getSource() == jbAnswer2) {
			System.out.println("answer 2 chosen");
			disableButtons();
			jbAnswer2.setBackground(new Color(163,184, 204));
			setUserAnswer(2);
			System.out.println(checkAnswer());
		} else if (e.getSource() == jbAnswer3) {
			System.out.println("answer 3 chosen");
			disableButtons();
			jbAnswer3.setBackground(new Color(163,184, 204));
			setUserAnswer(3);
			System.out.println(checkAnswer());
		} else if (e.getSource() == jbAnswer4) {
			System.out.println("answer 4 chosen");
			disableButtons();
			jbAnswer4.setBackground(new Color(163,184, 204));
			setUserAnswer(4);
			System.out.println(checkAnswer());
		}
	}

	public void startTimer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateBar(), 0, 10);
		//END TIMER COUNTDOWN
	}

	public void disableButtons() {
		jbAnswer1.setEnabled(false);
		jbAnswer2.setEnabled(false);
		jbAnswer3.setEnabled(false);
		jbAnswer4.setEnabled(false);
	}

	public boolean checkAnswer() {
		if (userAnswer == correctAnswer) {
			return true;
		} else {
			return false;
		}
	}

	public void reset() {
		userAnswer = -1;
		jbAnswer1.setEnabled(true);
		jbAnswer2.setEnabled(true);
		jbAnswer3.setEnabled(true);
		jbAnswer4.setEnabled(true);
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
	public int getCorrectAnswer() {
		return correctAnswer;
	}
	public void setQuestion(String newQuestion) {
		question = newQuestion;
	}
	public void setAnswer1(String newAnswer1) {
		answer1 = newAnswer1;
		jbAnswer1.setText(answer1);
	}
	public void setAnswer2(String newAnswer2) {
		answer2 = newAnswer2;
		jbAnswer2.setText(answer2);
	}
	public void setAnswer3(String newAnswer3) {
		answer3 = newAnswer3;
		jbAnswer3.setText(answer3);
	}
	public void setAnswer4(String newAnswer4) {
		answer4 = newAnswer4;
		jbAnswer4.setText(answer4);
	}
	public void setCorrectAnswer(int newCorrectAnswer) {
		correctAnswer = newCorrectAnswer;
	}
	public void setUserAnswer(int selection) {
		userAnswer = selection;
	}
}
