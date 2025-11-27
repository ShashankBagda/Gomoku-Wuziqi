package com.goody.nus.se.gomoku.gomoku.enums;

/**
 * Room type enum
 */
public enum RoomTypeEnum {
    /**
     * Casual/Public match (join by room code)
     */
    CASUAL((byte) 0),

    /**
     * Ranked match (auto matching)
     */
    RANKED((byte) 1),

    /**
     * Private room (with password)
     */
    PRIVATE((byte) 2);

    private final byte value;

    RoomTypeEnum(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    /**
     * Get enum from byte value
     *
     * @param value byte value
     * @return RoomType enum
     */
    public static RoomTypeEnum fromValue(byte value) {
        for (RoomTypeEnum type : RoomTypeEnum.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown room type value: " + value);
    }
}
