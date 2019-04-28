import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;
import java.util.Timer;

public class TriviaClient extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    private JTextArea jtQues;
    private JTextField jtfChat;
    private int chosenAnswer;
    private boolean answered = false;

    private String name;
    private String server;
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

    /**
     * @param args[0] Server IP
     * @param args[1] Player Name
     */
    private boolean connected = false;

    public static void main(String[] args) {

        new TriviaClient(args[0], args[1]);
    }

    public TriviaClient(String _server, String _name) {
        super("Tricky Trivia - Client");
        setLayout(new BorderLayout());
        setSize(600, 300);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel jpMain = new JPanel(new BorderLayout()); // Separates chat and game
        JPanel jpGame = new JPanel(new BorderLayout()); // Holds the game components see GUI mockup on trello
        // JPanel jpLives = new JPanel(new FlowLayout()); // Holds lives
        JPanel jpAns = new JPanel(new GridLayout(0, 1)); // Hold answer buttons
        JPanel jpChat = new JPanel(new BorderLayout()); // Holds chat contents
        JPanel jpTextIn = new JPanel(new BorderLayout()); // Holds chat entry
        
        Dimension jpbSize = new Dimension();
		jpbSize.setSize(500, 25);
		jpbRemaining.setPreferredSize(jpbSize);
		jpbRemaining.setMaximum(10000); //TIME TO ANSWER IN MILISECONDS
		jpbRemaining.setMinimum(0);
		jpbRemaining.setValue(10000);
		jpbRemaining.setStringPainted(true);
        jpGame.add(jpbRemaining, BorderLayout.NORTH);

        server = _server;
        name = _name;

        jtQues = new JTextArea("");
        Font largeFont = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
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
        JScrollPane jspStream = new JScrollPane(jtaStream);
        
        jpChat.add(jspStream, BorderLayout.CENTER);
        jpChat.add(jpTextIn, BorderLayout.SOUTH);

        jtfChat = new JTextField();
        jtfChat.addActionListener(this);
        jpTextIn.add(jtfChat);

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
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

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
        disableButtons();

        jpGame.add(jtQues, BorderLayout.CENTER);
        // jpGame.add(jpLives, BorderLayout.SOUTH);
        jpGame.add(jpAns, BorderLayout.SOUTH);

        jpMain.add(jpGame, BorderLayout.CENTER);
        jpMain.add(jpChat, BorderLayout.EAST);

        add(jpMain);
        setVisible(true);
        connect();
    }

    public void actionPerformed(ActionEvent ae) {
        try {
            if (ae.getSource() == jb1) {
                chosenAnswer = 1;
                jb1.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(1));
                answered = true;
            } else if (ae.getSource() == jb2) {
                chosenAnswer = 2;
                jb2.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(2));
                answered = true;
            } else if (ae.getSource() == jb3) {
                chosenAnswer = 3;
                jb3.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(3));
                answered = true;
            } else if (ae.getSource() == jb4) {
                chosenAnswer = 4;
                jb4.setBackground(Color.BLUE);
                disableButtons();
                oos.writeObject(new Answer(4));
                answered = true;
            } else if (ae.getSource() == jbSend || ae.getSource() == jtfChat) {
                // send chat message
                oos.writeObject(new Message(name, jtfChat.getText()));
                jtfChat.setText("");
            } else if (ae.getSource() == mItemExit) {
                shutdown();
            } else if (ae.getSource() == mItemAbout) {
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

    public class TriviaClientThread extends Thread implements Serializable {

        public void run() {
            try {
                oos = new ObjectOutputStream(s.getOutputStream());
                ois = new ObjectInputStream(s.getInputStream());
                connected = true;
                jbSend.setEnabled(true);

                oos.writeObject(name);

                while (connected) {
                    Object in = ois.readObject();

                    if (in instanceof Question) {
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
                        
                    } else if (in instanceof Message) {
                        Message m = (Message)in;
                        String[] messageSplit = m.getMessage().split(" ");
                        if (messageSplit[0].equals("/whisper")) {
                            String msg = m.getMessage().substring(m.getMessage().indexOf(" ") + 1);
                            msg = msg.substring(msg.indexOf(" ") + 1);
                            jtaStream.append("\n" + m.getSource() + " whispers to you: " + msg);
                        } else {
                            jtaStream.append("\n" + m.getSource() + ": " + m.getMessage());
                        }
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                    } else if (in instanceof String) {
                        jtaStream.append("\n" + (String) in);
                        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                    } else if (in instanceof Integer) {
                        int correctAnswer = (Integer)in;
                        disableButtons();
                        switch (chosenAnswer) {
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

                        switch (correctAnswer) {
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
            } catch (SocketException se) {
                // Client lost connection to server
                jtaStream.append("\nLost connection to server");
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                jbSend.setEnabled(false);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
    }
    
    public class UpdateBar extends TimerTask {
		public void run() {
			if(jpbRemaining.getValue() > 0) {
				jpbRemaining.setValue(jpbRemaining.getValue() - 10);
				// Changing printed value of progress bar
				jpbRemaining.setString(jpbRemaining.getValue() / 1000 + "." + (jpbRemaining.getValue() % 1000)/10 + " Seconds Remaining");
			} else {
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
				jpbRemaining.setValue(0);
				jpbRemaining.setString("Time's up!");
                jpbRemaining.setValue(10000);
                if (!answered) {
                    try {
                        oos.writeObject(new Answer(0));
                        chosenAnswer = 0;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
				this.cancel();
			}
		}
	}
    
    public void startTimer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateBar(), 0, 10);
	}
    
    public void connect() {
        try {
            s = new Socket(server, 16789);
            TriviaClientThread tct = new TriviaClientThread();
            tct.start();
            jtaStream.append("\nConnected to server.");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
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
        // System.out.println("Client shutting down...");
        try {
            if (connected) {
                s.close();
                ois.close();
                oos.close();
            } else {
                // System.out.println("Wasn't connected");
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