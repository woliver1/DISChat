
package woliver.dischat;

import edu.nps.moves.dis.EntityID;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JTextArea;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Maintains a list of chat users.
 * 
 * Copyright (c) 2015, William Oliver. All rights reserved.
 *
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 *
 * @author William Oliver
 * @version 0.1
 */
public class DisChatUserList {
    
    static final Logger logger = LogManager.getLogger(DisChatUserList.class.getName());
    
    private final Map<EntityID, DisChatUser>  userList;
    private final JTextArea userTextArea;
    
    public DisChatUserList(JTextArea userTextArea) {
        logger.entry(userTextArea);
        this.userList = new LinkedHashMap();
        this.userTextArea = userTextArea;
        logger.exit();
    }

    /**
     * Adds a new user to the list if we have not seen them before.
     * 
     * @param chatmsg 
     */
    void add(DisChatMessage chatmsg) {
        logger.entry(chatmsg);
        DisChatUser chatuser;
        
        chatuser = new DisChatUser(chatmsg);
        
        if (this.userList.containsKey(chatuser.getId())) {
            this.userList.get(chatuser.getId()).setLastSeenNow();
            //val.setLastSeenNow();
            logger.debug("Updating last seen time for user {}", chatuser.getId());
        } else {
            this.userList.put(chatuser.getId(), chatuser);
            logger.debug("Adding chatuser {}", chatuser.getId());
        }
        /* Now update the canvas */
        updateUserArea();
        logger.exit();
    }
    
    /**
     * Updates the user list in the GUI
     */
    private void updateUserArea() {
        logger.entry();
        String userstring;
        EntityID k;
        DisChatUser v;
        StringBuilder sb = new StringBuilder();
        
        userstring = "";
        for (Map.Entry pair : this.userList.entrySet()) {
            logger.debug("{} = {}", pair.getKey(), pair.getValue());
            k = (EntityID) pair.getKey();
            v = (DisChatUser) pair.getValue();
            sb.append(v.getId().toString());
            sb.append(" (");
            sb.append(v.getName());
            sb.append(")");
            sb.append("\n");
            sb.toString();
            logger.debug("K: {}   V:{}",sb.toString(), v.getName());
        }
        this.userTextArea.setText(sb.toString());
        logger.exit();
    }
}
