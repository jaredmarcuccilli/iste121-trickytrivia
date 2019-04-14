import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.util.*;

/**
 * A multithreaded client that recieves messages from a server.
 * 
 * @author Jake Christoforo
 * @author Colin Halter
 * @author Jared Marcuccilli 
 * @author Mark Weathersby
 * 
 * @version 03212019
 */

/**
 * Contains GUI functionality.
 */
public class MyClient extends JFrame implements ActionListener {
    private BufferedReader bin = null;
    private Socket s = null;
    private Socket heartbeatSocket = null;
    private OutputStream out = null;
    private PrintWriter pout = null;
    
    private boolean connected = false;
    
    private JButton jbSend = null;
    private JButton jbDisconnect = null;
    private JTextArea jtaStream = null;
    private JScrollPane jspStream = null;
    private JTextField jtfMessage = null;
    
    /**
     * Creates new instance of client.
     */
    public static void main(String[] args) {
        new MyClient();
    }

    /**
     * Default constructor creates GUI 
     */
    public MyClient() {
        super("Chat Client");
        setLayout(new BorderLayout());
        setSize(500, 400);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                disconnect();
            }
        });
        
        jtaStream = new JTextArea();
        jtaStream.setSize(500, 300);
        jtaStream.setEditable(false);
        jspStream = new JScrollPane(jtaStream);
        jspStream.setSize(500, 300);
        
        add(jspStream, BorderLayout.CENTER);
        
        JPanel jpSouth = new JPanel();
        jpSouth.setLayout(new BorderLayout());
        
        // Create buttons
        jbSend = new JButton("Send");
            jbSend.addActionListener(this);
            jpSouth.add(jbSend, BorderLayout.EAST);
            
        jbDisconnect = new JButton("Disconnect");
            jbDisconnect.setEnabled(false);
            jbDisconnect.addActionListener(this);
            jpSouth.add(jbDisconnect, BorderLayout.WEST);
            
        jtfMessage = new JTextField();
            jtfMessage.addActionListener(this);
            jpSouth.add(jtfMessage, BorderLayout.CENTER);
            
        add(jpSouth, BorderLayout.SOUTH);
        
        jtaStream.setText("To connect, enter the IP of the server and press Send.");
        
        setLocationRelativeTo(null);
        setVisible(true);
        
    }
    
    /**
     * Handles action events for buttons. Will either call sendMessage() or disconnect from server.
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == jbSend || ae.getSource() == jtfMessage) {
            sendMessage();
        } else if (ae.getSource() == jbDisconnect && connected) {
            disconnect();
        }   
    }
    
    /**
     * Listens for incoming messages
     */
    public class HeartbeatThread extends Thread {
        private BufferedReader heartbeatBin = null;
        private Socket hs;
        
        public HeartbeatThread(Socket _heartbeatSocket) {
            try {
               InputStream heartbeatIn = _heartbeatSocket.getInputStream();
               heartbeatBin = new BufferedReader(new InputStreamReader(heartbeatIn));
               hs = _heartbeatSocket;
            } catch (IOException ioe) {
               ioe.printStackTrace();
            }   
        }
        
        /**
         * Listens for the server closing.
         */
        public void run() {
            try {
               while (true) {
                  if (heartbeatBin.readLine() == null) {
                     jbSend.setEnabled(false);
                     jbDisconnect.setEnabled(false);
                     jtaStream.append("\nServer disconnected. (heartbeat)");
                     out.close();
                     pout.close();
                     s.close();
                     connected = false;
                     return;
                  }
               }
            } catch (SocketException se) {
               jtaStream.append("\nServer disconnected. (se)");
            } catch (IOException ioe) {
               ioe.printStackTrace();
            }
        }       
    }
    
    /**
     * Connects to the server.
     * @param _server server ip.
     */
    public void connect(String _server) {
        try {
            s = new Socket(_server, 16789);
            
            // Remove these lines if server doesn't have a socket for heartbeat
            heartbeatSocket = new Socket(_server, 16790);
            HeartbeatThread ht = new HeartbeatThread(heartbeatSocket);
            ht.start();
            
            MyThread t = new MyThread();
            t.start();
   
            out = s.getOutputStream();
            pout = new PrintWriter(out);
            
            connected = true;
            jbDisconnect.setEnabled(true);
            
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
    
    /**
     * Disconnects client from server.
     */
    public void disconnect() {
        jtaStream.append("\nDisconnecting from server...");
        try {
            Thread.sleep(100);
            if (connected) {
                pout.println("Disconnect");
                pout.flush();   
                out.close();
                pout.close();
                s.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    
    /**
     * Sends message from jtfMessage to server.
     */
    public void sendMessage() {
        if (connected) {
            String message = jtfMessage.getText();
            if (message.equals("Disconnect")) {
               disconnect();
            }
            pout.println(message);
            pout.flush();
        } else {
            connect(jtfMessage.getText());
        }
        jtfMessage.setText("");
    }

    /**
     * Class listens for incoming message.
     */
    public class MyThread extends Thread {
    	
    	/**
    	 * Default constructor creates Buffered Reader and Input Stream.
    	 */
        public MyThread() {
            try {
               InputStream in = s.getInputStream();
               bin = new BufferedReader(new InputStreamReader(in));
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
        /**
         * Reads message and appends to JTextArea.
         */
        public void run() {

            while (true) {
                try {
                    if (bin.ready()) {
                        jtaStream.append("\n" + bin.readLine());
                        jtaStream.setCaretPosition(jtaStream.getDocument().getLength());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}