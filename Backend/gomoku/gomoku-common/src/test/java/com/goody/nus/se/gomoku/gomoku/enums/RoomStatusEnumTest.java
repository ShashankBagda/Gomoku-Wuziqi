package com.goody.nus.se.gomoku.gomoku.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for RoomStatusEnum
 *
 * @author Claude
 * @version 1.0
 */
class RoomStatusEnumTest {

    @Test
    @DisplayName("Should get WAITING from value 0")
    void testFromValueWaiting() {
        RoomStatusEnum status = RoomStatusEnum.fromValue((byte) 0);
        assertEquals(RoomStatusEnum.WAITING, status);
        assertEquals(0, status.getValue());
    }

    @Test
    @DisplayName("Should get MATCHED from value 1")
    void testFromValueMatched() {
        RoomStatusEnum status = RoomStatusEnum.fromValue((byte) 1);
        assertEquals(RoomStatusEnum.MATCHED, status);
        assertEquals(1, status.getValue());
    }

    @Test
    @DisplayName("Should get PLAYING from value 2")
    void testFromValuePlaying() {
        RoomStatusEnum status = RoomStatusEnum.fromValue((byte) 2);
        assertEquals(RoomStatusEnum.PLAYING, status);
        assertEquals(2, status.getValue());
    }

    @Test
    @DisplayName("Should get FINISHED from value 3")
    void testFromValueFinished() {
        RoomStatusEnum status = RoomStatusEnum.fromValue((byte) 3);
        assertEquals(RoomStatusEnum.FINISHED, status);
        assertEquals(3, status.getValue());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid value 4")
    void testFromValueInvalid() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RoomStatusEnum.fromValue((byte) 4)
        );
        assertEquals("Unknown room status value: 4", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid value 99")
    void testFromValueInvalid99() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RoomStatusEnum.fromValue((byte) 99)
        );
        assertEquals("Unknown room status value: 99", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative value")
    void testFromValueNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> RoomStatusEnum.fromValue((byte) -1)
        );
        assertEquals("Unknown room status value: -1", exception.getMessage());
    }

    @Test
    @DisplayName("Should verify all enum values have correct byte values")
    void testAllEnumValues() {
        assertEquals((byte) 0, RoomStatusEnum.WAITING.getValue());
        assertEquals((byte) 1, RoomStatusEnum.MATCHED.getValue());
        assertEquals((byte) 2, RoomStatusEnum.PLAYING.getValue());
        assertEquals((byte) 3, RoomStatusEnum.FINISHED.getValue());
    }
}
