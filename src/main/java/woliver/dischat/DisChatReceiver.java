package woliver.dischat;

import edu.nps.moves.dis.CommentPdu;

import edu.nps.moves.dis.VariableDatum;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to parse a DIS PDU and see if it contains a chat message.
 *
 * DisChatReceiver runs as a thread. It checks its queue, if a new disident
 * object arrives in the queue it checks to see if it contains a comment pdu,
 * then tries to extract the username and the message from the comment pdu.
 *
 * Copyright (c) 2015, William Oliver. All rights reserved.
 *
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 *
 * @author William Oliver
 * @version 0.1
 */
public class DisChatReceiver implements Runnable {

    static final Logger logger = LogManager.getLogger(DisChatReceiver.class.getName());
    private final BlockingQueue pduQueue;
    private final ChatController cc;

    /**
     * Shuts down the thread.
     */
    public void shutDown() {
        logger.entry();
        logger.debug("Generating an interupt (from shutdown");
        Thread.currentThread().interrupt();
        logger.exit();
    }

    public DisChatReceiver(BlockingQueue pduQueue, ChatController cc) {
        logger.entry(pduQueue);
        this.pduQueue = pduQueue;
        this.cc = cc;
        logger.exit();
    }

    /**
     * We receive a PDU - check if its a comment and call processPdu
     */
    @Override
    public void run() {
        logger.entry();
        DisIdent disobj;
        while (!(Thread.currentThread().isInterrupted())) {
            try {
                disobj = (DisIdent) this.pduQueue.take();
                logger.debug("got PDU of type: {}", disobj.getPdu().getClass().getName());
                if (disobj.getPdu() instanceof CommentPdu) {
                    logger.trace("Sending comment pdu to processPdu method");
                    processPdu(disobj);
                }
            } catch (InterruptedException ex) {
                logger.error("Generating an interupt.", ex);
                Thread.currentThread().interrupt();
            }
        }
        logger.exit();
    }

    /**
     * Processes the comment PDU to see if its a chat message.
     *
     * If it is, extract the message and give it to the TODO datumid = 600001
     * --- username datumid = 600002 --- Message
     *
     * TODO --- consider merging with DisTimeReceiver.
     *
     * @param pdu
     */
    private void processPdu(DisIdent disobj) {
        logger.entry(disobj);
        long numVarDatums;
        long datumlength;
        long datumid;
        byte[] datum;
        CommentPdu pdu;
        String decodeddatum;
        DisChatMessage chatmsg;
        List<VariableDatum> datumlist;
        pdu = (CommentPdu) disobj.getPdu();
        numVarDatums = pdu.getNumberOfVariableDatumRecords();
        datumlist = pdu.getVariableDatums();
        boolean isMessage = false;

        chatmsg = new DisChatMessage();

        for (VariableDatum d : datumlist) {
            datumlength = d.getVariableDatumLength();
            datumid = d.getVariableDatumID();
            datum = d.getVariableData();
            if (datum.length > datumlength) {
                logger.warn("Length ({}) exceeds the size parameter ({})", datum.length, datumlength);
            } else {
                try {
                    if (datumid == 600001) {
                        isMessage = true;
                        decodeddatum = new String(datum, "UTF-8");
                        chatmsg.setSendername(decodeddatum);

                    } else if (datumid == 600002) {
                        isMessage = true;
                        decodeddatum = new String(datum, "UTF-8");
                        chatmsg.setMessage(decodeddatum);
                        //chatmsg.setSenderip(disobj.getIp().toString());
                        //cc.messageReceived(chatmsg);
                    }
                } catch (UnsupportedEncodingException ex) {
                    logger.error(ex.getMessage());
                }

            }
        }
        if (isMessage) {
            chatmsg.setSenderip(disobj.getIp().toString());
            chatmsg.setSenderId(pdu.getOriginatingEntityID());
            chatmsg.setReceiverId(pdu.getReceivingEntityID());
            logger.debug("KILLME chat msg is {} from {} ({})",
                    chatmsg.getMessage(),
                    chatmsg.getSenderip(),
                    chatmsg.getSendername());
            cc.messageReceived(chatmsg);
        }
        logger.exit();
    }
}
