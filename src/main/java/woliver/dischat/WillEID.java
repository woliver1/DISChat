/*
 * WillEID
 *
 * Version 0.1
 * 17/2/16
 *
 * Copyright (c) 2015, William Oliver. All rights reserved.
 *
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 */
package woliver.dischat;

import edu.nps.moves.dis.EntityID;

/**
 * Holds a DIS entity identifier
 * 
 * The OpenDIS provided EntityID class does not have a sensible toString
 * method, this class simply extends it to have a toString that outputs the DIS
 * entity id in a more usual (for DIS) format.
 * @author woliver
 * @version 0.1
 */
public class WillEID extends EntityID {

    @Override
    public String toString() {
        return super.getSite() + ":" + super.getApplication() + ":" +super.getEntity();
    }
   
    
}
