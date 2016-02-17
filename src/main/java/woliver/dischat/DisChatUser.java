
package woliver.dischat;

import edu.nps.moves.dis.EntityID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Holds information about a chat user.
 * 
 * Copyright (c) 2015, William Oliver. All rights reserved.
 *
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 *
 * @author William Oliver
 * @version 0.1
 */
public class DisChatUser {
    
    static final Logger logger = LogManager.getLogger(DisChatUser.class.getName());
    private EntityID id;          /** The ID of the chat user */
    private String name;          /** The name of the user */
    private DateTime lastseen;    /** The date/time of the last time we saw the user */
    private DateTime lastmessage; /** the time we last had a message */
    private DateTime firstseen;   /** The first time we saw them */

    /**
     * Takes initial values from a chat message object.
     * 
     * @param msg
     */
    public DisChatUser(DisChatMessage msg) {
        logger.entry(msg);
        this.id = msg.getSenderId();
        this.name = msg.getSendername();
        this.lastseen = new DateTime();
        logger.exit();
    }
    
    /**
     * Gets Entity ID
     * @return
     */
    public EntityID getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(EntityID id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public DateTime getLastseen() {
        return lastseen;
    }

    /**
     * 
     * @param lastseen
     */
    public void setLastseen(DateTime lastseen) {
        this.lastseen = lastseen;
    }
    
    /**
     * This sets the lastseen time to the current time
     */
    public void setLastSeenNow() {
        this.lastseen = new DateTime();
        logger.debug("Lastseen time for {} set to {}", this.getId(), this.lastseen);
    }

    /**
     *
     * @return
     */
    public DateTime getLastmessage() {
        return lastmessage;
    }

    /**
     *
     * @param lastmessage
     */
    public void setLastmessage(DateTime lastmessage) {
        this.lastmessage = lastmessage;
    }

    /**
     *
     * @return
     */
    public DateTime getFirstseen() {
        return firstseen;
    }

    /**
     *
     * @param firstseen
     */
    public void setFirstseen(DateTime firstseen) {
        this.firstseen = firstseen;
    }
}
