package com.goody.nus.se.gomoku.gomoku.enums;

/**
 * Room status enum
 */
public enum RoomStatusEnum {
    /**
     * Waiting for players
     */
    WAITING((byte) 0),

    /**
     * Two players matched, ready to start
     */
    MATCHED((byte) 1),

    /**
     * Game is in progress
     */
    PLAYING((byte) 2),

    /**
     * Game finished
     */
    FINISHED((byte) 3);

    private final byte value;

    RoomStatusEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    /**
     * Get enum from byte value
     *
     * @param value byte value
     * @return RoomStatus enum
     */
    public static RoomStatusEnum fromValue(byte value) {
        for (RoomStatusEnum status : RoomStatusEnum.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown room status value: " + value);
    }
}
