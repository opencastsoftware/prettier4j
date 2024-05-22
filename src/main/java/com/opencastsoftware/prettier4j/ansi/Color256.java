/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import java.util.Objects;

class Color256 implements Color {
    public final int color;

    Color256(int color) {
        this.color = color & 0xFF;
    }

    public int color() {
        return color;
    }

    @Override
    public int[] fgParams() {
        return new int[] { 38, 5, color() };
    }

    @Override
    public int[] bgParams() {
        return new int[] { 48, 5, color() };
    }

    @Override
    public ColorType colorType() {
        return ColorType.COLOR_256;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color256 color256 = (Color256) o;
        return color == color256.color;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(color);
    }

    @Override
    public String toString() {
        return "Color256 [" +
                "color=" + color +
                ']';
    }
}
