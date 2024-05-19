/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

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
}
