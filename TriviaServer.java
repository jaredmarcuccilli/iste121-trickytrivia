import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;
import java.util.Timer;

public class TriviaServer extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    
    private Vector<TriviaServerThread> threads = new Vector<TriviaServerThread>();
    private Vector<Player> players = new Vector<Player>();
    private Vector<ObjectOutputStream> allObjectOutputStreams = new Vector<ObjectOutputStream>();
    private Question currentQuestion;
    private int currentQuestionNo;
    private BufferedReader questionBr = null;
    private boolean keepGoing = true;
    
    private JButton jbStartGame;
    private JProgressBar jpbRemaining = new JProgressBar();
    private JSlider slider = null;
    private JMenuItem mItemExit = null;
    private JMenuItem mItemAbout = null;
    private JMenuItem mItemHelp = null;
    
    public static void main(String[] args) {
        new TriviaServer();
    }
    
    public TriviaServer() {
        super("Tricky Trivia - Server");
        setLayout(new BorderLayout());
        setSize(600, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        
        jtaStream = new JTextArea();
        jtaStream.setEditable(false);
        jtaStream.setLineWrap(true);
        jtaStream.setWrapStyleWord(true);
        JScrollPane jspStream = new JScrollPane(jtaStream);
        add(jspStream);
        jtaStream.append("Trivia Server starting...");
        
        Dimension jpbSize = new Dimension();
        jpbSize.setSize(500, 25);
    	jpbRemaining.setPreferredSize(jpbSize);
    	jpbRemaining.setMaximum(10000);
    	jpbRemaining.setMinimum(0);
    	jpbRemaining.setValue(10000);
    	jpbRemaining.setStringPainted(true);
        jpbRemaining.setString(jpbRemaining.getValue() / 1000 + "." + (jpbRemaining.getValue() % 1000)/10 + " Seconds Remaining");
        add(jpbRemaining, BorderLayout.NORTH);
        
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
        
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
        
        JPanel jpSouth = new JPanel();
        
        slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 25);
            slider.setMinorTickSpacing(1);
            slider.setMajorTickSpacing(10);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setSnapToTicks(true);
            slider.setPreferredSize(new Dimension(300, 50));
            slider.setEnabled(false);
            jpSouth.add(slider);
            
        jbStartGame = new JButton("Start Game!");
            jbStartGame.addActionListener(this);
            jbStartGame.setEnabled(false);
            jpSouth.add(jbStartGame);
            
        add(jpSouth, BorderLayout.SOUTH);
        
        setVisible(true);
        
        try {
            String[] ipAddress = InetAddress.getLocalHost().toString().split("/");
            jtaStream.append("\nIP Address: " + ipAddress[1]);
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
          
        try {
            FileInputStream fis = new FileInputStream("questions.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            questionBr = new BufferedReader(new InputStreamReader(bis));
            
            ServerSocket ss = new ServerSocket(16789);
            
            slider.setEnabled(true);
            jbStartGame.setEnabled(true); // enable these once we know another server isn't already running
            while(true) {
                    jtaStream.append("\nWaiting for a client...");
                    jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                    Socket s = ss.accept();
                    TriviaServerThread tst = new TriviaServerThread(s);
                    threads.add(tst);
                    tst.start();
                    jtaStream.append("\nClient connected. Current connections: " + threads.size());
                    jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
            }
        } catch (BindException be) {
            jtaStream.append("\nPort is already in use. (is a server already running on this machine?)");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == jbStartGame) {
            startGame();
        } else if (ae.getSource() == mItemExit) {
            shutdown();
        } else if (ae.getSource() == mItemAbout) {
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(null, "Tricky Trivia\nCreated for Prof. Patric's ISTE-121\nJake Christoforo\nColin Halter\nJared Marcuccilli\nMark Weathersby", "About Tricky Trivia", JOptionPane.INFORMATION_MESSAGE);
        } else if (ae.getSource() == mItemHelp) {
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(null, "How to Play:\nProvide players with the IP address displayed in the text area.\nSpecify how many questions you would like to play, and once all players have connected click \"Start Game!\"\nPlayers have 10 seconds to answer.\nThere are 5 seconds between each question.\nScore is calculated based on how quickly players respond.\nIncorrect responses subtract points.", "Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public class UpdateBar extends TimerTask {
		public void run() {
			if(jpbRemaining.getValue() > 0) {
				jpbRemaining.setValue(jpbRemaining.getValue() - 10);
				// Changing printed value of progress bar
				jpbRemaining.setString(jpbRemaining.getValue() / 1000 + "." + (jpbRemaining.getValue() % 1000)/10 + " Seconds Remaining");
			} else {
				jtaStream.append("\nTime's up!");
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
				jpbRemaining.setValue(0);
				jpbRemaining.setString("Time's up!");
                sendClients(currentQuestion.getCorrectAnswerNum());
                jpbRemaining.setValue(10000);
                if (keepGoing == false) {
                    gameOver();
                }
				this.cancel();
			}
		}
	}
    
    public void startTimer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateBar(), 0, 10);
	}
    
    public void startGame() {
        jbStartGame.setEnabled(false);
        slider.setEnabled(false);
        jtaStream.append("\nStarting game...");
        jtaStream.append("\nRunning for " + slider.getValue() + " questions.");
        Timer timer = new Timer();
		timer.scheduleAtFixedRate(new sendQuestion(), 0, 15000);
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
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                
                while (true) {
                    Object in = ois.readObject();
                    if (in instanceof Answer) {
                        Answer a = (Answer)in;
                        if (a.getPlayerAnswerNum() == currentQuestion.getCorrectAnswerNum()) {
                            // On correct answer, add score to player based on the time they answered
                            thisPlayer.addPlayerScore(jpbRemaining.getValue()/100);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + " in  " + (10 - jpbRemaining.getValue()/1000) + " seconds, which is correct. Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            a.setPlayerAnswerNum(0);
                        } else if (a.getPlayerAnswerNum() == 0) {
                            thisPlayer.subtractPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " didn't answer in time! Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            a.setPlayerAnswerNum(0);
                        } else {
                            // incorrect answer, update score
                            thisPlayer.subtractPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + ", which is incorrect. Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            a.setPlayerAnswerNum(0);
                        }
                    } else if (in instanceof Message) {
                        Message m = (Message)in;
                        String[] messageSplit = m.getMessage().split(" ");
                        boolean playerFound = false;
                        if (messageSplit[0].equals("/whisper") && messageSplit.length > 1) {
                            String msg = m.getMessage().substring(m.getMessage().indexOf(" ") + 1);
                            msg = msg.substring(msg.indexOf(" ") + 1);
                            for (TriviaServerThread t : threads) {
                                if (t.getThisPlayer().getPlayerName().equalsIgnoreCase(messageSplit[1])) {
                                    t.sendThisClient(m);
                                    jtaStream.append("\n" + m.getSource() + " whispers to " + t.getThisPlayer().getPlayerName() +": " + msg); // get rid of the first 2 words; /whisper and the destination name
                                    playerFound = true;
                                }
                            }
                            if (!playerFound) {
                                jtaStream.append("\nCouldn't find player " + m.getSource() + " tried to whisper to: " + m.getMessage());
                                sendThisClient("I couldn't find the player you were trying to whisper to.");
                            }
                        } else {
                            jtaStream.append("\n" + m.toString());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            sendClients(m);
                        }
                    }
                }
            } catch (SocketException se) {
                // server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                threads.remove(this);
                players.remove(thisPlayer);
                allObjectOutputStreams.remove(oos);
                jtaStream.append("\nCurrent connections: " + threads.size());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
            } catch (EOFException eofe) {
                // server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                threads.remove(this);
                players.remove(thisPlayer);
                allObjectOutputStreams.remove(oos);
                jtaStream.append("\nCurrent connections: " + threads.size());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
        
        public Player getThisPlayer() {
            return thisPlayer;
        }
        
        public void sendThisClient(Object _o) {
            try {
                oos.writeObject(_o);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    // Server shutdown
    public void shutdown() {
        jtaStream.append("\nServer shutting down...");
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
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
    
    public void gameOver() {
        jtaStream.append("\nGame is over!");
        jpbRemaining.setString("Game is over!");
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
    }
    
    public class sendQuestion extends TimerTask {
        public void run() {
            if (keepGoing) {
                currentQuestionNo++;
                currentQuestion = getNewQuestion();
                if (currentQuestion != null) {
                    sendClients(currentQuestion);
                    jtaStream.append("\n" + currentQuestionNo + ". " + currentQuestion.toString());
                    jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                } 
                startTimer();
                if (currentQuestionNo == slider.getValue()) {
                    keepGoing = false;
                }
            }
        }
    }
    
    // Return a Question object
    public Question getNewQuestion() {
        String[] questionElements = new String[6];
        
        try {
            questionElements = questionBr.readLine().split("~");
            Question q = new Question(questionElements[0], questionElements[1], questionElements[2], questionElements[3], questionElements[4], questionElements[5]);
            return q;
        } catch (FileNotFoundException fnfe) {
         
        } catch (IOException ioe) {
         
        } catch (NullPointerException npe) {
            jtaStream.append("\nReached end of question file");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        }
        return null;
    }
}