import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;
import java.util.Timer;
/**
 * Trivia game client.
 *
 * @author Jake Christoforo
 * @author Colin Halter
 * @author Jared Marcuccilli
 * @author Mark Weathersby
 *  
 * @version 1.0.0
 */
public class TriviaClient extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    private JTextArea jtQues;
    private JTextField jtfChat;
    private int chosenAnswer;
    private boolean answered = false;

    private String name;
    private Socket s;
    private ObjectInputStream ois;
    private ObjectOutputStream oos; // Object writer

    private JButton jb1; //
    private JButton jb2; // Answer
    private JButton jb3; // Buttons
    private JButton jb4; //

    private JButton jbSend;
    private JProgressBar jpbRemaining = new JProgressBar();
    private JMenuItem mItemExit = null;
    private JMenuItem mItemAbout = null;
    private JMenuItem mItemHelp = null;

    private boolean connected = false;

    /**
     * Create a new TriviaClient.
     */
    public static void main(String[] args) {
        new TriviaClient();
    }
    
    /**
     * Create GUI elements.
     * Input information to connect to server.
     */
    public TriviaClient() {
        // Create initial gui
        super("Tricky Trivia - Client");
        setLayout(new BorderLayout());
        setSize(600, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // Create different JPanels
        JPanel jpMain = new JPanel(new BorderLayout());     // Separates chat and game
        JPanel jpGame = new JPanel(new BorderLayout());     // Holds the game components
        JPanel jpAns = new JPanel(new GridLayout(0, 1));    // Hold answer buttons
        JPanel jpChat = new JPanel(new BorderLayout());     // Holds chat contents
        JPanel jpTextIn = new JPanel(new BorderLayout());   // Holds chat entry
        
        // Create time remaining bar
        Dimension jpbSize = new Dimension();
		jpbSize.setSize(500, 25);
		jpbRemaining.setPreferredSize(jpbSize);
		jpbRemaining.setMaximum(10000); // Time to answer in millis
		jpbRemaining.setMinimum(0);
		jpbRemaining.setValue(10000);
		jpbRemaining.setStringPainted(true);
        jpGame.add(jpbRemaining, BorderLayout.NORTH);
        
        // Create text area that holds question
        jtQues = new JTextArea("");
        Font largeFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
        jtQues.setBackground(Color.LIGHT_GRAY);
        jtQues.setLineWrap(true);
        jtQues.setWrapStyleWord(true);
        jtQues.setFont(largeFont);
        jtQues.setRows(4);
        jtQues.setEditable(false);
        
        // Create chat text area
        jtaStream = new JTextArea();
        jtaStream.setColumns(20);
        jtaStream.setEditable(false);
        jtaStream.setWrapStyleWord(true);
        jtaStream.setLineWrap(true);
        JScrollPane jspStream = new JScrollPane(jtaStream);
        
        // Add chat text area and text input to JPanel
        jpChat.add(jspStream, BorderLayout.CENTER);
        jpChat.add(jpTextIn, BorderLayout.SOUTH);
        
        // Chat input text field
        jtfChat = new JTextField();
        jtfChat.addActionListener(this);
        jpTextIn.add(jtfChat);
        
        // Chat send button
        jbSend = new JButton("Send");
        jbSend.setEnabled(false);
        jbSend.addActionListener(this);
        jpTextIn.add(jbSend, BorderLayout.EAST);

        jtaStream.append("Trivia Client starting...");
        
        // Menu bar ->
        JMenuBar mBar = new JMenuBar();
		setJMenuBar(mBar);
		JMenu mGame = new JMenu("Game");
		mGame.setMnemonic('G');
        mBar.add(mGame);
        
        JMenu mHelp = new JMenu("About");
        mHelp.setMnemonic('A');
        mBar.add(mHelp);
        
		mItemExit = new JMenuItem("Exit");
        mItemExit.setMnemonic('x');
        mItemExit.addActionListener(this);
        
        mItemAbout = new JMenuItem("About");
        mItemAbout.setMnemonic('b');
        mItemAbout.addActionListener(this);
        
        mItemHelp = new JMenuItem("Help");
        mItemHelp.setMnemonic('h');
        mItemHelp.addActionListener(this);

		mGame.add(mItemExit);
        mHelp.add(mItemAbout);
		mHelp.add(mItemHelp);
        // <- Menu bar
        
        // Add window listener to shutdown on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
        
        // Text field for startup box
        JTextField username = new JTextField();
        JTextField server = new JTextField();
        
        Object[] message = {
                "Welcome to Tricky Trivia!",
                "Username:", username,
                "Server IP:", server
        };
    
        int option = JOptionPane.showConfirmDialog(null, message, "Tricky Trivia - Connect", JOptionPane.OK_CANCEL_OPTION);
        boolean inputVerify = true;
        while (inputVerify) {
            if (option == JOptionPane.OK_OPTION) { // If "ok" pressed, verify that a username has been input
                name = username.getText();
                if (name.length() > 0) {
                    inputVerify = false;
                } else {
                    Object[] messageNeedUsername = {
                        "Welcome to Tricky Trivia!",
                        "Please enter a username.",
                        "Username:", username,
                        "Server IP:", server
                    };
                    option = JOptionPane.showConfirmDialog(null, messageNeedUsername, "Tricky Trivia - Connect", JOptionPane.OK_CANCEL_OPTION);
                }
            } else { // If "cancel" or red X pressed, exit program
                System.exit(0);
            }
        }
        
        // Add answer JButtons
        jb1 = new JButton("1");
        jb1.addActionListener(this);
        jb1.setOpaque(true);
        jpAns.add(jb1);
        jb2 = new JButton("2");
        jb2.setOpaque(true);
        jb2.addActionListener(this);
        jpAns.add(jb2);
        jb3 = new JButton("3");
        jb3.setOpaque(true);
        jb3.addActionListener(this);
        jpAns.add(jb3);
        jb4 = new JButton("4");
        jb4.setOpaque(true);
        jb4.addActionListener(this);
        jpAns.add(jb4);
        disableButtons(); // Buttons start out disabled
        
        // Add question and answer areas to game JPanel
        jpGame.add(jtQues, BorderLayout.CENTER);
        jpGame.add(jpAns, BorderLayout.SOUTH);
        
        // Add game and chat areas to main JPanel
        jpMain.add(jpGame, BorderLayout.CENTER);
        jpMain.add(jpChat, BorderLayout.EAST);
        
        add(jpMain);
        setVisible(true);
        connect(server.getText()); // Connect using server from input box
    }
    
    /**
     * Perform action based on ActionEvents.
     * @param ActionEvent
     */
    public void actionPerformed(ActionEvent ae) {
        try {
            if (ae.getSource() == jb1) { // Answer 1
                chosenAnswer = 1;
                jb1.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(1)); // Send Answer object to server
                answered = true;
            } else if (ae.getSource() == jb2) { // Answer 2
                chosenAnswer = 2;
                jb2.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(2));
                answered = true;
            } else if (ae.getSource() == jb3) { // Answer 3
                chosenAnswer = 3;
                jb3.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(3));
                answered = true;
            } else if (ae.getSource() == jb4) { // Answer 4
                chosenAnswer = 4;
                jb4.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(4));
                answered = true;
            } else if (ae.getSource() == jbSend || ae.getSource() == jtfChat) { // Chat send button, or "enter" pressed inside text field
                oos.writeObject(new Message(name, jtfChat.getText())); // Send Message object to server
                jtfChat.setText(""); // Clear text field
            } else if (ae.getSource() == mItemExit) { // Menu bar "exit"
                shutdown();
            } else if (ae.getSource() == mItemAbout) { // Menu bar "about"
                JFrame frame = new JFrame();
                JOptionPane.showMessageDialog(null, "Tricky Trivia\nCreated for Prof. Patric's ISTE-121\nJake Christoforo\nColin Halter\nJared Marcuccilli\nMark Weathersby", "About Tricky Trivia", JOptionPane.INFORMATION_MESSAGE);
            } else if (ae.getSource() == mItemHelp) {
                JFrame frame = new JFrame();
                JOptionPane.showMessageDialog(null, "Once the game operator starts the game, you will receive a question every 15 seconds.\nYou will have 10 seconds to answer the question.\nSelect the correct answer as fast as possible to win points!\nChat with other players using the chat window!\nTo whisper a message to another player, type /whisper followed by their username and your message.", "Help", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /** 
     * Thread for client communication with server.
     */
    public class TriviaClientThread extends Thread implements Serializable {
        
        /**
         * Run thread.
         */
        public void run() {
            try {
                // Creat object io streams
                oos = new ObjectOutputStream(s.getOutputStream());
                ois = new ObjectInputStream(s.getInputStream());
                connected = true;
                jbSend.setEnabled(true);

                oos.writeObject(name); // Send username to server first

                while (connected) {
                    Object in = ois.readObject(); // Constantly be ready to read object

                    if (in instanceof Question) { // If a Question...
                        startTimer();
                        enableButtons();
                        answered = false;
                        Question q = (Question) in;
                        jtQues.setText(q.getQuestion());
                        jb1.setText(q.getAnswer1());
                        jb2.setText(q.getAnswer2());
                        jb3.setText(q.getAnswer3());
                        jb4.setText(q.getAnswer4());
                        enableButtons();
                        
                        jb1.setBackground(null);
                        jb2.setBackground(null);
                        jb3.setBackground(null);
                        jb4.setBackground(null);
                        
                    } else if (in instanceof Message) { // If a Message...
                        Message m = (Message)in;
                        String[] messageSplit = m.getMessage().split(" ");
                        if (messageSplit[0].equals("/whisper")) { // If /whisper command, split out message
                            String msg = m.getMessage().substring(m.getMessage().indexOf(" ") + 1);
                            msg = msg.substring(msg.indexOf(" ") + 1);
                            jtaStream.append("\n" + m.getSource() + " whispers to you: " + msg);
                        } else { // Otherwise write the whole message
                            jtaStream.append("\n" + m.getSource() + ": " + m.getMessage());
                        }
                        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                    } else if (in instanceof String) { // If a String, just append to chat text area
                        jtaStream.append("\n" + (String) in);
                        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                    } else if (in instanceof Integer) { // If an Integer...
                        int correctAnswer = (Integer)in; // Integer received is the correct answer
                        disableButtons(); // Disable the buttons since time is up
                        switch (chosenAnswer) { // Set the button of the chosen answer to red
                            case 1:
                                jb1.setBackground(Color.RED);
                                jb1.setOpaque(true);
                                repaint();
                                break;
                            case 2:
                                jb2.setBackground(Color.RED);
                                jb2.setOpaque(true);
                                repaint();
                                break;
                            case 3:
                                jb3.setBackground(Color.RED);
                                jb3.setOpaque(true);
                                repaint();
                                break;
                            case 4:
                                jb4.setBackground(Color.RED);
                                jb4.setOpaque(true);
                                repaint();
                                break;
                            default:
                                break;
                        }

                        switch (correctAnswer) { // Set the button of the correct answer to green. If the correct answer was chosen, this will overwrite the red with green.
                            case 1:
                                jb1.setBackground(Color.GREEN);
                                jb1.setOpaque(true);
                                repaint();
                                break;
                            case 2:
                                jb2.setBackground(Color.GREEN);
                                jb2.setOpaque(true);
                                repaint();
                                break;
                            case 3:
                                jb3.setBackground(Color.GREEN);
                                jb3.setOpaque(true);
                                repaint();
                                break;
                            case 4:
                                jb4.setBackground(Color.GREEN);
                                jb4.setOpaque(true);
                                repaint();
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (SocketException se) { // Client lost connection to server
                jtaStream.append("\nLost connection to the server!");
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                disableButtons();
                jbSend.setEnabled(false);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }
    
    /**
     * Timer bar.
     * Runs for 10 seconds. Once time is up, display "Time's Up!"
     */
    public class UpdateBar extends TimerTask {
		public void run() {
			if(jpbRemaining.getValue() > 0) { // If time is left...
				jpbRemaining.setValue(jpbRemaining.getValue() - 10); // Changing printed value of progress bar
				jpbRemaining.setString(jpbRemaining.getValue() / 1000 + "." + (jpbRemaining.getValue() % 1000)/10 + " Seconds Remaining");
			} else { // If time is up...
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
				jpbRemaining.setValue(0);
				jpbRemaining.setString("Time's up!");
                jpbRemaining.setValue(10000);
                if (!answered) { // If player didn't answer in time, send an Answer of 0 to the server. This will always be incorrect.
                    try {
                        oos.writeObject(new Answer(0));
                        chosenAnswer = 0;
                    } catch (SocketException se) {
                        // Server abruptly closed connection.
                        disableButtons();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
				this.cancel();
			}
		}
	}
    
    /**
     * Create the timer bar and update it.
     */
    public void startTimer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateBar(), 0, 10);
	}
    
    /**
     * Connect to the server.
     * @param String Server IP to connect to
     */
    public void connect(String _server) {
        try {
            s = new Socket(_server, 16789);
            TriviaClientThread tct = new TriviaClientThread(); // Create a client thread
            tct.start();
            jtaStream.append("\nConnected to server.");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        } catch (UnknownHostException uhe) {
            jtaStream.append("\nUnknown host: " + _server);
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
    
    /**
     * Shutdown client.
     */
    public void shutdown() {
        try {
            if (connected) { // Close io streams
                s.close();
                ois.close();
                oos.close();
            }
        } catch (SocketException se) {
            // Server abruptly closed connection.
            disableButtons();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.exit(0);
    }
    
    /**
     * Enable answer choice buttons.
     */
    public void enableButtons() {
        jb1.setEnabled(true);
        jb2.setEnabled(true);
        jb3.setEnabled(true);
        jb4.setEnabled(true);
    }
    
    /**
     * Disable answer choice buttons.
     */
    public void disableButtons() {
        jb1.setEnabled(false);
        jb2.setEnabled(false);
        jb3.setEnabled(false);
        jb4.setEnabled(false);
    }
}//