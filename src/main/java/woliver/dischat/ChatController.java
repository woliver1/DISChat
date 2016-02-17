/*
 * Class to be the controller for our DIS Chat application.
 * 
 * TODO
 * 1. Needs to check network paramerters are correct before connecting.
 * 2. Needs to check the dis thread for users and write them into
 *    the usersTextArea
 * 3. Needs to check the dis thread and check for messages and write them 
 *    into the chatTextArea
 */

package woliver.dischat;

import edu.nps.moves.dis.EntityID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JTextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller for the DISChat application.
 * 
 * The chat controllers task is to coordinate the GUI with the various tasks
 * that the chat application performs - the connection of the network socket,
 * the writing of messages to the chat window, and the management of the 
 * user list for display in the user list.
 * 
 * Copyright (c) 2015, William Oliver. All rights reserved.
 * 
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 * 
 * @author William Oliver
 * @version 0.1
 */
public class ChatController {
    
    static final Logger logger = LogManager.getLogger(ChatController.class.getName());
    private MulticastSocket socket;
    boolean socketIsConnected = false;
    
    private final JTextArea chatTextArea;
    private final JTextArea userTextArea;

    private final EntityID senderId; /** Originating entity id */
    private final EntityID receiverId; /** Originating entity id */
    
    private short exerciseId; /** DIS exercise ID */
    private String username;   /** Username set for chat */
    
    /* Network and DIS receiving objects */
    private final LinkedBlockingQueue<DisIdent> pduQueue;
    private  DisReceiver disReceiver;
    private final  DisChatReceiver disChatReceiver;
    private Thread receiverThread;
    private Thread disChatThread;
    
    /* Object to send DIS comment PDUs with chat messages */
    private final LinkedBlockingQueue<DisChatMessage> sendQueue;
    private DisChatSender disChatSender;
    private Thread senderThread;
    
    /* User List */
    private final DisChatUserList userList;

    /**
     * Initialises the various components of the chat application.
     * 
     * Needs to set up the sending thread
     * and the blocking queues used to communicate. 
     * 
     * @param chatTextArea  The GUI text area for chat messages
     * @param userTextArea  The GUI text area for the user list
     */
    public ChatController(JTextArea chatTextArea, JTextArea userTextArea) {
        logger.entry(chatTextArea, userTextArea);
        
        this.chatTextArea = chatTextArea;
        this.userTextArea = userTextArea;
        /* Create queues */
        this.pduQueue = new LinkedBlockingQueue<>();
        this.sendQueue = new LinkedBlockingQueue<>();
        /* Chat receive object and thread (the network receive thread is set 
           up in the connection method. */
        this.disChatReceiver = new DisChatReceiver(pduQueue, this);
        this.disChatThread = new Thread(disChatReceiver);
        this.disChatThread.start();
        
        this.senderId = new EntityID();
        this.receiverId = new EntityID();
        
        this.userList = new DisChatUserList(userTextArea);

        logger.exit();
    }

    /**
     * Performs checks when the chat button is pressed.
     * 
     * If the checks pass the network is connected and we are online. If they
     * fail nothing is done and a message is sent to the chat window.
     * TODO -- merge with connectSocket
     */
    public void connectButtonPressed() {
        throw new UnsupportedOperationException("Not yet implimented.");
    }

    /**
     * Sends the chat message when the send button is pressed.
     * 
     * Performs some checks, and if they pass the message is sent and added to
     * the chat window. If they fail nothing is done and a message is sent to
     * the chat window.
     *
     * @param message the chat message
     */
    public void sendButtonPressed(String message) {
        logger.entry(message);
        DisChatMessage chatmsg;
        chatmsg = new DisChatMessage();
        if (message.trim().length() > 0)  {
            chatmsg.setMessage(message);
            chatmsg.setSenderId(this.senderId);
            chatmsg.setReceiverId(this.receiverId);
            chatmsg.setSendername(this.username);
            chatmsg.setExerciseId(this.exerciseId);
            logger.debug("Send button pressed - message is {}", message);
            messageReceived(chatmsg); /* Sends message to message area */

            try {
                this.sendQueue.put(chatmsg);
            } catch (InterruptedException ex) {
                logger.error("FIXME - unable to queue message!", ex);
            }
        } else {
            logger.warn("Invalid message --- ({}).", message);
        }
        logger.exit();
    }
    
    /**
     * Receives messages and displays them in the GUI.
     * 
     * When the DIS chat receiver thread receives a new message it calls this
     * to have it added to the chatTextArea. It filters out messages with
     * our own entity id as the multicast socket will also receive the 
     * messages we just sent out - resulting in duplicates.
     * 
     * TODO --- Create an updateUserList and an updateMessageArea method.
     *          This method should validate and then send on if OK
     * 
     * @param chatmsg chat message object received.
     */
    public void messageReceived(DisChatMessage chatmsg) {
        
        this.userList.add(chatmsg);
        
        /* filter out messages from ourselves. */
        if (chatmsg.getSenderId().equals(this.senderId)) {
            logger.debug("Filtering out message from myself - {} ({})", this.senderId.toString(), chatmsg.getMessage());
        } else {
            this.chatTextArea.append(chatmsg.getSenderId().toString() + ":: " + chatmsg.getMessage() + "\n");
        }
    }
    
    /**
     * Tries to create a socket on the ip and port given.
     * 
     * We use a multicast socket as we can use this to send unicast or multicast
     * UDP - but we cant multicast on a standard socket. 
     * 
     * @param ip    ip address string for the socket
     * @param port  port for the socket to use
     * @return true if the socket is connected.
     */
    public boolean connectSocket(String ip, String port) {

        logger.entry(ip, port);
        int ipport;        
        InetAddress ipaddr;

        if (!this.socketIsConnected) {
            try {
                ipaddr = InetAddress.getByName(ip);
                ipport = Integer.parseInt(port);
                this.socket = new MulticastSocket(ipport);
                if (ipaddr.isMulticastAddress()) {
                    logger.debug("InetAddress is a multicast address: {}:{}", ip, port);
                    this.socket.joinGroup(ipaddr);
                } else {
                    logger.debug("InetAddress is not a multicast  address: {}:{}", ip,port);
                }
                
            } catch (IOException|NumberFormatException e) {
                logger.error("Can't create socket({}:{}). {}", ip, port, e);
                return logger.exit(false);
            }
            this.socketIsConnected = true;
            //logger.debug("socketIsConnected is {}", this.socketIsConnected);

            logger.debug("Creating new Disreceiver thread.");
            this.disReceiver = new DisReceiver(this.socket, this.pduQueue);
            this.receiverThread = new Thread(this.disReceiver);
            this.receiverThread.start();
            logger.debug("Creating new DisChatSender thread.");
            this.disChatSender = new DisChatSender(this.socket, this.sendQueue, ipaddr, ipport);
            this.disChatThread = new Thread(this.disChatSender);
            this.disChatThread.start();
            
            return logger.exit(true);  // Should this 
        } else {
            logger.warn("connectSocket called but it appears to be connected already.");
            return logger.exit(true);
        }
    }
    
    /**
     * Disconnects from the network by closing the socket. 
     * 
     * Closing the socket will make the receiverThread and disChatThread - they
     * will catch the exception and close down. 
     * finish.
     * 
     * @return 
     */
    public boolean disConnectSocket() {
        logger.entry();
        
        if (socketIsConnected) {
            /* Shutdown the receive thread */
            logger.debug("Shutting down the receive and sending thread.");
            logger.debug("Closing socket.");
            socket.close();
            socketIsConnected = false;

            return logger.exit(true);
        } else {
            logger.warn("disConnectSocket called but it appears to be disconnected already.");
            return logger.exit(true);
        }
    }

    /**
     * Receives a string and uses it to set the sending site ID
     * @param s 
     */
    public void setSendSiteId(String s) {
        int i;
        i = Integer.parseInt(s);
        this.senderId.setSite(i);
        logger.debug("Sending Site Id set to {}", s);
    }

    /**
     * Receives a string and uses it to set the sending application ID
     * @param s 
     */
    public void setSendAppId(String s) {
        int i;
        i = Integer.parseInt(s);
        this.senderId.setApplication(i);
        logger.debug("Sending Application Id set to {}", s);
    }
    /**
     * Receives a string and uses it to set the sending entity ID
     * @param s 
     */
    public void setSendEntId(String s) {
        int i;
        i = Integer.parseInt(s);
        this.senderId.setEntity(i);
        logger.debug("Sending Entity Id set to {}", s);
    }

    /**
     * Receives a string and uses it to set the receiving Site ID
     * @param s 
     */
    public void setRecvSiteId(String s) {
        int i;
        i = Integer.parseInt(s);
        this.receiverId.setSite(i);
        logger.debug("Receiving Site Id set to {}", s);
    }

    /**
     * Receives a string and uses it to set the receiving application ID
     * @param s 
     */
    public void setRecvAppId(String s) {
        int i;
        i = Integer.parseInt(s);
        this.receiverId.setSite(i);
        logger.debug("Receiving Application Id set to {}", s);
    }

    /**
     * Receives a string and uses it to set the receiving entity ID
     * @param s 
     */
    public void setRecvEntId(String s) {
        int i;
        i = Integer.parseInt(s);
        this.receiverId.setSite(i);
        logger.debug("Receiving Entity Id set to {}", s);
    }
    
    /**
     * Sets the DIS sender ID
     * @param site
     * @param app
     * @param ent 
     */
    public void setSenderId (String site, String app, String ent) {
        //logger.debug("KILLME - {} {} {}", site, app, ent);
        int s;  /* site */
        int a;  /* application */
        int e;  /* entity */
        s = Integer.parseInt(site);
        a = Integer.parseInt(app);
        e = Integer.parseInt(ent);
        this.senderId.setSite(s);
        this.senderId.setApplication(a);  
        this.senderId.setEntity(e);
        //logger.debug("KILLME out - {} {} {}", this.senderId.getSite(), this.senderId.getApplication(), this.senderId.getEntity());
    }
    
    public EntityID getSenderId() {
        return this.senderId;
    }
    
    /**
     * Sets the DIS receiver ID
     * @param site
     * @param app
     * @param ent 
     */
    public void setReceiverId (String site, String app, String ent) {
        //logger.debug("KILLME - {} {} {}", site, app, ent);
        int s;  /* site */
        int a;  /* application */
        int e;  /* entity */
        s = Integer.parseInt(site);
        a = Integer.parseInt(app);
        e = Integer.parseInt(ent);
        this.receiverId.setSite(s);
        this.receiverId.setApplication(a);  
        this.receiverId.setEntity(e);
        //logger.debug("KILLME out - {} {} {}", this.senderId.getSite(), this.senderId.getApplication(), this.senderId.getEntity());
    }
    
    /**
     * Gets the sending entity id
     * 
     * @return the entity id
     */
    public EntityID getReceiverId() {
        return this.senderId;
    }
    
    /**
     * Gets the exercise id
     * 
     * @return the exercise id
     */
    public short getExerciseId() {
        return exerciseId;
    }

    /**
     * Sets the exercise Id
     * 
     * @param exerciseId 
     */
    public void setExerciseId(short exerciseId) {
        this.exerciseId = exerciseId;
        logger.debug("Exercise Id set to {}", exerciseId);
    }
    public void setExerciseId(String exerciseId) {
        this.exerciseId = Short.parseShort(exerciseId);
        logger.debug("Exercise Id set to {}", exerciseId);
    }

    /**
     * Gets the user name (nick name).
     * 
     * @return the user name
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     * 
     * @param username 
     */
    public void setUsername(String username) {
        this.username = username;
        logger.debug("Username set to {}", username);
    }
}
