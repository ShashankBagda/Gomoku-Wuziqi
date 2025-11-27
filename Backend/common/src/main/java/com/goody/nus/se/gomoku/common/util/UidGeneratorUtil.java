package com.goody.nus.se.gomoku.common.util;

/**
 * unique id generator util
 *
 * @author Haotian
 * @version 1.0, 2025/10/3
 */
@SuppressWarnings("MagicNumber")
public final class UidGeneratorUtil {

    private UidGeneratorUtil() {
    }

    /**
     * Generate a unique ID
     *
     * <p><ul>
     * <li>Final UID composition: 0(64bit) + timestamp(63 ~ 32 bit) + machineId(31 ~ 25 bit) + counter(24 ~ 1bit)</li>
     * <li>@see <a href="https://github.com/twitter/snowflake/tree/snowflake-2010#solution">snowflake</a></li>
     * <li>@see <a href="http://mongodb.github.io/mongo-java-driver/3.7/javadoc/org/bson/types/ObjectId.html">ObjectId</a></li>
     * </ul>
     *
     * @return generated unique ID
     */
    public static long generateUid() {
        ObjectId objectId = ObjectId.get();
        long timestamp = objectId.getTimestamp();
        long machineId = (objectId.getMachineIdentifier() & 0XEF);
        long count = (objectId.getCounter() & 0xFFFFFF);
        timestamp = timestamp << 31;
        machineId = machineId << 24;
        return (timestamp | machineId | count);
    }

    /**
     * Extract timestamp from ID
     *
     * <p>Unit: seconds
     *
     * @param id the unique ID
     * @return timestamp in seconds
     */
    public static long getTimestamp(long id) {
        return (id >> 31);
    }

    /**
     * Extract timestamp from ID (returns null if input is null)
     *
     * <p>Unit: seconds
     *
     * @param id the unique ID (nullable)
     * @return timestamp in seconds, or null if input is null
     */
    public static Long getTimestamp(Long id) {
        if (id == null) {
            return null;
        }
        return (id >> 31);
    }

    /**
     * Convert millisecond timestamp to ID
     *
     * @param timestamp timestamp in milliseconds
     * @return generated ID
     */
    public static long getId(long timestamp) {
        return (timestamp / 1000) << 31;
    }

    /**
     * Convert millisecond timestamp to ID (returns null if input is null)
     *
     * @param timestamp timestamp in milliseconds (nullable)
     * @return generated ID, or null if input is null
     */
    public static Long getId(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return (timestamp / 1000) << 31;
    }
}
