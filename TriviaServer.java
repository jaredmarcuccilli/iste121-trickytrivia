import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;
import java.util.Timer;

public class TriviaServer extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    
    private Vector<Thread> threads = new Vector<Thread>();
    private Vector<Player> players = new Vector<Player>();
    private Vector<ObjectOutputStream> allObjectOutputStreams = new Vector<ObjectOutputStream>();
    private static final int QUESTIONS = 20; // this should be set in the gui
    private Question currentQuestion;
    private int currentQuestionNo;
    private BufferedReader questionBr = null;
    private boolean serverOpen = true;
    private boolean keepGoing = true;
    
    private JButton jbSendQuestion;
    private JButton jbStartGame;
    private JProgressBar jpbRemaining = new JProgressBar();
    
    public static void main(String[] args) {
        new TriviaServer();
    }
    
    public TriviaServer() {
        super("Trivia - Server");
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setSize(600, 400);
        setResizable(false);
        
        jtaStream = new JTextArea();
        jtaStream.setEditable(false);
        jtaStream.setLineWrap(true);
        jtaStream.setWrapStyleWord(true);
        JScrollPane jspStream = new JScrollPane(jtaStream);
        add(jspStream);
        jtaStream.append("Trivia Server starting...");
        
        Dimension jpbSize = new Dimension();
		jpbSize.setSize(500, 25);
		jpbRemaining.setPreferredSize(jpbSize);
		jpbRemaining.setMaximum(10000); //TIME TO ANSWER IN MILISECONDS
		jpbRemaining.setMinimum(0);
		jpbRemaining.setValue(10000);
		jpbRemaining.setStringPainted(true);
        add(jpbRemaining, BorderLayout.NORTH);
        
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
        
        JPanel jpSouth = new JPanel();
        jbStartGame = new JButton("Start Game!");
            jbStartGame.addActionListener(this);
            jpSouth.add(jbStartGame);
        jbSendQuestion = new JButton("Send Next Question");
            jbSendQuestion.addActionListener(this);
            //jpSouth.add(jbSendQuestion);
        add(jpSouth, BorderLayout.SOUTH);
        
        setVisible(true);
        
        try {
            FileInputStream fis = new FileInputStream("questions.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            questionBr = new BufferedReader(new InputStreamReader(bis));
            
            ServerSocket ss = new ServerSocket(16789);
            while(serverOpen) {
                jtaStream.append("\nWaiting for a client...");
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                Socket s = ss.accept();
                TriviaServerThread tst = new TriviaServerThread(s);
                threads.add(tst);
                tst.start();
                jtaStream.append("\nClient connected. Current connections: " + threads.size());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        jtaStream.append("\nStarting the game!");
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == jbSendQuestion) {
            //sendQuestion();
        } else if (ae.getSource() == jbStartGame) {
            startGame();
        }
    }
    
    public class UpdateBar extends TimerTask {
		public void run() {
            jbSendQuestion.setEnabled(false);
			if(jpbRemaining.getValue() > 0) {
				jpbRemaining.setValue(jpbRemaining.getValue() - 10);
				// Changing printed value of progress bar
				jpbRemaining.setString(jpbRemaining.getValue() / 1000 + "." + (jpbRemaining.getValue() % 1000)/10 + " Seconds Remaining");
			} else {
				jtaStream.append("\nTime's up!");
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
				jpbRemaining.setValue(0);
				jpbRemaining.setString("Time's up!");
                sendClients(currentQuestion.getCorrectAnswerNum());
                jpbRemaining.setValue(10000);
                jbSendQuestion.setEnabled(true);
                if (keepGoing == false) {
                    gameOver();
                }
				this.cancel();
			}
		}
	}
    
    public void startTimer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateBar(), 0, 10);
	}
    
    public void startGame() {
        jbStartGame.setEnabled(false);
        jtaStream.append("\nStarting game...");
        serverOpen = false;
        Timer timer = new Timer();
		timer.scheduleAtFixedRate(new sendQuestion(), 0, 15000);
    }
    
    public class TriviaServerThread extends Thread implements Serializable {
        private Socket s;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private Player thisPlayer;
        
        public TriviaServerThread(Socket _s) {
            s = _s;
        }
        
        public void run() {
            try {
                ois = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());
                allObjectOutputStreams.add(oos);
                thisPlayer = new Player((String)ois.readObject());
                players.add(thisPlayer);
                jtaStream.append("\n" + thisPlayer.getPlayerName() + " joined the server");
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                
                while (true) {
                    Object in = ois.readObject();
                    if (in instanceof Answer) {
                        Answer a = (Answer)in;
                        if (a.getPlayerAnswerNum() == currentQuestion.getCorrectAnswerNum()) {
                            // On correct answer, add score to player based on the time they answered
                            thisPlayer.addPlayerScore(jpbRemaining.getValue()/100);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + " in  " + (10 - jpbRemaining.getValue()/1000) + " seconds, which is correct. Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                        } else if (a.getPlayerAnswerNum() == 0) {
                            thisPlayer.subtractPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " didn't answer in time! Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                        } else {
                            // incorrect answer, update score
                            thisPlayer.subtractPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + ", which is incorrect. Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                        }

                    } else if (in instanceof Message) {
                        Message m = (Message)in;
                        jtaStream.append("\n" + m.toString());
                        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                        sendClients(m);
                    }
                }
            } catch (SocketException se) {
                // server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                threads.remove(this);
                players.remove(thisPlayer);
                allObjectOutputStreams.remove(oos);
            } catch (EOFException eofe) {
                // server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                threads.remove(this);
                players.remove(thisPlayer);
                allObjectOutputStreams.remove(oos);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }
    
    // Server shutdown
    public void shutdown() {
        jtaStream.append("\nServer shutting down...");
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        System.exit(0);
    }
    
    // Sent object to all clients
    public void sendClients(Object _o) {
        for (ObjectOutputStream oos : allObjectOutputStreams) {
            try {
                oos.writeObject(_o);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public void gameOver() {
        jtaStream.append("\nGame is over!");
        jpbRemaining.setString("Game is over!");
    }
    
    //public void sendQuestion() {
    public class sendQuestion extends TimerTask {
        public void run() {
            if (keepGoing) {
                currentQuestionNo++;
                currentQuestion = getNewQuestion();
                if (currentQuestion != null) {
                    sendClients(currentQuestion);
                    jtaStream.append("\n" + currentQuestionNo + ". " + currentQuestion.toString());
                    jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                } 
                startTimer();
                if (currentQuestionNo == QUESTIONS) {
                    keepGoing = false;
                }
            }
        }
    }
    
    // Return a Question object
    public Question getNewQuestion() {
        String[] questionElements = new String[6];
        
        try {
            questionElements = questionBr.readLine().split("~");
            Question q = new Question(questionElements[0], questionElements[1], questionElements[2], questionElements[3], questionElements[4], questionElements[5]);
            return q;
        } catch (FileNotFoundException fnfe) {
         
        } catch (IOException ioe) {
         
        } catch (NullPointerException npe) {
            jbSendQuestion.setEnabled(false);
            jtaStream.append("\nReached end of question file");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        }
        return null;
    }
}