/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package woliver.dischat;

import edu.nps.moves.dis.EntityID;

/**
 *
 * Copyright (c) 2015, William Oliver. All rights reserved.
 *
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 *
 * @author William Oliver
 * @version 0.1
 */
public class WillEID extends EntityID {

    @Override
    public String toString() {
        return super.getSite() + ":" + super.getApplication() + ":" +super.getEntity();
    }
   
    
}
