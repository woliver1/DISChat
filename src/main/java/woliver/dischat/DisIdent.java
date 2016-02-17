
package woliver.dischat;

import edu.nps.moves.dis.Pdu;
import edu.nps.moves.disutil.DisTime;
import static java.lang.Math.floor;
import java.net.InetAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

/**
 * Holds a DIS object.
 *
 * TODO merge with DisScenarion DisIdent
 *
 * Copyright (c) 2015, William Oliver. All rights reserved.
 *
 * This work is licensed under the GPL Version 3 open source license, available
 * at http://www.gnu.org/licenses/
 *
 * @author William Oliver
 * @version 0.1
 */
public class DisIdent {

    static final Logger logger = LogManager.getLogger(DisIdent.class.getName());

    private final long receivetime;
    private Pdu pdu;
    private boolean isabsolutetime;
    private double disSecPastHour;
    private long disMillisPastHour;
    private long disMillis;
    private DateTime distime;
    private InetAddress ip;



    public DisIdent(long timestamp, Pdu pdu) {

        this.receivetime = timestamp;
        this.pdu = pdu;       
        // Extract the timestamp from the PDU and turn it into something useful.
        this.extractTimeStamp(this.pdu.getTimestamp());
    }

    /**
     * 
     * @param ts The DIS timestamp
     */
    private void extractTimeStamp(long ts) {

        long min;
        long sec;
        long millis;
        DateTime dt = new DateTime(); // Current Time

        this.disSecPastHour = (double) (ts >> 1) * 3600.0 / 2147483648.0;
        this.disMillisPastHour = (long) (this.disSecPastHour * 1000);
        this.isabsolutetime = ((int) ts & DisTime.ABSOLUTE_TIMESTAMP_MASK) == 1;
        min = (int) (this.disSecPastHour / 60);
        sec = (int) (this.disSecPastHour - (min * 60));
        millis = (int) ((this.disSecPastHour - floor(this.disSecPastHour)) * 1000);
        
        //sec -= (min * 60);
        /*logger.debug("-----  ");
        logger.debug("raw distime {}", ts);
        logger.debug("distime     {}", this.disSecPastHour);
        logger.debug("mins        {}", min);
        logger.debug("sec         {}", sec);
        logger.debug("millis      {}", millis);
        logger.debug("Time is     {}", (this.isabsolutetime ? "ABS" : "REL"));
        //logger.debug("ZZZ Datetime    {}", this.distime);
        logger.debug("Recievetime {}", this.receivetime);
        logger.debug("curretnt dt {}", dt); 
        */
        if ((min >= 0) && (sec >= 0) && (millis >= 0))
        {
            int Y = dt.getYear();
            int M = dt.getMonthOfYear();
            int D = dt.getDayOfMonth();
            int h = dt.getHourOfDay();
            int m = (int) min;
            int s = (int) sec;
            int ms = (int) millis;
            try {
                this.distime = new DateTime(Y, M, D, h, m, s, ms);
                logger.debug("Dis time    {}", this.distime);
            } catch (org.joda.time.IllegalFieldValueException ex) {
                logger.debug("Time Bad!!!!!!!!!!!!!!!!");
                logger.debug("{} {} {} {} {} {} {}, Y, M, D, h, m, s, ms");
                logger.debug("Exception {}", ex);
            }
        }
        else
        {
            logger.debug("Dis time     BAD");
        }
        logger.debug("Done ---------------------");
    }
    
    /* Getters and Setters */
    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public Pdu getPdu() {
        return pdu;
    }

    public void setPdu(Pdu pdu) {
        this.pdu = pdu;
    }
    
}

