/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Attrs {
    private final Color fgColor;
    private final Color bgColor;
    private final Boolean bold;
    private final Boolean faint;
    private final Boolean italic;
    private final Boolean underline;
    private final Boolean blink;
    private final Boolean inverse;
    private final Boolean strikethrough;

    Attrs(
      Color fgColor,
      Color bgColor,
      Boolean bold,
      Boolean faint,
      Boolean italic,
      Boolean underline,
      Boolean blink,
      Boolean inverse,
      Boolean strikethrough) {
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.bold = bold;
        this.faint = faint;
        this.italic = italic;
        this.underline = underline;
        this.blink = blink;
        this.inverse = inverse;
        this.strikethrough = strikethrough;
    }

    public String apply() {
        IntStream.Builder builder = IntStream.builder();

        if (isBold() != null && isBold()) {
            builder.add(DisplayStyle.BOLD.code());
        }
        if (isFaint() != null && isFaint()) {
            builder.add(DisplayStyle.FAINT.code());
        }
        if (isItalic() != null && isItalic()) {
            builder.add(DisplayStyle.ITALIC.code());
        }
        if (isUnderline() != null && isUnderline()) {
            builder.add(DisplayStyle.UNDERLINE.code());
        }
        if (isBlink() != null && isBlink()) {
            builder.add(DisplayStyle.BLINK.code());
        }
        if (isInverse() != null && isInverse()) {
            builder.add(DisplayStyle.INVERSE.code());
        }
        if (isStrikethrough() != null && isStrikethrough()) {
            builder.add(DisplayStyle.STRIKETHROUGH.code());
        }
        if (fgColor() != null) {
            for (int param : fgColor().fgParams()) {
                builder.add(param);
            }
        }
        if (bgColor() != null) {
            for (int param : bgColor().bgParams()) {
                builder.add(param);
            }
        }

        String sgrParams = builder.build()
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(";"));

        return Ansi.CSI + sgrParams + 'm';
    }

    public Attrs merge(Attrs other) {
        Builder builder = new Builder();
        builder.buildFrom(this);
        builder.buildFrom(other);
        return builder.build();
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

    public Color fgColor() {
        return fgColor;
    }

    public Color bgColor() {
        return bgColor;
    }

    public Boolean isBold() {
        return bold;
    }

    public Boolean isFaint() {
        return faint;
    }

    public Boolean isItalic() {
        return italic;
    }

    public Boolean isUnderline() {
        return underline;
    }

    public Boolean isBlink() {
        return blink;
    }

    public Boolean isInverse() {
        return inverse;
    }

    public Boolean isStrikethrough() {
        return strikethrough != null && strikethrough;
    }

    public static Attrs EMPTY = new Attrs(null, null, false, false, false, false, false, false, false);

    public static UnaryOperator<Builder> fg(Color color) {
        return builder -> builder.withFg(color);
    }

    public static UnaryOperator<Builder> bg(Color color) {
        return builder -> builder.withBg(color);
    }

    public static UnaryOperator<Builder> bold() {
        return Builder::withBold;
    }

    public static UnaryOperator<Builder> faint() {
        return Builder::withFaint;
    }

    public static UnaryOperator<Builder> italic() {
        return Builder::withItalic;
    }

    public static UnaryOperator<Builder> underline() {
        return Builder::withUnderline;
    }

    public static UnaryOperator<Builder> blink() {
        return Builder::withBlink;
    }

    public static UnaryOperator<Builder> inverse() {
        return Builder::withInverse;
    }

    public static UnaryOperator<Builder> strikethrough() {
        return Builder::withStrikethrough;
    }

    public static class Builder {
        private Color fgColor;
        private Color bgColor;
        private Boolean bold;
        private Boolean faint;
        private Boolean italic;
        private Boolean underline;
        private Boolean blink;
        private Boolean inverse;
        private Boolean strikethrough;

        public Builder buildFrom(Attrs existing) {
            if (existing.fgColor() != null) {
                withFg(existing.fgColor());
            }
            if (existing.bgColor() != null) {
                withBg(existing.bgColor());
            }
            if (existing.isBold() != null) {
                withBold(existing.isBold());
            }
            if (existing.isFaint() != null) {
                withFaint(existing.isFaint());
            }
            if (existing.isItalic() != null) {
                withItalic(existing.isItalic());
            }
            if (existing.isUnderline() != null) {
                withUnderline(existing.isUnderline());
            }
            if (existing.isBlink() != null) {
                withBlink(existing.isBlink());
            }
            if (existing.isInverse() != null) {
                withInverse(existing.isInverse());
            }
            if (existing.isStrikethrough() != null) {
                withStrikethrough(existing.isStrikethrough());
            }
            return this;
        }

        public Attrs build() {
            return new Attrs(fgColor, bgColor, bold, faint, italic, underline, blink, inverse, strikethrough);
        }

        public Builder withFg(Color color) {
            this.fgColor = color;
            return this;
        }

        public Builder withBg(Color color) {
            this.bgColor = color;
            return this;
        }

        public Builder withBold() {
            this.bold = true;
            return this;
        }
        public Builder withBold(boolean bold) {
            this.bold = bold;
            return this;
        }

        public Builder withFaint() {
            this.faint = true;
            return this;
        }
        public Builder withFaint(boolean faint) {
            this.faint = faint;
            return this;
        }

        public Builder withItalic() {
            this.italic = true;
            return this;
        }
        public Builder withItalic(boolean italic) {
            this.italic = italic;
            return this;
        }

        public Builder withUnderline() {
            this.underline = true;
            return this;
        }
        public Builder withUnderline(boolean underline) {
            this.underline = underline;
            return this;
        }

        public Builder withBlink() {
            this.blink = true;
            return this;
        }
        public Builder withBlink(boolean blink) {
            this.blink = blink;
            return this;
        }

        public Builder withInverse() {
            this.inverse = true;
            return this;
        }
        public Builder withInverse(boolean inverse) {
            this.inverse = inverse;
            return this;
        }

        public Builder withStrikethrough() {
            this.strikethrough = true;
            return this;
        }
        public Builder withStrikethrough(boolean strikethrough) {
            this.strikethrough = strikethrough;
            return this;
        }
    }
}
