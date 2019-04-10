/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Stepan
 */
public class UTCTimeRange extends Pair<Long, Long> {

    private final static Logger logger = LogManager.getLogger();

    public UTCTimeRange(Long key, Long value) {
        super(key, value);
    }

    public UTCTimeRange(int key, int value) {
        super(new Long(key), new Long(value));
    }

    public UTCTimeRange() {
        super(new Long(0), new Long(0));
    }

    public void setStart(Long t) {
        setKey(t);
    }

    public void setEnd(Long t) {
        setValue(t);
    }

    public Long getStart() {
        return getKey();
    }

    public Long getEnd() {
        return getValue();
    }

    static public long getUtcTime(LocalDateTime dateTime, int adjustment) {
        return getUtcTime(dateTime, zoneId, adjustment);
    }
    public static ZoneId zoneId = ZoneId.systemDefault();

    public static void setZoneId(ZoneId zoneId) {
        UTCTimeRange.zoneId = zoneId;
    }

    static public Long getUtcTime(LocalDateTime dateTime, ZoneId _zoneID, int adjustment) {
        logger.debug("getUtcTime " + dateTime);
        return (dateTime.toInstant(_zoneID.getRules().getOffset(dateTime)).getEpochSecond() + adjustment) * 1000;

    }
}
