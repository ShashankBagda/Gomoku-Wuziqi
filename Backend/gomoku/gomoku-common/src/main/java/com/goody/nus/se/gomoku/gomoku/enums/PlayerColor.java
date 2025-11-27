package com.goody.nus.se.gomoku.gomoku.enums;

import lombok.Getter;

/**
 * Player color in Gomoku game
 */
@Getter
public enum PlayerColor {
    BLACK(1),
    WHITE(2);

    private final int value;

    PlayerColor(int value) {
        this.value = value;
    }

    /**
     * Get PlayerColor from integer value
     */
    public static PlayerColor fromValue(int value) {
        for (PlayerColor color : values()) {
            if (color.value == value) {
                return color;
            }
        }
        throw new IllegalArgumentException("Invalid player color value: " + value);
    }

    /**
     * Get opponent's color
     */
    public PlayerColor getOpponent() {
        return this == BLACK ? WHITE : BLACK;
    }
}
