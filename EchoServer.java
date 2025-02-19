// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


package simpleChat;


import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import simpleChat.common.Message;

import java.io.IOException;
import java.net.InetAddress;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
  }

  
  //Instance methods ************************************************
  /*
     * Server side commands
     */
    public void handleMessageFromServerConsole(String message) {
        if (message.startsWith("#")) {
            String[] parameters = message.split(" ");
            String command = parameters[0];
            switch (command) {
                case "#quit":
                    //closes the server and then exits it
                    try {
                        this.close();
                    } catch (IOException e) {
                        System.exit(1);
                    }
                    System.exit(0);
                    break;
                case "#stop":
                    this.stopListening();
                    break;
                case "#close":
                    try {
                        this.close();
                    } catch (IOException e) {
		    e.printStackTrace();
                    }
                    break;
                case "#setport":
                    if (!this.isListening() && this.getNumberOfClients() < 1) {
                        super.setPort(Integer.parseInt(parameters[1]));
                        System.out.println("Port set to " + Integer.parseInt(parameters[1]));
                    } else {
                        System.out.println("Can't do that now. Server is connected.");
                    }
                    break;
                case "#start":
                    if (!this.isListening()) {
                        try {
                            this.listen();
                        } catch (IOException e) {
                            //error listening for clients
                        }
                    } else {
                        System.out.println("We are already started and listening for clients!.");
                    }
                    break;
                case "#getport":
                    System.out.println("Current port is " + this.getPort());
                    break;
                default:
                    System.out.println("Invalid command: '" + command+ "'");
                    break;
            }
        } else {
            this.sendToAllClients(new Message(message, Message.ORIGIN_SERVER));
        }
    }
  /*
     * This method overrides the implementation found in AbstractServer
     */
    
    public synchronized void clientConnected(ConnectionToClient client) {
        InetAddress newClientIP = client.getInetAddress();
        String message = "Welcome client at " + newClientIP.getHostAddress() + " !!";
        Message msg = new Message(message, Message.ORIGIN_SERVER);
        this.sendToAllClients(msg);
        System.out.println(message);
    }

    /*
     * Override the client disconnected to let everyone know the user left
     */
    
    public synchronized void clientDisconnected(ConnectionToClient client) {
        String message = client.getInfo("username") + " has logged off!";
        Message msg = new Message(message, Message.ORIGIN_SERVER);
        this.sendToAllClients(msg);
        System.out.println(message);
    }

    /*
     * Override the client exception to let everyone know the user left
     */
    
    public synchronized void clientException(ConnectionToClient client, Throwable exception) {
        String message = client.getInfo("username") + " has logged off!";
        Message msg = new Message(message, Message.ORIGIN_SERVER);
        this.sendToAllClients(msg);
        System.out.println(message);
    }

  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        String message = msg.toString();
        if (message.startsWith("#")) {
            String[] params = message.substring(1).split(" ");
            if (params[0].equalsIgnoreCase("login") && params.length > 1) {
                if (client.getInfo("username") == null) {
                    client.setInfo("username", params[1]);
                } else {
                    try {
                        client.sendToClient(new Message("Your username has already been set!", Message.ORIGIN_SERVER));
                    } catch (IOException e) {
                    }
                }

            }
        } else {
            if (client.getInfo("username") == null) {
                try {
                    client.sendToClient(new Message("Please set a username before messaging the server!", Message.ORIGIN_SERVER));
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Message received: " + msg + " from " + client.getInfo("username"));
                this.sendToAllClients(new Message(client.getInfo("username") + " > " + message, Message.ORIGIN_CLIENT));
            }
        }
    }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
}
//End of EchoServer class
