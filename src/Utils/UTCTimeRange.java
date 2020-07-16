/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * @author Stepan
 */
public class UTCTimeRange extends Pair<Long, Long> {

    public static ZoneId zoneId = ZoneId.systemDefault();

    static public long getUtcTime(LocalDateTime dateTime, int adjustment) {
        return getUtcTime(dateTime, zoneId, adjustment);
    }

    public static void setZoneId(ZoneId zoneId) {
        UTCTimeRange.zoneId = zoneId;
    }

    static public Long getUtcTime(LocalDateTime dateTime, ZoneId _zoneID, int adjustment) {
        return (dateTime.toInstant(_zoneID.getRules().getOffset(dateTime)).getEpochSecond() + adjustment) * 1000;

    }

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

    private String getLocalDateTime(long l) {
        if (l == 0) {
            return "[]";
        } else {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(l), zoneId).toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("from: ").append(getLocalDateTime(getStart())).append(" to: ")
                .append(getLocalDateTime(getEnd()));
        return ret.toString();
    }

}
