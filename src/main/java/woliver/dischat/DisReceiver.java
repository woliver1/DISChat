/*
 * DisReceiver
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

import edu.nps.moves.dis.Pdu;
import edu.nps.moves.disutil.PduFactory;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to get a datagram, see if its a PDU and pass it to a queue.
 * 
 * @author William Oliver
 * @version 0.1
 */
public class DisReceiver implements Runnable {

    static final Logger logger = LogManager.getLogger(DisReceiver.class.getName());
    private final BlockingQueue q;
    private final MulticastSocket socket;
    public static final int MAX_PDU_SIZE = 8192;
    public DisIdent disident;

    /**
     * Instantiates a network receiving thread.
     * 
     * @param socket
     * @param q 
     */
    public DisReceiver(MulticastSocket socket, BlockingQueue q) {
        logger.entry();
        this.q = q;
        this.socket = socket;
        logger.debug("KILLME - starting receiver ...({} {})", this.q, this.socket);
        logger.exit();
    }
    
    /**
     * Initiates a shutdown of the thread.
     */
    public void shutdown() {
        logger.entry();
        logger.debug("Generating an interupt (from shutdown");
        Thread.currentThread().interrupt();
        logger.exit();
    }
    
    @Override
    public void run() {
        logger.entry();
        
        PduFactory pduFactory = new PduFactory();
        Pdu pdu;
        DisIdent disobj;
        
        int packetcount = 0;
        int pducount = 0;
        int badpducount = 0;

        while (!(Thread.currentThread().isInterrupted())) {    
            
            byte buffer[];
            DatagramPacket packet;
            
            buffer = new byte[MAX_PDU_SIZE];
            packet = new DatagramPacket(buffer, buffer.length);
            logger.debug("KILLME - in loop listening to ...({} {})", this.q, this.socket);
            try {
                this.socket.receive(packet);
                logger.debug("KILLME --- packet {}!", packet.getAddress());
            } catch (IOException ex) {
                Thread.currentThread().interrupt();
                logger.error("Exception caught - receive thread closing.", ex);
            }
            packetcount++;
            byte pduBytes[] = packet.getData();
            pdu = pduFactory.createPdu(pduBytes);

            // If we got back a valid PDU, add it to our list
            if (pdu != null) {
                pducount++;
                disobj = new DisIdent(System.currentTimeMillis(), pdu);
                disobj.setIp(packet.getAddress());
                try {
                    this.q.put(disobj);
                    logger.debug("Got a new PDU (number {})", pducount);
                } catch (InterruptedException ex) {
                    logger.error("Inerupted! {}", ex);
                    this.shutdown();
                }                

            } else {
                badpducount++;
                logger.warn("Bad pdu count: {}", badpducount);
            }
        }
        logger.debug("Finishing. Received {} packets.", packetcount); 
        logger.exit();
    }
}
