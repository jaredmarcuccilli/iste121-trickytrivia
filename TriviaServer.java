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
    private static final int PLAYERS = 2; // this should be set in the gui
    private Question currentQuestion;
    
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
        for (int i = 0; i < 10; i++) { // 10 questions for testing
            currentQuestion = getQuestion();
            sendClients(currentQuestion);
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
    
    }
    
    public class TriviaServerThread extends Thread implements Serializable {
        private Socket s;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private String name;
        
        public TriviaServerThread(Socket _s) {
            s = _s;
        }
        
        public void run() {
            try {
                ois = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());
                allObjectOutputStreams.add(oos);
                
                name = (String)ois.readObject();
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
        for (ObjectOutputStream oos : allObjectOutputStreams) {
            try {
                oos.writeObject(_o);
                System.out.println("Sent the following object to " + threads.size() + " clients: " + _o.toString());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    // Return a Question object
    public Question getQuestion() {
        BufferedReader br = null;
        String[] questionElements = new String[6];
        
        try {
            FileInputStream fis = new FileInputStream("questions.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            br = new BufferedReader(new InputStreamReader(bis));
            
            questionElements = br.readLine().split(":");
        } catch (FileNotFoundException fnfe) {
         
        } catch (IOException ioe) {
         
        }
          
        Question q = new Question(questionElements[0], questionElements[1], questionElements[2], questionElements[3], questionElements[4], questionElements[5]);
        return q;
    }
}