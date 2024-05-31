/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j;

import java.util.Objects;

/**
 * Options which determine how {@link Doc}s are rendered.
 */
public class RenderOptions {
    private static final RenderOptions DEFAULT = new RenderOptions(80, true);

    private final int lineWidth;
    private final boolean emitAnsiEscapes;

    RenderOptions(int lineWidth, boolean emitAnsiEscapes) {
        this.lineWidth = lineWidth;
        this.emitAnsiEscapes = emitAnsiEscapes;
    }

    /**
     * Determines the preferred maximum line width.
     *
     * @return the preferred maximum line width.
     */
    public int lineWidth() {
        return this.lineWidth;
    }

    /**
     * Determines whether to emit ANSI escape code sequences.
     *
     * @return whether to emit ANSI escape code sequences.
     */
    public boolean emitAnsiEscapes() {
        return this.emitAnsiEscapes;
    }

    /**
     * Create a {@link RenderOptions} populated with default values.
     * <p>
     * This configures a preferred maximum line width of 80 characters,
     * and enables ANSI escape code sequences.
     *
     * @return the default rendering options.
     */
    public static RenderOptions defaults() {
        return DEFAULT;
    }

    /**
     * Creates a {@link Builder} for {@link RenderOptions}.
     * @return a {@link RenderOptions} builder.
     */
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

    /**
     * A builder for configuring {@link RenderOptions}.
     */
    public static class Builder {
        private int lineWidth;
        private boolean emitAnsiEscapes;
        private Builder() {}

        /**
         * Build the resulting {@link RenderOptions}.
         *
         * @return the {@link RenderOptions}.
         */
        public RenderOptions build() {
            return new RenderOptions(this.lineWidth, this.emitAnsiEscapes);
        }

        /**
         * Set the preferred maximum rendering width.
         *
         * @param width the preferred maximum rendering width.
         * @return this {@link Builder} for fluent usage.
         */
        public Builder lineWidth(int width) {
            this.lineWidth = width;
            return this;
        }

        /**
         * Set whether to emit ANSI escape code sequences.
         *
         * @param emitAnsi whether to emit ANSI escape code sequences.
         * @return this {@link Builder} for fluent usage.
         */
        public Builder emitAnsiEscapes(boolean emitAnsi) {
            this.emitAnsiEscapes = emitAnsi;
            return this;
        }
    }
}
