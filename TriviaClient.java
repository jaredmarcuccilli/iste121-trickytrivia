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
    
    private String name;
    private String server;
    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    
    private boolean connected = false;
    
    public static void main(String[] args) {

        new TriviaClient(args[0], args[1]);
    }
    
    public TriviaClient(String _server, String _name) {
        super("Trivia - Client");
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setSize(500, 500);
        setVisible(true);
        
        server = _server;
        name = _name;
        
        jtaStream = new JTextArea();
        add(jtaStream);
        jtaStream.append("Trivia Client starting...");
        
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
        
        connect();
    }
    
    public void actionPerformed(ActionEvent ae) {
    
    }
    
    public class TriviaClientThread extends Thread implements Serializable {
        
        public TriviaClientThread() {
        
        }
        
        public void run() {
            try {
                oos = new ObjectOutputStream(s.getOutputStream());
                ois = new ObjectInputStream(s.getInputStream());
                connected = true;
                
                oos.writeObject(name);
                
                while (connected) {
                    Object in = ois.readObject();
                    
                    if (in instanceof Question) {
                        Question q = (Question)in;
                        jtaStream.append("\n" + q.getQuestion() + "\n");
                        jtaStream.append(q.getAnswer1() + "\n");
                        jtaStream.append(q.getAnswer2() + "\n");
                        jtaStream.append(q.getAnswer3() + "\n");
                        jtaStream.append(q.getAnswer4());
                    }
                }
            } catch (SocketException se) {
                // client lost connection to server
                System.out.println("Lost connection to server");
                shutdown();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }
    
    public void connect() {
        try {
            s = new Socket(server, 16789);
            
            TriviaClientThread tct = new TriviaClientThread();
            tct.start();

        } catch (UnknownHostException uhe) {
            jtaStream.append("\nUnknown host: " + server);
            jtaStream.append("\nPlease enter another hostname.");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        } catch (ConnectException ce) {
            jtaStream.append("\nConnection refused. (is the server running?)");
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
                System.out.println("Wasn't connected");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.exit(0);
    }
}