/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

public enum ColorType {
    COLOR_16(1),
    COLOR_256(2),
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
