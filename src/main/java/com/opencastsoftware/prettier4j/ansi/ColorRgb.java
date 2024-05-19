/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

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

    @Override
    public int[] fgParams() {
        return new int[] { 38, 2, red(), green(), blue() };
    }

    @Override
    public int[] bgParams() {
        return new int[] { 48, 2, red(), green(), blue() };
    }
}
