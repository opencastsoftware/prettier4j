/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;


import java.util.Objects;
import java.util.function.LongUnaryOperator;

import static com.opencastsoftware.prettier4j.ansi.Attrs.*;

public class Styles {
    public static StylesOperator fg(Color color) {
        return new Fg(color);
    }

    public static StylesOperator bg(Color color) {
        return new Bg(color);
    }

    public static StylesOperator bold() {
        return Bold.getInstance();
    }

    public static StylesOperator faint() {
        return Faint.getInstance();
    }

    public static StylesOperator italic() {
        return Italic.getInstance();
    }

    public static StylesOperator underline() {
        return Underline.getInstance();
    }

    public static StylesOperator blink() {
        return Blink.getInstance();
    }

    public static StylesOperator inverse() {
        return Inverse.getInstance();
    }

    public static StylesOperator strikethrough() {
        return Strikethrough.getInstance();
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
        long noColorType = attrs & maskValue;
        if (color == null) return noColorType;
        int colorTypeCode = color.colorType().code();
        long newColorType = (long) colorTypeCode << shiftValue;
        return noColorType | newColorType;
    }

    private static long withColor(long attrs, Color color, long maskValue, long shiftValue) {
        long noColor = attrs & maskValue;

        if (color == null) return noColor;

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

    public interface StylesOperator extends LongUnaryOperator {}

    static class Fg implements StylesOperator {
        private final Color color;

        public Fg(Color color) {
            this.color = color;
        }

        @Override
        public long applyAsLong(long attrs) {
            return withFgColor(attrs, color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Fg fg = (Fg) o;
            return Objects.equals(color, fg.color);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(color);
        }

        @Override
        public String toString() {
            return "Fg [" +
                    "color=" + color +
                    ']';
        }
    }

    static class Bg implements StylesOperator {
        private final Color color;

        public Bg(Color color) {
            this.color = color;
        }

        @Override
        public long applyAsLong(long attrs) {
            return withBgColor(attrs, color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bg fg = (Bg) o;
            return Objects.equals(color, fg.color);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(color);
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
