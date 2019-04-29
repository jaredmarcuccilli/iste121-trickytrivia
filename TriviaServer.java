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
 * Trivia game server. Accepts client connections and handles game logic.
 *
 * @author Jake Christoforo
 * @author Colin Halter
 * @author Jared Marcuccilli
 * @author Mark Weathersby
 *  
 * @version 1.0.0
 */
public class TriviaServer extends JFrame implements ActionListener {
    private JTextArea jtaStream;
    
    private Vector<TriviaServerThread> threads = new Vector<TriviaServerThread>();
    private Vector<Player> players = new Vector<Player>();
    private Vector<ObjectOutputStream> allObjectOutputStreams = new Vector<ObjectOutputStream>();
    private Question currentQuestion;
    private int currentQuestionNo;
    private BufferedReader questionBr = null;
    private boolean keepGoing = true;
    
    private JFrame jfScores;
    
    private JButton jbStartGame;
    private JProgressBar jpbRemaining = new JProgressBar();
    private JSlider slider = null;
    private JMenuItem mItemExit = null;
    private JMenuItem mItemAbout = null;
    private JMenuItem mItemHelp = null;
    
    /**
     * Create a new TriviaServer.
     */
    public static void main(String[] args) {
        new TriviaServer();
    }
    
    /**
     * Create GUI elements and accept client connections.
     * Create threads with client sockets.
     */
    public TriviaServer() {
        // Create initial gui
        super("Tricky Trivia - Server");
        setLayout(new BorderLayout());
        setSize(600, 400);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // Create main text area
        jtaStream = new JTextArea();
        jtaStream.setEditable(false);
        jtaStream.setLineWrap(true);
        jtaStream.setWrapStyleWord(true);
        JScrollPane jspStream = new JScrollPane(jtaStream);
        add(jspStream);
        jtaStream.append("Trivia Server starting...");
        
        // Create time remaining bar
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
        
        // Add window listener to shutdown on close
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    shutdown();
                }
        });
        
        JPanel jpSouth = new JPanel();
        
        // Create the question number slider
        slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 25);
            slider.setMinorTickSpacing(1);
            slider.setMajorTickSpacing(10);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            slider.setSnapToTicks(true);
            slider.setPreferredSize(new Dimension(300, 50));
            slider.setEnabled(false);
            jpSouth.add(slider);
        
        // Start game button
        jbStartGame = new JButton("Start Game!");
            jbStartGame.addActionListener(this);
            jbStartGame.setEnabled(false);
            jpSouth.add(jbStartGame);
            
        add(jpSouth, BorderLayout.SOUTH);
        
        drawBoard();
        setVisible(true);
        
        // Print out the server's IP info
        try {
            String[] ipAddress = InetAddress.getLocalHost().toString().split("/");
            jtaStream.append("\nIP Address: " + ipAddress[1]);
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        
        // Create reader for questions.txt
        try {
            FileInputStream fis = new FileInputStream("questions.txt");
            BufferedInputStream bis = new BufferedInputStream(fis);
            questionBr = new BufferedReader(new InputStreamReader(bis));
            
            ServerSocket ss = new ServerSocket(16789);
            
            slider.setEnabled(true);
            jbStartGame.setEnabled(true); // Enable these once we know another server isn't already running
            while(true) {
                    jtaStream.append("\nWaiting for a client...");
                    jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                    Socket s = ss.accept();
                    TriviaServerThread tst = new TriviaServerThread(s); // Create a thread with a new client connection
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
    }
    
    /**
     * Create the leaderboard JFrame
     */
    public void drawBoard() {
    	jfScores = new JFrame("Trivia - Leaderboard");
    	jfScores.setLayout(new GridLayout(0,2));
        jfScores.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jfScores.setResizable(false);
    	
        redrawBoard();
        
    	jfScores.setSize(250,400);
        jfScores.setLocationRelativeTo(null);
    	jfScores.setVisible(true);
    }
    
    public void redrawBoard() {
        jfScores.getContentPane().removeAll();
        jfScores.getContentPane().repaint();
        for (Player currPlayer : players) {
            JLabel jlName = new JLabel();
            JLabel jlScore = new JLabel();
            jlName.setFont(new Font("helvetica", Font.PLAIN, 32));
            jlScore.setFont(new Font("helvetica", Font.PLAIN, 32));
            jlName.setText(currPlayer.getPlayerName());
            jfScores.add(jlName);
            jlScore.setText("" + currPlayer.getPlayerScore());
            jfScores.add(jlScore);
            jfScores.revalidate();
            jfScores.repaint();
    	}
    }
    
    /**
     * Perform action based on ActionEvents.
     * @param ActionEvent
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == jbStartGame) { // Start game button
            startGame();
        } else if (ae.getSource() == mItemExit) { // Menu bar "exit"
            shutdown();
        } else if (ae.getSource() == mItemAbout) { // Menu bar "about"
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(null, "Tricky Trivia\nCreated for Prof. Patric's ISTE-121\nDeveloped by:\n   Jake Christoforo\n   Colin Halter\n   Jared Marcuccilli\n   Mark Weathersby", "About Tricky Trivia", JOptionPane.INFORMATION_MESSAGE);
        } else if (ae.getSource() == mItemHelp) { // Menu bar "help"
            JFrame frame = new JFrame();
            JOptionPane.showMessageDialog(null, "How to Play:\nProvide players with the IP address displayed in the text area.\nSpecify how many questions you would like to play, and once all players have connected click \"Start Game!\"\nPlayers have 10 seconds to answer.\nThere are 5 seconds between each question.\nScore is calculated based on how quickly players respond.\nIncorrect responses subtract points.", "Help", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Timer bar.
     * Runs for 10 seconds. Once time is up, display "Time's Up!"
     */
    public class UpdateBar extends TimerTask {
		public void run() {
			if(jpbRemaining.getValue() > 0) {
				jpbRemaining.setValue(jpbRemaining.getValue() - 10);
				// Changing printed value of progress bar
				jpbRemaining.setString(jpbRemaining.getValue() / 1000 + "." + (jpbRemaining.getValue() % 1000)/10 + " Seconds Remaining");
			} else {
                redrawBoard();
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
    
    /**
     * Create the timer bar and update it.
     */
    public void startTimer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new UpdateBar(), 0, 10);
	}
    
    /**
     * Start the game. Send new questions to the clients every 15 seconds.
     */
    public void startGame() {
        keepGoing = true;
        currentQuestionNo = 0;
        jbStartGame.setEnabled(false);
        slider.setEnabled(false);
        jtaStream.append("\nStarting game...");
        jtaStream.append("\nRunning for " + slider.getValue() + " questions.");
        Timer timer = new Timer();
		timer.scheduleAtFixedRate(new sendQuestion(), 0, 15000);
    }
    
    /**
     * Thread for each client. Handles object io and keeps track of player's score.
     */
    public class TriviaServerThread extends Thread implements Serializable {
        private Socket s;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private Player thisPlayer;
        
        /**
         * Constructor.
         * @param Socket The client's socket
         */
        public TriviaServerThread(Socket _s) {
            s = _s;
        }
        
        /**
         * Run thread.
         */
        public void run() {
            try {
                ois = new ObjectInputStream(s.getInputStream());
                oos = new ObjectOutputStream(s.getOutputStream());
                allObjectOutputStreams.add(oos);
                thisPlayer = new Player((String)ois.readObject());
                players.add(thisPlayer);
                
                jtaStream.append("\n" + thisPlayer.getPlayerName() + " joined the server");
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                
                redrawBoard();
                
                while (true) {
                    Object in = ois.readObject();
                    if (in instanceof Answer) { // If an Answer is received...
                        Answer a = (Answer)in;
                        if (a.getPlayerAnswerNum() == currentQuestion.getCorrectAnswerNum()) {
                            // On correct answer, add score to player based on the time they answered
                            thisPlayer.addPlayerScore(jpbRemaining.getValue()/100);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + " in  " + (10 - jpbRemaining.getValue()/1000) + " seconds, which is correct. Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            a.setPlayerAnswerNum(0); 
                        } else if (a.getPlayerAnswerNum() == 0) {
                            // If answer is 0, player didn't answer in time
                            thisPlayer.subtractPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " didn't answer in time! Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            a.setPlayerAnswerNum(0);
                        } else {
                            // Incorrect answer, update score
                            thisPlayer.subtractPlayerScore(10);
                            jtaStream.append("\n" + thisPlayer.getPlayerName() + " answered " + a.getPlayerAnswerNum() + ", which is incorrect. Their score is: " + thisPlayer.getPlayerScore());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            a.setPlayerAnswerNum(0);
                        }
                    } else if (in instanceof Message) { // If a Message is received...
                        Message m = (Message)in;
                        String[] messageSplit = m.getMessage().split(" ");
                        boolean playerFound = false;
                        if (messageSplit[0].equals("/whisper") && messageSplit.length > 1) { // If a /whisper command...
                            String msg = m.getMessage().substring(m.getMessage().indexOf(" ") + 1);
                            msg = msg.substring(msg.indexOf(" ") + 1);
                            for (TriviaServerThread t : threads) { // Find the destination player and send only them the message
                                if (t.getThisPlayer().getPlayerName().equalsIgnoreCase(messageSplit[1])) {
                                    t.sendThisClient(m);
                                    jtaStream.append("\n" + m.getSource() + " whispers to " + t.getThisPlayer().getPlayerName() +": " + msg); // Get rid of the first 2 words; /whisper and the destination name
                                    playerFound = true;
                                }
                            }
                            if (!playerFound) { // If destination player couldn't be found...
                                jtaStream.append("\nCouldn't find player " + m.getSource() + " tried to whisper to: " + m.getMessage());
                                sendThisClient("I couldn't find the player you were trying to whisper to.");
                            }
                        } else { // If a general message, send to all clients and append to chat log
                            jtaStream.append("\n" + m.toString());
                            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                            sendClients(m);
                        }
                    }
                }
            } catch (SocketException se) {
                // Server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                threads.remove(this);
                players.remove(thisPlayer);
                redrawBoard();
                allObjectOutputStreams.remove(oos);
                jtaStream.append("\nCurrent connections: " + players.size());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
            } catch (EOFException eofe) {
                // Server lost connection to client
                jtaStream.append("\nLost connection to: " + thisPlayer.getPlayerName());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                threads.remove(this);
                players.remove(thisPlayer);
                redrawBoard();
                allObjectOutputStreams.remove(oos);
                jtaStream.append("\nCurrent connections: " + players.size());
                jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
        
        /**
         * Return a thread's Player object.
         * @return thisPlayer
         */
        public Player getThisPlayer() {
            return thisPlayer;
        }
        
        /**
         * Send an object to this thread's player.
         * @param Object The object to send to this player
         */
        public void sendThisClient(Object _o) {
            try {
                oos.writeObject(_o);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    /**
     * Shut down the server.
     */
    public void shutdown() {
        jtaStream.append("\nServer shutting down...");
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        System.exit(0);
    }
    
    /**
     * Send an object to all clients.
     * @param Object The object to send to all clients
     */
    public void sendClients(Object _o) {
        for (ObjectOutputStream oos : allObjectOutputStreams) {
            try {
                oos.writeObject(_o);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    /**
     * Indicate that the game is over.
     */
    public void gameOver() {
        jtaStream.append("\nGame is over!");
        jpbRemaining.setString("Game is over!");
        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());

    }
    
    /**
     * Send a Question to all clients. Keep track of what number question we are on.
     */
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
    
    /**
     * Grab a new Question from the text file.
     * @return Question
     */
    public Question getNewQuestion() {
        String[] questionElements = new String[6];
        
        try {
            questionElements = questionBr.readLine().split("~");
            Question q = new Question(questionElements[0], questionElements[1], questionElements[2], questionElements[3], questionElements[4], questionElements[5]);
            return q;
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            jtaStream.append("\nReached end of question file");
            jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
        }
        return null;
    }
}//