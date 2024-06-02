/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * This class contains utilities for operating on display attributes.
 * <p>
 * We use a packed representation which stores the display attribute states in a {@code long}.
 * <p>
 * This approach is inspired by Li Haoyi's post <a href="https://www.lihaoyi.com/post/MicrooptimizingyourScalacode.html">Micro-optimizing your Scala code</a>,
 * which describes the techniques used in the <a href="https://github.com/com-lihaoyi/fansi">fansi</a> library to represent ANSI styled text.
 * <p>
 * The attributes are laid out as follows:
 * <pre>{@code
 * 63                47
 *  00000000 00000000 00000000 00000000
 *  |__||__| |________________________|
 *  |   |    | background color
 *  |   | foreground color type
 *  | background coloor type
 *
 * 31                15
 *  00000000 00000000 00000000 00000000
 *  |________________________| |______|
 *  |                          | display styles
 *  | foreground color
 * }</pre>
 * <p>
 * This layout ensures that {@link DisplayStyle}s can be treated as simple bit flags.
 */
@API(status = INTERNAL, consumers = {"com.opencastsoftware.prettier4j"})
public class Attrs {
    private static final int MAX_SGR_PARAMS = 16;

    static final long FG_COLOR_TYPE_SHIFT = Byte.SIZE * 7;
    static final long FG_COLOR_SHIFT = Byte.SIZE;
    static final long FG_COLOR_MASK = 0xF0FF_FFFF_0000_00FFL;

    static final long BG_COLOR_TYPE_SHIFT = Byte.SIZE * 7 + 4;
    static final long BG_COLOR_SHIFT = Byte.SIZE * 4;
    static final long BG_COLOR_MASK = 0x0F00_0000_FFFF_FFFFL;

    public static final long EMPTY = 0L;
    public static final long NULL = -1;

    private Attrs() {}

    public static long withStyles(long attrs, Styles.StylesOperator ...styles) {
        for (Styles.StylesOperator style : styles) {
            attrs = style.applyAsLong(attrs);
        }
        return attrs;
    }

    public static String transition(long prev, long next) {
        if (next <= EMPTY) {
            return isEmpty(prev) ? "" : AnsiConstants.RESET;
        }

        int[] sgrParams = new int[MAX_SGR_PARAMS];
        int sgrParamsCount = 0;

        // Bold & faint are both reset by the same SGR code
        if ((isBold(next) || isFaint(next)) ^ (isBold(prev) || isFaint(prev))) {
            if (isBold(next)) {
                sgrParams[sgrParamsCount++] = DisplayStyle.BOLD.code();
            } else if (isFaint(next)) {
                sgrParams[sgrParamsCount++] = DisplayStyle.FAINT.code();
            } else {
                sgrParams[sgrParamsCount++] = DisplayStyle.NORMAL_INTENSITY.code();
            }
        }

        if (isItalic(next) ^ isItalic(prev)) {
            sgrParams[sgrParamsCount++] = isItalic(next)
                ? DisplayStyle.ITALIC.code()
                : DisplayStyle.ITALIC_OFF.code();
        }

        if (isUnderline(next) ^ isUnderline(prev)) {
            sgrParams[sgrParamsCount++] = isUnderline(next)
                ? DisplayStyle.UNDERLINE.code()
                : DisplayStyle.UNDERLINE_OFF.code();
        }

        if (isBlink(next) ^ isBlink(prev)) {
            sgrParams[sgrParamsCount++] = isBlink(next)
                ? DisplayStyle.BLINK.code()
                : DisplayStyle.BLINK_OFF.code();
        }

        if (isInverse(next) ^ isInverse(prev)) {
            sgrParams[sgrParamsCount++] = isInverse(next)
                ? DisplayStyle.INVERSE.code()
                : DisplayStyle.INVERSE_OFF.code();
        }

        if (isStrikethrough(next) ^ isStrikethrough(prev)) {
            sgrParams[sgrParamsCount++] = isStrikethrough(next)
                ? DisplayStyle.STRIKETHROUGH.code()
                : DisplayStyle.STRIKETHROUGH_OFF.code();
        }

        Color nextFgColor = fgColor(next);
        Color prevFgColor = fgColor(prev);

        if (nextFgColor != null && !nextFgColor.equals(prevFgColor)) {
            for (int param : nextFgColor.fgParams()) {
                sgrParams[sgrParamsCount++] = param;
            }
        } else if (nextFgColor == null && prevFgColor != null) {
            sgrParams[sgrParamsCount++] = Color16.DEFAULT.fgCode();
        }

        Color nextBgColor = bgColor(next);
        Color prevBgColor = bgColor(prev);

        if (nextBgColor != null && !nextBgColor.equals(prevBgColor)) {
            for (int param : nextBgColor.bgParams()) {
                sgrParams[sgrParamsCount++] = param;
            }
        } else if (nextBgColor == null && prevBgColor != null) {
            sgrParams[sgrParamsCount++] = Color16.DEFAULT.bgCode();
        }

        if (sgrParamsCount == 0) return "";

        StringBuilder result = new StringBuilder(AnsiConstants.CSI);

        for (int i = 0; i < sgrParamsCount; i++) {
            if (i > 0) result.append(';');
            result.append(sgrParams[i]);
        }

        return result.append('m').toString();
    }

    private static ColorType colorType(long attrs, long shiftValue) {
        int colorTypeCode = (int) (attrs >>> shiftValue) & 0xF;
        if (colorTypeCode == 0) return null;
        return ColorType.withCode(colorTypeCode);
    }

    public static ColorType fgColorType(long attrs) {
        if (attrs == Attrs.NULL) return null;
        return colorType(attrs, FG_COLOR_TYPE_SHIFT);
    }

    public static ColorType bgColorType(long attrs) {
        if (attrs == Attrs.NULL) return null;
        return colorType(attrs, BG_COLOR_TYPE_SHIFT);
    }

    private static Color color(long attrs, ColorType colorType, long shiftValue) {
        if (colorType == null) return null;

        int colorBits = (int) (attrs >>> shiftValue) & 0xFFFFFF;

        switch (colorType) {
            case COLOR_16:
                return Color16.withCode(colorBits);
            case COLOR_XTERM:
                return new ColorXterm(colorBits);
            case COLOR_RGB:
                return ColorRgb.fromPacked(colorBits);
        }

        return null;
    }

    public static Color fgColor(long attrs) {
        if (attrs == Attrs.NULL) return null;
        ColorType colorType = fgColorType(attrs);
        return color(attrs, colorType, FG_COLOR_SHIFT);
    }

    public static Color bgColor(long attrs) {
        if (attrs == Attrs.NULL) return null;
        ColorType colorType = bgColorType(attrs);
        return color(attrs, colorType, BG_COLOR_SHIFT);
    }

    public static boolean isBold(long attrs) {
        return (attrs & 1) > 0;
    }

    public static boolean isFaint(long attrs) {
        return (attrs & (1L << 1)) > 0;
    }

    public static boolean isItalic(long attrs) {
        return (attrs & (1L << 2)) > 0;
    }

    public static boolean isUnderline(long attrs) {
        return (attrs & (1L << 3)) > 0;
    }

    public static boolean isBlink(long attrs) {
        return (attrs & (1L << 4)) > 0;
    }

    public static boolean isInverse(long attrs) {
        return (attrs & (1L << 5)) > 0;
    }

    public static boolean isStrikethrough(long attrs) {
        return (attrs & (1L << 6)) > 0;
    }

    public static boolean isEmpty(long attrs) {
        return attrs == EMPTY;
    }
}
