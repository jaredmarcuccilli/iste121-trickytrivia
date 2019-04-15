import java.net.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.text.*;

public class TriviaClient extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    private JTextArea jtQues;
    private JTextField jtfChat;
    
    private String name;
    private String server;
    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;	// Object writer
    
    private JButton jb1;	//
    private JButton jb2;	// Answer
    private JButton jb3;	// Buttons
    private JButton jb4;	//
    
    private JButton jbSend;
    
    /**
     * @param args[0] Server IP
     * @param args[1] Player Name
     */
    private boolean connected = false;
    
    public static void main(String[] args) {

        new TriviaClient(args[0], args[1]);
    }
    
    public TriviaClient(String _server, String _name) {
        super("Trivia - Client");
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setSize(600, 500);
        setResizable(false);
        setVisible(true);
        
        JPanel jpMain = new JPanel(new BorderLayout());	// Separates chat and game
        JPanel jpGame = new JPanel(new BorderLayout());	// Holds the game components see GUI mockup on trello
        // JPanel jpLives = new JPanel(new FlowLayout());	// Holds lives
        JPanel jpAns = new JPanel(new GridLayout(0,1));	// Hold answer buttons
        JPanel jpQues = new JPanel(new BorderLayout());	// Holds question
        JPanel jpChat = new JPanel(new BorderLayout());	// Holds chat contents
        JPanel jpTextIn = new JPanel(new BorderLayout());	// Holds chat entry 
        
        server = _server;
        name = _name;
        
        jtQues = new JTextArea("");
            Font largeFont = new Font(Font.SANS_SERIF, Font.PLAIN, 40);
        	   jtQues.setBackground(Color.LIGHT_GRAY);
        	   jtQues.setLineWrap(true);
        	   jtQues.setWrapStyleWord(true);
        	   jtQues.setFont(largeFont);
    		   jtQues.setRows(4);
    		   jtQues.setEditable(false);
        
        jtaStream = new JTextArea();
        jtaStream.setColumns(20);
        jtaStream.setEditable(false);
        jtaStream.setWrapStyleWord(true);
        jtaStream.setLineWrap(true);
        
        jtfChat = new JTextField();
        jtfChat.addActionListener(this);
        jpTextIn.add(jtfChat);
        
        jbSend = new JButton("Send");
        jbSend.addActionListener(this);
        jpTextIn.add(jbSend, BorderLayout.EAST);
        
        jpChat.add(jtaStream, BorderLayout.CENTER);
        jpChat.add(jpTextIn, BorderLayout.SOUTH);
        
        jtaStream.append("Trivia Client starting...");
        
        addWindowListener(
            new WindowAdapter(){public void windowClosing(WindowEvent e) 
                {
                    shutdown();
                }});
        
        jb1 = new JButton("1");
            jb1.addActionListener(this);
            jpAns.add(jb1);
        jb2 = new JButton("2");
            jb2.addActionListener(this);
            jpAns.add(jb2);
        jb3 = new JButton("3");
            jb3.addActionListener(this);
            jpAns.add(jb3);
        jb4 = new JButton("4");
            jb4.addActionListener(this);
            jpAns.add(jb4);
        disableButtons();
        
    	try {
			BufferedImage lightHeart = ImageIO.read(new File("Light.png"));
			BufferedImage darkHeart = ImageIO.read(new File("Dark.png"));
			
			// for(int i = 0;i<3;i++) {jpLives.add(new JLabel(new ImageIcon(lightHeart)));}
		} catch (IOException e1) {
			//e1.printStackTrace();
			System.out.println("Couldn't read icons");
		}

        jpGame.add(jtQues, BorderLayout.NORTH); 
        // jpGame.add(jpLives, BorderLayout.SOUTH);
        jpGame.add(jpAns, BorderLayout.CENTER); 
       
        jpMain.add(jpGame, BorderLayout.CENTER);
        jpMain.add(jpChat, BorderLayout.EAST);
          
        add(jpMain);
        jpMain.repaint();
        
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
            } else if (ae.getSource() == jbSend || ae.getSource() == jtfChat) {
                // send chat message
                oos.writeObject(new Message(name, jtfChat.getText()));
                jtfChat.setText("");
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
                        jtQues.setText(q.getQuestion());
                        jb1.setText(q.getAnswer1());
                        jb2.setText(q.getAnswer2());
                        jb3.setText(q.getAnswer3());
                        jb4.setText(q.getAnswer4());
                        enableButtons();
                    } else if (in instanceof Message) {
                        Message m = (Message)in;
                        jtaStream.append("\n" + m.getSource() + ": " + m.getMessage());
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