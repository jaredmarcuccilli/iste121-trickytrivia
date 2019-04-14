import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.io.Serializable;
// Jared Marcuccilli Lab 8
public class SaleServer extends JFrame implements ActionListener {
   private ArrayList<Thread> threads = new ArrayList<Thread>();
   private Vector<Order> orders = new Vector<Order>();
   private boolean keepGoing = true;
   
   private JLabel jlCurrent = null;
   
   private final static Object lock = new Object();
   
   public static void main(String[] args) {
      new SaleServer();
   }
   
   public SaleServer() {
      // create gui
      super("Order System - Server");
      setLayout(new GridLayout(0, 1));
      setResizable(false);
      
      JLabel jlHost = new JLabel();
         jlHost.setHorizontalAlignment(JLabel.CENTER);
         add(jlHost);
      JLabel jl1 = new JLabel("Existing records: ");
         jl1.setHorizontalAlignment(JLabel.CENTER);
         add(jl1);
      JLabel jlExisting = new JLabel("0");
         jlExisting.setHorizontalAlignment(JLabel.CENTER);
         add(jlExisting);
      JLabel jl2 = new JLabel("Records in vector: ");
         jl2.setHorizontalAlignment(JLabel.CENTER);
         add(jl2);
      jlCurrent = new JLabel("0");
         jlCurrent.setHorizontalAlignment(JLabel.CENTER);
         add(jlCurrent);
      JButton jbWrite = new JButton("Write to CSV");
         add(jbWrite);
      jbWrite.addActionListener(this);
      JButton jbShutdown = new JButton("Shutdown");
         add(jbShutdown);
      jbShutdown.addActionListener(this);
         
      addWindowListener(
         new WindowAdapter()
         {
            public void windowClosing(WindowEvent e)
            {
               shutdown();
            }
      });
            
      // need to check if orders.dat exists; if it does add all objects to orders vector
      File f = new File("orders.dat");
      if (f.exists()) {
         System.out.println("orders.dat exists, reading and adding to vector...");
         try {
            ObjectInputStream initialize = new ObjectInputStream(new FileInputStream(f));
            //while (initialize.available() > 0) {
               orders = (Vector)initialize.readObject();
               System.out.println(orders);
            //}
         } catch (Exception e) {
            e.printStackTrace();
         }
         jlExisting.setText(Integer.toString(orders.size()));
         jlCurrent.setText(Integer.toString(orders.size()));
         System.out.println("Read in " + orders.size() + " existing orders.");
      }
   
      try {
         jlHost.setText(InetAddress.getLocalHost().toString().substring(InetAddress.getLocalHost().toString().lastIndexOf("/") + 1)); // :(
        
         ServerSocket ss = new ServerSocket(16789);
         
         
         
         pack();
         setLocationRelativeTo(null);
         setVisible(true);
        
         while(true){ // run forever once up
            Socket s = ss.accept(); // wait for connection
            SaleServerThread sst = new SaleServerThread(s);
            threads.add(sst);
            sst.start();
            System.out.println("Client connected. Current connections: " + threads.size());
         }
      }
      catch( BindException be ) {
         System.out.println("Server already running on this computer, stopping.");
      }
      catch( IOException ioe ) {
         System.out.println("IO Error");
         ioe.printStackTrace();
      }
   }
   
   public void shutdown() {
      System.out.println("Shutting down...");
      writeDAT();
      System.exit(0);
   }
   
   public void writeCSV() {
      synchronized(lock) {
         System.out.println("Writing vector to csv...");
         try {
            PrintWriter out = new PrintWriter("orders.csv");
            for (Order o : orders) {
               // write salesperson, cust #, item #, and qty to orders.csv
               out.print(o.getSalesperson() + ",");
               out.print(o.getCustomerNumber() + ",");
               out.print(o.getItemNumber() + ",");
               out.print(o.getItemQuantity() + ",");
               out.println(o.getSalesperson());
            }
            out.close();
            orders.clear();
            jlCurrent.setText(Integer.toString(orders.size()));
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public void writeDAT() {
      synchronized(lock) {
         // write orders vector to orders.dat
         try {
            System.out.println("Writing vector of orders to orders.dat...");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("orders.dat"));
            oos.writeObject(orders);
            oos.flush();
            oos.close();
            orders.clear();
            jlCurrent.setText(Integer.toString(orders.size()));
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public void actionPerformed(ActionEvent ae) {
      // Gets the string on the component
      String actionString = ae.getActionCommand();
      if (actionString.equals("Shutdown")) {
         shutdown();
      } else if (actionString.equals("Write to CSV")) {
         writeCSV();
      }
   
   }
   
   public class SaleServerThread extends Thread implements Serializable {
      private Socket s;
      
      private ObjectInputStream ois;
      private ObjectOutputStream oos;
      
      private Object input;
      
      public SaleServerThread(Socket _s) {
         s = _s;
      }
      
      public void run() {
         try {
            ois = new ObjectInputStream(s.getInputStream());
            oos = new ObjectOutputStream(s.getOutputStream());         
         
            System.out.println("Client connected.");
                  
            while (keepGoing) {
               input = ois.readObject();
               
               if (input instanceof Order) {
                  synchronized(lock) {
                     orders.add((Order)input);
                     jlCurrent.setText(Integer.toString(orders.size()));
                  }
                  System.out.println("Added order: " + input);
               } else if (input instanceof String) {
                  stringCommand(input.toString());
               } else {
                  System.out.println("Unexpected object");
               }
            }
            
         } catch (IOException ioe) {
            ioe.printStackTrace();
         } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
         }
         
      }
      
      public void stringCommand(String _input) {
         System.out.println("Received command: " + _input);
         
         try {
            if (_input.equals("Get Count")) {
               System.out.println("Sent to client: " + orders.size());
               oos.writeObject(orders.size());
               oos.flush();
            } else if (_input.equals("Exit")) {
               clientShutdown();
            }
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }
      }
      
      public void clientShutdown() {
         try {
            keepGoing = false;
            System.out.println("Client left server. Current connections: " + threads.size());
            ois.close();
            oos.close();
            s.close();
            threads.remove(this);
            interrupt();
            return;
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }
      }  
   }
}