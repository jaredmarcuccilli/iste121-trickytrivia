import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;

public class TriviaClient extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    
    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    
    private boolean connected = false;
    
    public static void main(String[] args) {
        new TriviaClient();
    }
    
    public TriviaClient() {
        super("Trivia - Client");
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setSize(500, 500);
        
        JFrame test = new JFrame("Connect");
        test.setSize(400, 400);
        test.setVisible(true);
        
        setVisible(true);
        
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
    }
    
    public void actionPerformed(ActionEvent ae) {
    
    }
    
    public class TriviaClientThread extends Thread implements Serializable {
        
        public TriviaClientThread() {
        
        }
        
        public void run() {
            try {
                ois = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());
                connected = true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    public void connect(String _server) {
        try {
            s = new Socket(_server, 16789);
            
            TriviaClientThread tct = new TriviaClientThread();
            tct.start();

        } catch (UnknownHostException uhe) {
            jtaStream.append("\n\nUnknown host: " + _server);
            jtaStream.append("\nPlease enter another hostname.");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        } catch (ConnectException ce) {
            jtaStream.append("\n\nConnection refused. (is the server running?)");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public void shutdown() {
        System.out.println("Client shutting down...");
        try {
            if (connected) {
                s.close();
                ois.close();
                oos.close();
            } else {
                System.out.println("\tWasn't connected");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.exit(0);
    }
}