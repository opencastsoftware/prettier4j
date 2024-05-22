/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Attrs {
    static final long FG_COLOR_TYPE_SHIFT = Byte.SIZE * 7;
    static final long FG_COLOR_TYPE_MASK = 0xF0FF_FFFF_FFFF_FFFFL;

    static final long FG_COLOR_SHIFT = Byte.SIZE;
    static final long FG_COLOR_MASK = 0xFFFF_FFFF_0000_00FFL;

    static final long BG_COLOR_TYPE_SHIFT = Byte.SIZE * 7 + 4;
    static final long BG_COLOR_TYPE_MASK = 0xFFF_FFFF_FFFF_FFFFL;

    static final long BG_COLOR_SHIFT = Byte.SIZE * 4;
    static final long BG_COLOR_MASK = 0xFF00_0000_FFFF_FFFFL;

    private final long attrs;

    private static final Attrs EMPTY = new Attrs(0L);

    public static Attrs empty() {
        return Attrs.EMPTY;
    }

    Attrs(long attrs) {
        this.attrs = attrs;
    }

    public Attrs withStyles(Styles.StylesOperator ...styles) {
        long attrs = this.attrs;
        for (Styles.StylesOperator style : styles) {
            attrs = style.applyAsLong(attrs);
        }
        return new Attrs(attrs);
    }

    public String transitionTo(Attrs next) {
        if (next == null) {
            return Ansi.RESET;
        }

        IntStream.Builder builder = IntStream.builder();

        // Bold & faint are both reset by the same SGR code
        if (!next.isBold() && !next.isFaint() && (this.isBold() || this.isFaint())) {
            builder.add(DisplayStyle.NORMAL_INTENSITY.code());
        }
        if (next.isBold() && !this.isBold()) {
            builder.add(DisplayStyle.BOLD.code());
        }
        if (next.isFaint() && !this.isFaint()) {
            builder.add(DisplayStyle.FAINT.code());
        }
        // Italic
        if (next.isItalic() && !this.isItalic()) {
            builder.add(DisplayStyle.ITALIC.code());
        } else if (!next.isItalic() && this.isItalic()) {
            builder.add(DisplayStyle.ITALIC_OFF.code());
        }
        // Underline
        if (next.isUnderline() && !this.isUnderline()) {
            builder.add(DisplayStyle.UNDERLINE.code());
        } else if (!next.isUnderline() && this.isUnderline()) {
            builder.add(DisplayStyle.UNDERLINE_OFF.code());
        }
        // Blink
        if (next.isBlink() && !this.isBlink()) {
            builder.add(DisplayStyle.BLINK.code());
        } else if (!next.isBlink() && this.isBlink()) {
            builder.add(DisplayStyle.BLINK_OFF.code());
        }
        // Inverse
        if (next.isInverse() && !this.isInverse()) {
            builder.add(DisplayStyle.INVERSE.code());
        } else if (!next.isInverse() && this.isInverse()) {
            builder.add(DisplayStyle.INVERSE_OFF.code());
        }
        // Strikethrough
        if (next.isStrikethrough() && !this.isStrikethrough()) {
            builder.add(DisplayStyle.STRIKETHROUGH.code());
        } else if (!next.isStrikethrough() && this.isStrikethrough()) {
            builder.add(DisplayStyle.STRIKETHROUGH_OFF.code());
        }
        // Foreground color
        if (next.fgColor() != null && !next.fgColor().equals(this.fgColor())) {
            for (int param : next.fgColor().fgParams()) {
                builder.add(param);
            }
        }
        // Background color
        if (next.bgColor() != null && !next.bgColor().equals(this.bgColor())) {
            for (int param : next.bgColor().bgParams()) {
                builder.add(param);
            }
        }

        String sgrParams = builder.build()
            .mapToObj(Integer::toString)
            .collect(Collectors.joining(";"));

        return Ansi.CSI + sgrParams + 'm';
    }

    private ColorType colorType(long shiftValue) {
        int colorTypeBits = (int) (attrs >>> shiftValue) & 0xF;
        return ColorType.withCode(colorTypeBits);
    }

    public ColorType fgColorType() {
        return colorType(FG_COLOR_TYPE_SHIFT);
    }

    public ColorType bgColorType() {
        return colorType(BG_COLOR_TYPE_SHIFT);
    }

    private Color color(ColorType colorType, long shiftValue) {
        if (colorType == null) {
            return null;
        }

        int colorBits = (int) (attrs >>> shiftValue) & 0xFFFFFF;

        switch (colorType) {
            case COLOR_16:
                return Color16.withCode(colorBits);
            case COLOR_256:
                return new Color256(colorBits);
            case COLOR_RGB:
                return ColorRgb.fromPacked(colorBits);
        }

        return null;
    }

    public Color fgColor() {
        ColorType colorType = fgColorType();
        return color(colorType, FG_COLOR_SHIFT);
    }

    public Color bgColor() {
        ColorType colorType = bgColorType();
        return color(colorType, BG_COLOR_SHIFT);
    }

    public boolean isBold() {
        return (attrs & 1) > 0;
    }

    public boolean isFaint() {
        return (attrs & (1L << 1)) > 0;
    }

    public boolean isItalic() {
        return (attrs & (1L << 2)) > 0;
    }

    public boolean isUnderline() {
        return (attrs & (1L << 3)) > 0;
    }

    public boolean isBlink() {
        return (attrs & (1L << 4)) > 0;
    }

    public boolean isInverse() {
        return (attrs & (1L << 5)) > 0;
    }

    public boolean isStrikethrough() {
        return (attrs & (1L << 6)) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attrs attrs1 = (Attrs) o;
        return attrs == attrs1.attrs;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(attrs);
    }

    @Override
    public String toString() {
        return "Attrs [" +
                "attrs=" + attrs +
                ']';
    }
}
