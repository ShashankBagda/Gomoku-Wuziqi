package com.goody.nus.se.gomoku.gomoku.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for PlayerColor enum
 *
 * @author Claude
 * @version 1.0
 */
class PlayerColorTest {

    @Test
    @DisplayName("Should get BLACK from value 1")
    void testFromValueBlack() {
        PlayerColor color = PlayerColor.fromValue(1);
        assertEquals(PlayerColor.BLACK, color);
        assertEquals(1, color.getValue());
    }

    @Test
    @DisplayName("Should get WHITE from value 2")
    void testFromValueWhite() {
        PlayerColor color = PlayerColor.fromValue(2);
        assertEquals(PlayerColor.WHITE, color);
        assertEquals(2, color.getValue());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid value")
    void testFromValueInvalid() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PlayerColor.fromValue(99)
        );
        assertEquals("Invalid player color value: 99", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for value 0")
    void testFromValueZero() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PlayerColor.fromValue(0)
        );
        assertEquals("Invalid player color value: 0", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative value")
    void testFromValueNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> PlayerColor.fromValue(-1)
        );
        assertEquals("Invalid player color value: -1", exception.getMessage());
    }

    @Test
    @DisplayName("Should get WHITE as opponent of BLACK")
    void testGetOpponentBlack() {
        assertEquals(PlayerColor.WHITE, PlayerColor.BLACK.getOpponent());
    }

    @Test
    @DisplayName("Should get BLACK as opponent of WHITE")
    void testGetOpponentWhite() {
        assertEquals(PlayerColor.BLACK, PlayerColor.WHITE.getOpponent());
    }

    @Test
    @DisplayName("Should verify opponent relationship is symmetric")
    void testOpponentSymmetric() {
        PlayerColor black = PlayerColor.BLACK;
        PlayerColor white = black.getOpponent();
        assertEquals(PlayerColor.WHITE, white);
        assertEquals(black, white.getOpponent());
    }
}
