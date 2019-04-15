import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;

public class TriviaServer extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    
    private Vector<Thread> threads = new Vector<Thread>();
    private Vector<Player> players = new Vector<Player>();
    private Vector<ObjectOutputStream> allObjectOutputStreams = new Vector<ObjectOutputStream>();
    private static final int PLAYERS = 2; // this should be set in the gui
    private Question currentQuestion;
    private int currentQuestionNo;
    private int answersIn;
    private BufferedReader questionBr = null;
    
    private JButton jbSendQuestion;
    
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
        
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
        
        JPanel jpSouth = new JPanel();
        jbSendQuestion = new JButton("Send Next Question");
            jbSendQuestion.addActionListener(this);
            jpSouth.add(jbSendQuestion);
        add(jpSouth, BorderLayout.SOUTH);
        
        setVisible(true);
        
        try {
            FileInputStream fis = new FileInputStream("questions.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            questionBr = new BufferedReader(new InputStreamReader(bis));
            
            ServerSocket ss = new ServerSocket(16789);
            while(threads.size() < PLAYERS) {
                jtaStream.append("\nWaiting for a client...");
                Socket s = ss.accept();
                TriviaServerThread tst = new TriviaServerThread(s);
                threads.add(tst);
                tst.start();
                jtaStream.append("\nClient connected. Current connections: " + threads.size());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        jtaStream.append("\nStarting the game!");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == jbSendQuestion) {
            currentQuestion = getNewQuestion();
            sendClients(currentQuestion);
            jtaStream.append("\n" + currentQuestion.toString()); 
        }
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
                
                while (true) {
                    Object in = ois.readObject();
                    if (in instanceof Answer) {
                        Answer a = (Answer)in;
                        answersIn++;
                        if (a.getPlayerAnswerNum() == currentQuestion.getCorrectAnswerNum()) {
                            // correct answer, update score
                            thisPlayer.addPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + ", which is correct. Their score is: " + thisPlayer.getPlayerScore());
                            oos.writeObject("You got the question right. Your score is: " + thisPlayer.getPlayerScore());
                        } else {
                            // incorrect answer, update score
                            thisPlayer.subtractPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + ", which is incorrect. Their score is: " + thisPlayer.getPlayerScore());
                            oos.writeObject("You got the question wrong. Your score is: " + thisPlayer.getPlayerScore());
                        }
                        oos.writeObject(currentQuestion.getCorrectAnswerNum());
                        
                        if (answersIn == threads.size()) {
                            // trigger move on to next question, will also happen if timer runs out
                        }
                    } else if (in instanceof Message) {
                        Message m = (Message)in;
                        jtaStream.append("\n" + m.toString());
                        sendClients(m);
                    }
                }
            } catch (SocketException se) {
                // server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
                threads.remove(this);
                players.remove(thisPlayer);
                allObjectOutputStreams.remove(oos);
            } catch (EOFException eofe) {
                // server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
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
    
    // Return a Question object
    public Question getNewQuestion() {
        String[] questionElements = new String[6];
        
        try {
            questionElements = questionBr.readLine().split(":");
        } catch (FileNotFoundException fnfe) {
         
        } catch (IOException ioe) {
         
        }
          
        Question q = new Question(questionElements[0], questionElements[1], questionElements[2], questionElements[3], questionElements[4], questionElements[5]);
        return q;
    }
}