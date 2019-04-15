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
    
    private JButton jb1;
    private JButton jb2;
    private JButton jb3;
    private JButton jb4;
    
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
        jtaStream.setEditable(false);
        add(jtaStream);
        jtaStream.append("Trivia Client starting...");
        
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
        
        JPanel jpSouth = new JPanel();
        
        jb1 = new JButton("1");
            jb1.addActionListener(this);
            jpSouth.add(jb1);
        jb2 = new JButton("2");
            jb2.addActionListener(this);
            jpSouth.add(jb2);
        jb3 = new JButton("3");
            jb3.addActionListener(this);
            jpSouth.add(jb3);
        jb4 = new JButton("4");
            jb4.addActionListener(this);
            jpSouth.add(jb4);
        disableButtons();
        
        add(jpSouth, BorderLayout.SOUTH);
        
        connect();
    }
    
    public void actionPerformed(ActionEvent ae) {
        try {
            if (ae.getSource() == jb1) {
                disableButtons();
                oos.writeObject(new Answer(1));
            } else if (ae.getSource() == jb2) {
                disableButtons();
                oos.writeObject(new Answer(2));
            } else if (ae.getSource() == jb3) {
                disableButtons();
                oos.writeObject(new Answer(3));
            } else if (ae.getSource() == jb4) {
                disableButtons();
                oos.writeObject(new Answer(4));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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
                        enableButtons();
                    } else if (in instanceof Message) {
                    
                    } else if (in instanceof String) {
                        jtaStream.append("\n" + (String)in);
                    }
                    //in = ois.readObject();
                }
            } catch (SocketException se) {
                // client lost connection to server
                System.out.println("Lost connection to server");
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
            jtaStream.append("\nConnected to server.");
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
    
    public void enableButtons() {
        jb1.setEnabled(true);
        jb2.setEnabled(true);
        jb3.setEnabled(true);
        jb4.setEnabled(true);
    }
    
    public void disableButtons() {
        jb1.setEnabled(false);
        jb2.setEnabled(false);
        jb3.setEnabled(false);
        jb4.setEnabled(false);
    }    
}