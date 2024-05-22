/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import java.util.Objects;

public class ColorRgb implements Color {
    private final int red;
    private final int green;
    private final int blue;

    ColorRgb(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int red() {
        return this.red;
    }

    public int green() {
        return this.green;
    }

    public int blue() {
        return this.blue;
    }

    static ColorRgb fromPacked(int packed) {
        int r = packed & 0xFF;
        int g = (packed >>> Byte.SIZE) & 0xFF;
        int b = (packed >>> (Byte.SIZE * 2)) & 0xFF;
        return new ColorRgb(r, g, b);
    }

    int packed() {
        int r = red & 0xFF;
        int g = (green & 0xFF) << Byte.SIZE;
        int b = (blue & 0xFF) << (Byte.SIZE * 2);
        return r | g | b;
    }

    @Override
    public int[] fgParams() {
        return new int[] { 38, 2, red(), green(), blue() };
    }

    @Override
    public int[] bgParams() {
        return new int[] { 48, 2, red(), green(), blue() };
    }

    @Override
    public ColorType colorType() {
        return ColorType.COLOR_RGB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorRgb colorRgb = (ColorRgb) o;
        return red == colorRgb.red && green == colorRgb.green && blue == colorRgb.blue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }

    @Override
    public String toString() {
        return "ColorRgb [" +
                "red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ']';
    }
}
