import java.net.*;
import java.io.*;
import java.util.*;

/**
 * A multithreaded server to interact with many clients.
 * 
 * @author Jake Christoforo
 * @author Colin Halter
 * @author Jared Marcuccilli 
 * @author Mark Weathersby
 * 
 * @version 03212019
 */

/**
 * Creates server and waits for connections.
 */
public class MyMultiThreadedServer{
   private String clientMsg;
   private ArrayList<Thread> threads = new ArrayList<Thread>();
   private ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
   private int numThreads = -1;
   private HeartbeatThread ht = null;

   /**
    * Main method creates instance of MyMultiThreadedServer.
    * @Param args command line arguments.
    */
   public static void main(String [] args) {
      new MyMultiThreadedServer();
   }
	/**
	 * Default constructor sets up program. Creates sockets and handles thread creation.
	 */
   public MyMultiThreadedServer() {
   
      try {
         System.out.println("Starting server...");
         System.out.println("getLocalHost: "+InetAddress.getLocalHost() );
         System.out.println("getByName:    "+InetAddress.getByName("localhost") );
      
         ServerSocket ss = new ServerSocket(16789);
         Socket cs = null;
         
         ServerSocket hss = new ServerSocket(16790);
         Socket hcs = hss.accept();
         
         ht = new HeartbeatThread(hcs);
            ht.start();
         
         while(true){ // Run forever once up
            System.out.println("Waiting for a client...");
            cs = ss.accept(); // Wait for connection
            ThreadServer ths = new ThreadServer(cs);
            threads.add(ths);
            ths.start();
            System.out.println("Client connected.");
            
            
         }
      }
      catch( BindException be ) {
         System.out.println("Server already running on this computer, stopping.");
      }
      catch( IOException ioe ) {
         System.out.println("IO Error");
         ioe.printStackTrace();
      }
   
   } // end main
	/**
	 * Class waits for message.
	 */
   public class HeartbeatThread extends Thread {
      private Socket hs;
      private PrintWriter hpw;
      
      public HeartbeatThread(Socket _heartbeatSocket) {
            hs = _heartbeatSocket;
      }
      
      /**
       * Continuously write to heartbeat socket so client knows if server disconnects abnormally.
       */
      public void run() {
         try {
            hpw = new PrintWriter(new OutputStreamWriter(hs.getOutputStream()));
            while (true) {
               hpw.println("");
               hpw.flush();
               Thread.sleep(100);
            }
         } catch (IOException ioe) {
            ioe.printStackTrace();
         } catch (InterruptedException ie) {
         
         }
      }
   }
   
   /**
    * ThreadServer Class serves a single client.
    */
   public class ThreadServer extends Thread {
      private Socket cs;
      private BufferedReader br;
      private PrintWriter opw;
      private String name;
      private boolean keepGoing = true;
   
      /**
       * Default constructor.
       * @param cs client socket.
       */
      public ThreadServer(Socket _cs) {
         cs = _cs;
         name = "";
      }
   	
      /**
       * Handles connections and user interactions.
       */
      public void run() {
         
         try {
            
            br = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            opw = new PrintWriter(new OutputStreamWriter(cs.getOutputStream()));
           
            opw.println("You have connected to the server!");
           
            while (name.equals("")) {
               if (name.equals("")) {
                  opw.println("Please enter your name:");
                  opw.flush();
               }
               name = br.readLine();
            }
           
            if (name.equals("Disconnect")) { // Remove client from writers.
               opw.close();
               br.close();
               cs.close();
               writers.remove(opw);
               System.out.println("Client left before sending name.");
               return;
            }
            send(name + " joined the server.");
           
            opw.println("Welcome to the server " + name + "!");
            opw.flush();
           
            writers.add(opw); // Add writer to list of connected clients
           
            while (keepGoing) {
               synchronized(this) {
                  try {		
                     clientMsg = br.readLine(); // From client
                     if (clientMsg.equals("Disconnect")) {
                        keepGoing = false;
                        writers.remove(opw);
                        send(name + " left the server. (disconnect msg)");
                        opw.close();
                        br.close();
                        cs.close();
                        ht.interrupt();
                        return;
                     } else {
                        send(name + ": " + clientMsg);
                     }
                  } catch (SocketException se) {
                     writers.remove(opw);
                     send(name + " left the server. (se)");
                     opw.close();
                     br.close();
                     cs.close();
                     ht.interrupt();
                     return;
                  } catch (NullPointerException npe) {
                     writers.remove(opw);
                     send(name + " left the server. (npe)");
                     opw.close();
                     br.close();
                     cs.close();
                     ht.interrupt();
                     return;
                  } catch (Exception e) {
                     e.printStackTrace();
                  }
               }                        			
            }
         } catch(IOException e) { 
            e.printStackTrace();
         } catch (NullPointerException npe) {
            System.out.println("Client left abnormally before sending name.");
         }
      } // End while
      
      /**
       * Sends a message to each connected client.
       */
      public void send(String _msg) {
         for (PrintWriter opw : writers) {
            opw.println(_msg); // Send message to every client
            opw.flush();
         }
         System.out.println("> " +_msg + " (sent to " + writers.size() + " clients)");
      }
   } // End class ThreadServer 
} // End MyMultiThreadedServer