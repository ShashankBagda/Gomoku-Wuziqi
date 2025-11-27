package com.goody.nus.se.gomoku.gomoku.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for RoomTypeEnum
 *
 * @author Claude
 * @version 1.0
 */
class RoomTypeEnumTest {

    @Test
    @DisplayName("Should get CASUAL from value 0")
    void testFromValueCasual() {
        RoomTypeEnum type = RoomTypeEnum.fromValue((byte) 0);
        assertEquals(RoomTypeEnum.CASUAL, type);
        assertEquals(0, type.getValue());
    }

    @Test
    @DisplayName("Should get RANKED from value 1")
    void testFromValueRanked() {
        RoomTypeEnum type = RoomTypeEnum.fromValue((byte) 1);
        assertEquals(RoomTypeEnum.RANKED, type);
        assertEquals(1, type.getValue());
    }

    @Test
    @DisplayName("Should get PRIVATE from value 2")
    void testFromValuePrivate() {
        RoomTypeEnum type = RoomTypeEnum.fromValue((byte) 2);
        assertEquals(RoomTypeEnum.PRIVATE, type);
        assertEquals(2, type.getValue());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid value 3")
    void testFromValueInvalid() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RoomTypeEnum.fromValue((byte) 3)
        );
        assertEquals("Unknown room type value: 3", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid value 99")
    void testFromValueInvalid99() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RoomTypeEnum.fromValue((byte) 99)
        );
        assertEquals("Unknown room type value: 99", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative value")
    void testFromValueNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RoomTypeEnum.fromValue((byte) -1)
        );
        assertEquals("Unknown room type value: -1", exception.getMessage());
    }

    @Test
    @DisplayName("Should verify all enum values have correct byte values")
    void testAllEnumValues() {
        assertEquals((byte) 0, RoomTypeEnum.CASUAL.getValue());
        assertEquals((byte) 1, RoomTypeEnum.RANKED.getValue());
        assertEquals((byte) 2, RoomTypeEnum.PRIVATE.getValue());
    }
}
