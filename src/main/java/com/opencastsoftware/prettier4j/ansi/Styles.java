/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import java.util.function.LongUnaryOperator;

import static com.opencastsoftware.prettier4j.ansi.Attrs.*;

public class Styles {
    public static LongUnaryOperator fg(Color color) {
        return attrs -> withFgColor(attrs, color);
    }

    public static LongUnaryOperator bg(Color color) {
        return attrs -> withBgColor(attrs, color);
    }

    public static LongUnaryOperator bold() {
        return attrs -> attrs | 1;
    }

    public static LongUnaryOperator faint() {
        return attrs -> attrs | (1 << 1);
    }

    public static LongUnaryOperator italic() {
        return attrs -> attrs | (1 << 2);
    }

    public static LongUnaryOperator underline() {
        return attrs -> attrs | (1 << 3);
    }

    public static LongUnaryOperator blink() {
        return attrs -> attrs | (1 << 4);
    }

    public static LongUnaryOperator inverse() {
        return attrs -> attrs | (1 << 5);
    }

    public static LongUnaryOperator strikethrough() {
        return attrs -> attrs | (1 << 6);
    }

    private static long withFgColor(long attrs, Color color) {
        long withColorType = withColorType(attrs, color, FG_COLOR_TYPE_MASK, FG_COLOR_TYPE_SHIFT);
        return withColor(withColorType, color, FG_COLOR_MASK, FG_COLOR_SHIFT);
    }

    private static long withBgColor(long attrs, Color color) {
        long withColorType = withColorType(attrs, color, BG_COLOR_TYPE_MASK, BG_COLOR_TYPE_SHIFT);
        return withColor(withColorType, color, BG_COLOR_MASK, BG_COLOR_SHIFT);
    }

    private static long withColorType(long attrs, Color color, long maskValue, long shiftValue) {
        int colorTypeCode = color.colorType().code();
        long noColorType = attrs & maskValue;
        long newColorType = (long) colorTypeCode << shiftValue;
        return noColorType | newColorType;
    }

    private static long withColor(long attrs, Color color, long maskValue, long shiftValue) {
        long noColor = attrs & maskValue;
        long newColor = 0L;

        switch (color.colorType()) {
            case COLOR_16:
                Color16 color16 = (Color16) color;
                newColor = color16.code();
                break;
            case COLOR_256:
                Color256 color256 = (Color256) color;
                newColor = color256.color();
                break;
            case COLOR_RGB:
                ColorRgb colorRgb = (ColorRgb) color;
                newColor = colorRgb.packed();
                break;
        }

        return noColor | (newColor << shiftValue);
    }
}
