/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

/**
 * This enum represents the different color types that can be applied via
 * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_(Select_Graphic_Rendition)_parameters">Select Graphic Rendition parameters</a>.
 * <p>
 * This class is mainly used to store bit flags that are set into display attributes via
 * {@link Styles#fg(Color) fg} and {@link Styles#bg(Color) bg} and read from display attributes via
 * {@link Attrs#fgColorType(long) fgColorType} and {@link Attrs#bgColorType(long) bgColorType}.
 */
public enum ColorType {
    COLOR_16(1),
    COLOR_XTERM(2),
    COLOR_RGB(4);

    private final int code;

    ColorType(int code) {
        this.code = code;
    }

    public int code() {
        return this.code;
    }

    public static ColorType withCode(int code) {
        for (ColorType value : values()) {
            if (value.code() == code) {
                return value;
            }
        }

        return null;
    }
}
