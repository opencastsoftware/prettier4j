/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

enum DisplayStyle {
    RESET(0),

    // Display styles
    BOLD(1),
    FAINT(2),
    ITALIC(3),
    UNDERLINE(4),
    BLINK(5),
    INVERSE(7),
    STRIKETHROUGH(9),

    // Disable display styles
    NORMAL_INTENSITY(22),
    ITALIC_OFF(23),
    UNDERLINE_OFF(24),
    BLINK_OFF(25),
    INVERSE_OFF(27),
    STRIKETHROUGH_OFF(29);

    private final int code;

    DisplayStyle(int code) {
        this.code = code;
    }

    int code() {
        return this.code;
    }
}
