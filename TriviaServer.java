import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;

public class TriviaServer extends JFrame implements ActionListener {
    private ArrayList<Thread> threads = new ArrayList<Thread>();
    private static final int PLAYERS = 4;
    
    public static void main(String[] args) {
        new TriviaServer();
    }
    
    public TriviaServer() {
        super("Trivia - Server");
        setLocationRelativeTo(null);
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