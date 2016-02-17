
package woliver.dischat;

import edu.nps.moves.dis.CommentPdu;
import edu.nps.moves.dis.VariableDatum;
import edu.nps.moves.disutil.DisTime;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to send dischat messages on a network socket.
 * 
 * The class listens to a queue and upon receipt of a chat message, creates
 * a new comment PDU, packs the message into the PDU and sends it via
 * the network socket.
 * 
 * 
 * Copyright (c) 2015, William Oliver. All rights reserved.
 *
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 *
 * @author William Oliver
 * @version 0.1
 */
public class DisChatSender implements Runnable {

    static final Logger logger = LogManager.getLogger(DisChatSender.class.getName());

    private final BlockingQueue q;
    private final MulticastSocket socket;
    private final InetAddress ipaddr;
    private final int port;
    
    /**
     * Instantiates a thread to send DIS Comment pdus to a network socket.
     * 
     * @param socket  
     * @param q 
     * @param ipaddr 
     * @param port 
     */
    public DisChatSender(MulticastSocket socket, BlockingQueue q, InetAddress ipaddr, int port) {
        logger.entry();
        this.q = q;
        this.socket = socket;
        this.ipaddr = ipaddr;
        this.port = port;
        logger.trace("KILLME - starting sender ...({} {})", this.q, this.socket);
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
    
    /**
     * Receive chat messages from the queue, marshall them to a pdu and send.
     */
    @Override
    public void run() {
        logger.entry();
        DisChatMessage chatmsg;
        while (!(Thread.currentThread().isInterrupted())) {
            try {
                chatmsg = (DisChatMessage) this.q.take();
                sendMessage(chatmsg);
            } catch (InterruptedException ex) {
                logger.error("Generating an interupt.", ex);
                Thread.currentThread().interrupt();
            }
        }
        logger.exit();
    }
    
    /**
     * Packs the contents of a DisChatMessage into a Comment PDU and sends it.
     * 
     * The various attributes of the chat message object are mapped to the 
     * DIS Comment PDU fields and the PDU is then sent out on the network.
     * The following shows the fields in a DIS header and if we have to 
     * set them or the PDU class does it.
     * 
     * DIS header 
     *  | Field            | Set by  |
     *  |------------------+---------|
     *  | Protocol Version | OpenDIS |
     *  | Exercise ID      | Us      |
     *  | PDU Type         | OpenDIS |
     *  | Protocol Family  | OpenDIS |
     *  | Timestamp        | Us      |
     *  | Length           | OpenDIS |
     *  | PDU Status       | ???     |
     *
     * @param chatmsg 
     */
    private void sendMessage(DisChatMessage chatmsg) {
        logger.entry(chatmsg);
        
        DisTime disTime = DisTime.getInstance();
        CommentPdu cpdu = new CommentPdu();
        List<VariableDatum> datums = new ArrayList();

        cpdu.setExerciseID(chatmsg.getExerciseId());
        cpdu.setTimestamp(disTime.getDisAbsoluteTimestamp());

        /*
         * Now we set the originating and receiving IDs
         */
        //EntityID originatingeid = cpdu.getOriginatingEntityID();
        cpdu.setOriginatingEntityID(chatmsg.getSenderId());
        cpdu.setReceivingEntityID(chatmsg.getReceiverId());

        /*
         * Add the three variable datum records.
         */
        datums.add(createDatum(chatmsg.getSendername(), 600001));
        datums.add(createDatum(chatmsg.getMessage(), 600002));
        
        cpdu.setVariableDatums(datums);

        /*
         * Now write the PDU out
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        cpdu.marshal(dos);

        /*
         * The byte array here is the packet in DIS format. We put that into a
         * datagram and send it.
         */
        byte[] data = baos.toByteArray();

        DatagramPacket packet;
        try {
            packet = new DatagramPacket(data, data.length, this.ipaddr, this.port);
            this.socket.send(packet);
            logger.debug("Packet sent on {}:{}", this.ipaddr, this.port); 
        } catch (SocketException ex) {
            logger.error("Unable to send packet", ex);
        } catch (IOException ex) {
            logger.error("Unable to send packet", ex);
        } catch (java.lang.IllegalArgumentException ex) {
            logger.error("Unable to send packet. Illegal arg");
            logger.error("Packet length {}, IP FIXME and port {}.", data.length, this.socket.getLocalPort());
            logger.error(ex);
            System.exit(-2);
        }
        logger.exit();
    }
    
    /**
     * Creates a variable datum.
     *
     * @param str       The string to pack into the variable datum
     * @param datumid   the identifier of the datum
     * @return          a new VariableDatum
     */
    private VariableDatum createDatum(String str, int datumid) {
        logger.entry(str, datumid);
        
        VariableDatum datum = new VariableDatum();
        logger.debug("Message and datum id {} {}", str, datumid);
        byte[] b = str.getBytes(Charset.forName("UTF-8"));
        datum.setVariableDatumID(datumid);
        datum.setVariableDatumLength(b.length * 8);
        datum.setVariableData(b);
        return logger.exit(datum);
    }
}
