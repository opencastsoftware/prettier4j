/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

public interface Color {
    int[] fgParams();
    int[] bgParams();

    ColorType colorType();

    static Color none() {
        return Color16.DEFAULT;
    }

    static Color black() {
        return Color16.BLACK;
    }

    static Color red() {
        return Color16.RED;
    }

    static Color green() {
        return Color16.GREEN;
    }

    static Color yellow() {
        return Color16.YELLOW;
    }

    static Color blue() {
        return Color16.BLUE;
    }

    static Color magenta() {
        return Color16.MAGENTA;
    }

    static Color cyan() {
        return Color16.CYAN;
    }

    static Color white() {
        return Color16.WHITE;
    }

    static Color brightBlack() {
        return Color16.BRIGHT_BLACK;
    }

    static Color brightRed() {
        return Color16.BRIGHT_RED;
    }

    static Color brightGreen() {
        return Color16.BRIGHT_GREEN;
    }

    static Color brightYellow() {
        return Color16.BRIGHT_YELLOW;
    }

    static Color brightBlue() {
        return Color16.BRIGHT_BLUE;
    }

    static Color brightMagenta() {
        return Color16.BRIGHT_MAGENTA;
    }

    static Color brightCyan() {
        return Color16.BRIGHT_CYAN;
    }

    static Color brightWhite() {
        return Color16.BRIGHT_WHITE;
    }

    static Color xterm256(int color) {
        return new Color256(color);
    }

    static Color rgb(int r, int g, int b) {
        return new ColorRgb(r, g, b);
    }
}
