import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.util.*;
import java.io.Serializable;
// Jared Marcuccilli Lab 8
public class SaleClient extends JFrame implements ActionListener {
   private boolean connected = false;
   private ObjectOutputStream oos = null;
   private ObjectInputStream ois = null;
   
   private Socket s = null;
   
   JTextField jtfServer = null;
   JTextField jtfSalesperson = null;
   JTextField jtfCustomerNumber = null;
   JTextField jtfItemNumber = null;
   JTextField jtfQuantity = null;
   JLabel jlConnected = null;
   
   public static void main(String[] args) {
      new SaleClient();
   }
   
   public SaleClient() {
      super("Order System - Client");
      setLayout(new GridLayout(0, 2));
      setResizable(false);
      
      addWindowListener(
         new WindowAdapter()
         {
            public void windowClosing(WindowEvent e)
            {
               shutdown();
            }
      });
      
      JLabel jlServer = new JLabel("Server: ");
         add(jlServer);
      jtfServer = new JTextField();
         add(jtfServer);
         
      JLabel jlSalesperson = new JLabel("Salesperson: ");
         add(jlSalesperson);
      jtfSalesperson = new JTextField();
         add(jtfSalesperson);
         
      JLabel jlCustomerNumber = new JLabel("Customer #: ");
         add(jlCustomerNumber);
      jtfCustomerNumber = new JTextField();
         add(jtfCustomerNumber);
         
      JLabel jlItemNumber = new JLabel("Item #: ");
         add(jlItemNumber);
      jtfItemNumber = new JTextField();
         add(jtfItemNumber);
         
      JLabel jlQuantity = new JLabel("Item Qty: ");
         add(jlQuantity);
      jtfQuantity = new JTextField();
         add(jtfQuantity);
      
      JButton jbConnect = new JButton("Connect");
         add(jbConnect);
         jbConnect.addActionListener(this);
            
      JButton jbSend = new JButton("Send Order");
         add(jbSend);
         jbSend.addActionListener(this);
         
      JButton jbGetCount = new JButton("Get Count");
         add(jbGetCount);
         jbGetCount.addActionListener(this);
         
      JButton jbExit = new JButton("Exit");
         add(jbExit);
         jbExit.addActionListener(this);
         
      JLabel jlStatus = new JLabel("Server Status: ");
         jlStatus.setHorizontalAlignment(JLabel.CENTER);
         add(jlStatus);
         
      jlConnected = new JLabel("Disconnected");
         add(jlConnected);
         
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
   }

   public void actionPerformed(ActionEvent ae) {
      // Gets the string on the component
      String actionString = ae.getActionCommand();
      if (actionString.equals("Connect")) {
         connect();
      } else if (actionString.equals("Exit")) {
         shutdown();
      } else if (!connected) {
         System.out.println("Must be connected.");
      } else if (actionString.equals("Send Order")) {
         sendOrder();
      } else if (actionString.equals("Get Count")) {
         sendStringCommand("Get Count");
         try {
            int input = (Integer)ois.readObject();
            JOptionPane.showMessageDialog(null, "Current records: " + input);  
         } catch (SocketException se) {
            System.out.println("Can't communicate with server.");
            jlConnected.setText("Disconnected");
            connected = false;
         } catch (EOFException eofe) {
            System.out.println("Can't communicate with server.");
            jlConnected.setText("Disconnected");
            connected = false;
         } catch (Exception e) {
            e.printStackTrace();
         }
      } 
   }
   
   public void connect() {
      if (!connected) {
         try {
            s = new Socket(jtfServer.getText(), 16789);
            
            System.out.println("Connected to server.");
            jlConnected.setText("Connected");

            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
            
            connected = true;
         } catch (ConnectException ce) {
            System.out.println("Connection refused by server. (is the server running?)");
         } catch (IOException ioe) {
            ioe.printStackTrace();
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else {
         System.out.println("Already connected.");
      }
   }
   
   public void sendOrder() {
      if (connected) {
         try {
            Order order = new Order(jtfSalesperson.getText(), jtfCustomerNumber.getText(), jtfQuantity.getText(), Integer.parseInt(jtfQuantity.getText()));
            oos.writeObject(order);
            System.out.println("Sent order.");
         } catch (SocketException se) {
            System.out.println("Can't communicate with server.");
            jlConnected.setText("Disconnected");
            connected = false;
         } catch (IOException ioe) {
            ioe.printStackTrace();
         } catch (NumberFormatException nfe) {
            System.out.println("Invalid input.");
         }
      } else {
         System.out.println("Must be connected to send order.");
      }
   }
   
   public void shutdown() {
      sendStringCommand("Exit");
      if (connected) {
         try {
            s.close();
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }
         System.out.println("Leaving server.");
      } else {
         System.out.println("Not connected to server.");
      }
      System.exit(0);
   }
   
   public void sendStringCommand(String _command) {
      if (connected) {
         try {
            oos.writeObject(new String(_command));
            oos.flush();
         } catch (SocketException se) {
            
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }
      } else {
         System.out.println("Must be connected to send command.");
      }
   }
}