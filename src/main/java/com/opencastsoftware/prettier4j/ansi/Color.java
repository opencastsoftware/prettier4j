/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

public interface Color {
    /**
     * Produces the SGR (Select Graphic Rendition) parameters required to apply this {@link Color} as the foreground color.
     *
     * @return the SGR (Select Graphic Rendition) parameters required to apply this {@link Color} as the foreground color.
     */
    int[] fgParams();

    /**
     * Produces the SGR (Select Graphic Rendition) parameters required to apply this {@link Color} as the background color.
     *
     * @return the SGR (Select Graphic Rendition) parameters required to apply this {@link Color} as the background color.
     */
    int[] bgParams();

    /**
     * The {@link ColorType} of this {@link Color}.
     *
     * @return the {@link ColorType ColorType} of this {@link Color}.
     */
    ColorType colorType();

    /**
     * Restores the default color for a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a {@link Color} representing the default color.
     */
    static Color none() {
        return Color16.DEFAULT;
    }

    /**
     * Applies the color black to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} black.
     */
    static Color black() {
        return Color16.BLACK;
    }

    /**
     * Applies the color red to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} red.
     */
    static Color red() {
        return Color16.RED;
    }

    /**
     * Applies the color green to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} green.
     */
    static Color green() {
        return Color16.GREEN;
    }

    /**
     * Applies the color yellow to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} yellow.
     */
    static Color yellow() {
        return Color16.YELLOW;
    }

    /**
     * Applies the color blue to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} blue.
     */
    static Color blue() {
        return Color16.BLUE;
    }

    /**
     * Applies the color magenta to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} magenta.
     */
    static Color magenta() {
        return Color16.MAGENTA;
    }

    /**
     * Applies the color cyan to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} cyan.
     */
    static Color cyan() {
        return Color16.CYAN;
    }

    /**
     * Applies the color white to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return the {@link Color} white.
     */
    static Color white() {
        return Color16.WHITE;
    }

    /**
     * Applies a brighter shade of the color black (dark grey) to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter black (dark grey) {@link Color}.
     */
    static Color brightBlack() {
        return Color16.BRIGHT_BLACK;
    }

    /**
     * Applies a brighter shade of the color red to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} red.
     */
    static Color brightRed() {
        return Color16.BRIGHT_RED;
    }

    /**
     * Applies a brighter shade of the color green to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} green.
     */
    static Color brightGreen() {
        return Color16.BRIGHT_GREEN;
    }

    /**
     * Applies a brighter shade of the color yellow to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} yellow.
     */
    static Color brightYellow() {
        return Color16.BRIGHT_YELLOW;
    }

    /**
     * Applies a brighter shade of the color blue to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} blue.
     */
    static Color brightBlue() {
        return Color16.BRIGHT_BLUE;
    }

    /**
     * Applies a brighter shade of the color magenta to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} magenta.
     */
    static Color brightMagenta() {
        return Color16.BRIGHT_MAGENTA;
    }

    /**
     * Applies a brighter shade of the color cyan to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} cyan.
     */
    static Color brightCyan() {
        return Color16.BRIGHT_CYAN;
    }

    /**
     * Applies a brighter shade of white to a {@link com.opencastsoftware.prettier4j.Doc Doc} when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} white.
     */
    static Color brightWhite() {
        return Color16.BRIGHT_WHITE;
    }

    /**
     * Applies the given color code from the xterm 256-color palette to a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} white.
     */
    static Color xterm(int color) {
        return new ColorXterm(color);
    }

    /**
     * Applies the given 24-bit RGB color to a {@link com.opencastsoftware.prettier4j.Doc Doc}
     * when {@link com.opencastsoftware.prettier4j.Doc#styled styled}
     * via {@link Styles#fg(Color) fg} or {@link Styles#bg(Color) bg}.
     *
     * @return a brighter shade of the {@link Color} white.
     */
    static Color rgb(int r, int g, int b) {
        return new ColorRgb(r, g, b);
    }
}
