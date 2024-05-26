/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;

import static com.opencastsoftware.prettier4j.ansi.Attrs.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttrsTest {
    @Test
    void nullAttrsHasNoColorType() {
        assertThat(fgColorType(NULL), is(nullValue()));
        assertThat(bgColorType(NULL), is(nullValue()));
    }

    @Test
    void emptyAttrsHasNoColorType() {
        assertThat(fgColorType(EMPTY), is(nullValue()));
        assertThat(bgColorType(EMPTY), is(nullValue()));
    }

    @Test
    void nullAttrsHasNoColor() {
        assertThat(fgColor(NULL), is(nullValue()));
        assertThat(bgColor(NULL), is(nullValue()));
    }


    @Test
    void emptyAttrsHasNoColor() {
        assertThat(fgColor(EMPTY), is(nullValue()));
        assertThat(bgColor(EMPTY), is(nullValue()));
    }

    @Test
    void emptyAttrsIsNotBold() {
        assertFalse(isBold(EMPTY), "Empty Attrs should not be isBold");
    }

    @Test
    void emptyAttrsIsNotFaint() {
        assertFalse(isFaint(EMPTY), "Empty Attrs should not be isFaint");
    }

    @Test
    void emptyAttrsIsNotItalic() {
        assertFalse(isItalic(EMPTY), "Empty Attrs should not be isItalic");
    }

    @Test
    void emptyAttrsIsNotUnderline() {
        assertFalse(isUnderline(EMPTY), "Empty Attrs should not be isUnderline");
    }

    @Test
    void emptyAttrsIsNotBlink() {
        assertFalse(isBlink(EMPTY), "Empty Attrs should not be isBlink");
    }

    @Test
    void emptyAttrsIsNotInverse() {
        assertFalse(isInverse(EMPTY), "Empty Attrs should not be isInverse");
    }

    @Test
    void emptyAttrsIsNotStrikethrough() {
        assertFalse(isStrikethrough(EMPTY), "Empty Attrs should not be isStrikethrough");
    }

    @Test
    void boldAttrsIsBold() {
        assertTrue(isBold(withStyles(EMPTY, Styles.bold())), "Bold Attrs should be isBold");
    }

    @Test
    void faintAttrsIsFaint() {
        assertTrue(isFaint(withStyles(EMPTY, Styles.faint())), "Faint Attrs should be isFaint");
    }

    @Test
    void italicAttrsIsItalic() {
        assertTrue(isItalic(withStyles(EMPTY, Styles.italic())), "Italic Attrs should be isItalic");
    }

    @Test
    void underlineAttrsIsUnderline() {
        assertTrue(isUnderline(withStyles(EMPTY, Styles.underline())), "Underline Attrs should be isUnderline");
    }

    @Test
    void blinkAttrsIsBlink() {
        assertTrue(isBlink(withStyles(EMPTY, Styles.blink())), "Blink Attrs should be isBlink");
    }

    @Test
    void inverseAttrsIsInverse() {
        assertTrue(isInverse(withStyles(EMPTY, Styles.inverse())), "Inverse Attrs should be isInverse");
    }

    @Test
    void strikethroughAttrsIsStrikethrough() {
        assertTrue(isStrikethrough(withStyles(EMPTY, Styles.strikethrough())), "Strikethrough Attrs should be isStrikethrough");
    }

    @Property
    void attrsWithFgColorReturnsFgColor(@ForAll("colors") Color color) {
        long withColor = withStyles(EMPTY, Styles.fg(color));
        assertThat(fgColorType(withColor), is(equalTo(color.colorType())));
        assertThat(fgColor(withColor), is(equalTo(color)));
    }

    @Property
    void attrsWithBgColorReturnsBgColor(@ForAll("colors") Color color) {
        long withColor = withStyles(EMPTY, Styles.bg(color));
        assertThat(bgColorType(withColor), is(equalTo(color.colorType())));
        assertThat(bgColor(withColor), is(equalTo(color)));
    }

    @Property
    void attrsCanSetFgBgColorIndependently(@ForAll("colors") Color fgColor, @ForAll("colors") Color bgColor) {
        long withColor = withStyles(EMPTY, Styles.fg(fgColor), Styles.bg(bgColor));

        assertThat(fgColorType(withColor), is(equalTo(fgColor.colorType())));
        assertThat(fgColor(withColor), is(equalTo(fgColor)));

        assertThat(bgColorType(withColor), is(equalTo(bgColor.colorType())));
        assertThat(bgColor(withColor), is(equalTo(bgColor)));
    }

    @Provide
    Arbitrary<Color> colors() {
        return Arbitraries.oneOf(
                Arbitraries.create(Color::none),
                Arbitraries.create(Color::black),
                Arbitraries.create(Color::red),
                Arbitraries.create(Color::green),
                Arbitraries.create(Color::yellow),
                Arbitraries.create(Color::blue),
                Arbitraries.create(Color::magenta),
                Arbitraries.create(Color::cyan),
                Arbitraries.create(Color::white),

                Arbitraries.create(Color::brightBlack),
                Arbitraries.create(Color::brightRed),
                Arbitraries.create(Color::brightGreen),
                Arbitraries.create(Color::brightYellow),
                Arbitraries.create(Color::brightBlue),
                Arbitraries.create(Color::brightMagenta),
                Arbitraries.create(Color::brightCyan),
                Arbitraries.create(Color::brightWhite),

                Arbitraries.integers().between(0, 255)
                        .map(Color::xterm),

                Arbitraries.integers().between(0, 255)
                        .tuple3().map(t -> Color.rgb(t.get1(), t.get2(), t.get3()))
        );
    }
}
