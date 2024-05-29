/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j;

import java.util.Objects;

public class RenderOptions {
    private final int lineWidth;
    private final boolean emitAnsiEscapes;

    RenderOptions(int lineWidth, boolean emitAnsiEscapes) {
        this.lineWidth = lineWidth;
        this.emitAnsiEscapes = emitAnsiEscapes;
    }

    public int lineWidth() {
        return this.lineWidth;
    }

    public boolean emitAnsiEscapes() {
        return this.emitAnsiEscapes;
    }

    public static RenderOptions defaults() {
        return new RenderOptions(80, true);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RenderOptions that = (RenderOptions) o;
        return lineWidth == that.lineWidth && emitAnsiEscapes == that.emitAnsiEscapes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineWidth, emitAnsiEscapes);
    }

    @Override
    public String toString() {
        return "RenderOptions[" +
                "lineWidth=" + lineWidth +
                ", emitAnsiEscapes=" + emitAnsiEscapes +
                ']';
    }

    public static class Builder {
        private int lineWidth;
        private boolean emitAnsiEscapes;
        private Builder() {}

        public RenderOptions build() {
            return new RenderOptions(this.lineWidth, this.emitAnsiEscapes);
        }

        public Builder lineWidth(int width) {
            this.lineWidth = width;
            return this;
        }

        public Builder emitAnsiEscapes(boolean emitAnsi) {
            this.emitAnsiEscapes = emitAnsi;
            return this;
        }
    }
}
