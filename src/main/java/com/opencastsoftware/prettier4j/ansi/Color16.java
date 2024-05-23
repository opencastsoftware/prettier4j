/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

enum Color16 implements Color {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),

    DEFAULT(9),

    BRIGHT_BLACK(60),
    BRIGHT_RED(61),
    BRIGHT_GREEN(62),
    BRIGHT_YELLOW(63),
    BRIGHT_BLUE(64),
    BRIGHT_MAGENTA(65),
    BRIGHT_CYAN(66),
    BRIGHT_WHITE(67);

    private final int code;

    Color16(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public int fgCode() {
        return code + 30;
    }

    @Override
    public int[] fgParams() {
        return new int[] { fgCode() };
    }

    public int bgCode() {
        return code + 40;
    }

    @Override
    public int[] bgParams() {
        return new int[] { bgCode() };
    }

    @Override
    public ColorType colorType() {
        return ColorType.COLOR_16;
    }

    static Color16 withCode(int code) {
        for (Color16 value : values()) {
            if (value.code() == code) {
                return value;
            }
        }

        return null;
    }
}
