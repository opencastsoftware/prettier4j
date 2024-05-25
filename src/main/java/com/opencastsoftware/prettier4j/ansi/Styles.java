/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;


import java.util.Objects;
import java.util.function.LongUnaryOperator;

import static com.opencastsoftware.prettier4j.ansi.Attrs.*;

public class Styles {
    private Styles() {}

    /**
     * Sets the foreground color for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @param color the {@link Color} to apply as the foreground color.
     * @return a {@link StylesOperator} that applies the given foreground {@link Color}.
     */
    public static StylesOperator fg(Color color) {
        return new Fg(color);
    }

    /**
     * Sets the background color for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @param color the {@link Color} to apply as the background color.
     * @return a {@link StylesOperator} that applies the given background {@link Color}.
     */
    public static StylesOperator bg(Color color) {
        return new Bg(color);
    }

    /**
     * Sets the bold display style for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @return a {@link StylesOperator} that applies the bold display style.
     */
    public static StylesOperator bold() {
        return Bold.getInstance();
    }

    /**
     * Sets the faint display style for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @return a {@link StylesOperator} that applies the faint display style.
     */
    public static StylesOperator faint() {
        return Faint.getInstance();
    }

    /**
     * Sets the italic display style for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @return a {@link StylesOperator} that applies the italic display style.
     */
    public static StylesOperator italic() {
        return Italic.getInstance();
    }

    /**
     * Sets the underline display style for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @return a {@link StylesOperator} that applies the underline display style.
     */
    public static StylesOperator underline() {
        return Underline.getInstance();
    }

    /**
     * Sets the blink display style for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @return a {@link StylesOperator} that applies the blink display style.
     */
    public static StylesOperator blink() {
        return Blink.getInstance();
    }

    /**
     * Sets the inverse display style for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @return a {@link StylesOperator} that applies the inverse display style.
     */
    public static StylesOperator inverse() {
        return Inverse.getInstance();
    }

    /**
     * Sets the strikethrough display style for a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when used as an argument to {@link com.opencastsoftware.prettier4j.Doc#styled styled}.
     *
     * @return a {@link StylesOperator} that applies the strikethrough display style.
     */
    public static StylesOperator strikethrough() {
        return Strikethrough.getInstance();
    }

    /**
     * An operator that is used to apply display styles to a {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public interface StylesOperator extends LongUnaryOperator {}

    static abstract class ColorStylesOperator implements StylesOperator {
        protected final Color color;

        private final long colorTypeMask;
        private final long colorTypeShift;

        private final long colorMask;
        private final long colorShift;

        ColorStylesOperator(
                Color color,
                long colorTypeMask,
                long colorTypeShift,
                long colorMask,
                long colorShift) {
            this.color = color;
            this.colorTypeMask = colorTypeMask;
            this.colorTypeShift = colorTypeShift;
            this.colorMask = colorMask;
            this.colorShift = colorShift;
        }

        private long withColorType(long attrs) {
            long noColorType = attrs & colorTypeMask;
            if (color == null) return noColorType;
            int colorTypeCode = color.colorType().code();
            long newColorType = (long) colorTypeCode << colorTypeShift;
            return noColorType | newColorType;
        }

        private long withColor(long attrs) {
            long noColor = attrs & colorMask;

            if (color == null) return noColor;

            long newColor = 0L;

            switch (color.colorType()) {
                case COLOR_16:
                    Color16 color16 = (Color16) color;
                    newColor = color16.code();
                    break;
                case COLOR_XTERM:
                    ColorXterm colorXterm = (ColorXterm) color;
                    newColor = colorXterm.color();
                    break;
                case COLOR_RGB:
                    ColorRgb colorRgb = (ColorRgb) color;
                    newColor = colorRgb.packed();
                    break;
            }

            return noColor | (newColor << colorShift);
        }

        @Override
        public long applyAsLong(long attrs) {
            long withColorType = withColorType(attrs);
            return withColor(withColorType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ColorStylesOperator that = (ColorStylesOperator) o;
            return colorTypeMask == that.colorTypeMask &&
                    colorTypeShift == that.colorTypeShift &&
                    colorMask == that.colorMask &&
                    colorShift == that.colorShift &&
                    Objects.equals(color, that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, colorTypeMask, colorTypeShift, colorMask, colorShift);
        }
    }

    static class Fg extends ColorStylesOperator {
        Fg(Color color) {
            super(color,
                FG_COLOR_TYPE_MASK, FG_COLOR_TYPE_SHIFT,
                FG_COLOR_MASK, FG_COLOR_SHIFT);
        }

        @Override
        public String toString() {
            return "Fg [" +
                    "color=" + color +
                    ']';
        }
    }

    static class Bg extends ColorStylesOperator {
        Bg(Color color) {
            super(color,
                BG_COLOR_TYPE_MASK, BG_COLOR_TYPE_SHIFT,
                BG_COLOR_MASK, BG_COLOR_SHIFT);
        }

        @Override
        public String toString() {
            return "Bg [" +
                    "color=" + color +
                    ']';
        }
    }

    static abstract class DisplayStylesOperator implements StylesOperator {
        private final int shiftValue;

        DisplayStylesOperator(int shiftValue) {
            this.shiftValue = shiftValue;
        }

        @Override
        public long applyAsLong(long attrs) {
            return attrs | (1L << shiftValue);
        }
    }

    static class Bold extends DisplayStylesOperator {
        private static final Bold INSTANCE = new Bold();

        static Bold getInstance() {
            return INSTANCE;
        }

        Bold() {
            super(0);
        }

        @Override
        public String toString() {
            return "Bold []";
        }
    }

    static class Faint extends DisplayStylesOperator {
        private static final Faint INSTANCE = new Faint();

        static Faint getInstance() {
            return INSTANCE;
        }

        Faint() {
            super(1);
        }

        @Override
        public String toString() {
            return "Faint []";
        }
    }

    static class Italic extends DisplayStylesOperator {
        private static final Italic INSTANCE = new Italic();

        static Italic getInstance() {
            return INSTANCE;
        }

        Italic() {
            super(2);
        }

        @Override
        public String toString() {
            return "Italic []";
        }
    }

    static class Underline extends DisplayStylesOperator {
        private static final Underline INSTANCE = new Underline();

        static Underline getInstance() {
            return INSTANCE;
        }

        Underline() {
            super(3);
        }

        @Override
        public String toString() {
            return "Underline []";
        }
    }

    static class Blink extends DisplayStylesOperator {
        private static final Blink INSTANCE = new Blink();

        static Blink getInstance() {
            return INSTANCE;
        }

        Blink() {
            super(4);
        }

        @Override
        public String toString() {
            return "Blink []";
        }
    }

    static class Inverse extends DisplayStylesOperator {
        private static final Inverse INSTANCE = new Inverse();

        static Inverse getInstance() {
            return INSTANCE;
        }

        Inverse() {
            super(5);
        }

        @Override
        public String toString() {
            return "Inverse []";
        }
    }

    static class Strikethrough extends DisplayStylesOperator {
        private static final Strikethrough INSTANCE = new Strikethrough();

        static Strikethrough getInstance() {
            return INSTANCE;
        }

        Strikethrough() {
            super(6);
        }

        @Override
        public String toString() {
            return "Strikethrough []";
        }
    }
}
