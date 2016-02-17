package woliver.dischat;

import edu.nps.moves.dis.EntityID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to hold an individual DIS Chat message.
 * 
 * All messages sent and received are put in a chatmessage object.
 * It contains a the IDs and for received messages, the IP address.
 * 
 * Copyright (c) 2015, William Oliver. All rights reserved.
 * 
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 * 
 * @author William Oliver
 * @version 0.1
 */
public class DisChatMessage {
    static final Logger logger = LogManager.getLogger(DisChatMessage.class.getName());
    private String message;       /** The chat message */
    private String sendername;    /** The username of the sender */
    private short exerciseId;     /** The DIS exercise id */
    
    private EntityID senderId;    /** Entity id of the Sender */
    private EntityID receiverId;  /** Entity id of the receiver */
    private String senderip;      /** The senders ip address */

    public DisChatMessage() {
        this.senderId = new EntityID();
        this.receiverId = new EntityID();
    }
    /**
     *  Gets the message
     * @return 
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message.trim();
    }

    public String getSendername() {
        return sendername;
    }

    public void setSendername(String sendername) {
        this.sendername = sendername.trim();
    }

    public short getExerciseId() {
        return this.exerciseId;
    }

    public void setExerciseId(short exid) {
        this.exerciseId = exid;
        //System.out.println("Exercise ID set to " + this.exerciseId);
    }

    public String getSenderip() {
        return senderip;
    }

    public void setSenderip(String senderip) {
        this.senderip = senderip;
    }
   
    /**
     * Sets the DIS sender ID
     * @param eid 
     */
    public void setSenderId(EntityID eid) {
        this.senderId = eid;
    }
   
    /**
     * Gets the sending entity ID
     *
     * @return the entity ID
     */
    public EntityID getSenderId() {
        return this.senderId;
    }
    
    /**
     * Sets the DIS receiver ID
     */
    public void setReceiverId (EntityID eid) {
        this.receiverId = eid;
    }
    
    /**
     * Gets the receiving entity ID
     *
     * @return the entity ID
     */
    public EntityID getReceiverId() {
        return this.receiverId;
    }

}
