import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;

public class TriviaServer extends JFrame implements ActionListener {
    private Vector<Thread> threads = new Vector<Thread>();
    private Vector<ObjectOutputStream> allObjectOutputStreams = new Vector<ObjectOutputStream>();
    private static final int PLAYERS = 1; // this should be set in the gui
    private Question currentQuestion;
    private int currentQuestionNo;
    private int answersIn;
    private BufferedReader questionBr = null;
    
    public static void main(String[] args) {
        new TriviaServer();
    }
    
    public TriviaServer() {
        super("Trivia - Server");
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setSize(500, 500);
        setVisible(true);
        
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
        
        try {
            FileInputStream fis = new FileInputStream("questions.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            questionBr = new BufferedReader(new InputStreamReader(bis));
            
            ServerSocket ss = new ServerSocket(16789);
            while(threads.size() < PLAYERS) {
                System.out.println("Waiting for a client...");
                Socket s = ss.accept();
                TriviaServerThread tst = new TriviaServerThread(s);
                threads.add(tst);
                tst.start();
                System.out.println("Client connected. Current connections: " + threads.size());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        System.out.println("Starting the game!");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        //for (int i = 0; i < 10; i++) { // 10 questions for testing
            currentQuestion = getNewQuestion();
            //System.out.println(currentQuestion.toString());
            sendClients(currentQuestion);
        //}
    }
    
    public void actionPerformed(ActionEvent ae) {
    
    }
    
    public class TriviaServerThread extends Thread implements Serializable {
        private Socket s;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private String name;
        private int score;
        
        public TriviaServerThread(Socket _s) {
            s = _s;
        }
        
        public String getPlayerName() {
            return name;
        }
        
        public int getPlayerScore() {
            return score;
        }
        
        public void run() {
            try {
                ois = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());
                allObjectOutputStreams.add(oos);
                System.out.println(allObjectOutputStreams.size());
                name = (String)ois.readObject();
                System.out.println(name + " joined the server");
                
                while (true) {
                    Object in = ois.readObject();
                    if (in instanceof Answer) {
                        Answer a = (Answer)in;
                        answersIn++;
                        if (a.getPlayerAnswerNum() == currentQuestion.getCorrectAnswerNum()) {
                            // update score
                        }
                        if (answersIn == threads.size()) {
                            // trigger move on to next question, will also happen if timer runs out
                        }
                    } else if (in instanceof Message) {
                    
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }
    
    // Server shutdown
    public void shutdown() {
        System.out.println("Server shutting down...");
        System.exit(0);
    }
    
    // Sent object to all clients
    public void sendClients(Object _o) {
        System.out.println(allObjectOutputStreams.size());
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