/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import com.jparams.verifier.tostring.ToStringVerifier;
import net.jqwik.api.*;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttrsTest {
    @Test
    void emptyAttrsHasNoColorType() {
        assertThat(Attrs.empty().fgColorType(), is(nullValue()));
        assertThat(Attrs.empty().bgColorType(), is(nullValue()));
    }

    @Test
    void emptyAttrsHasNoColor() {
        assertThat(Attrs.empty().fgColor(), is(nullValue()));
        assertThat(Attrs.empty().bgColor(), is(nullValue()));
    }

    @Test
    void emptyAttrsIsNotBold() {
        assertFalse(Attrs.empty().isBold(), "Empty Attrs should not be isBold");
    }

    @Test
    void emptyAttrsIsNotFaint() {
        assertFalse(Attrs.empty().isFaint(), "Empty Attrs should not be isFaint");
    }

    @Test
    void emptyAttrsIsNotItalic() {
        assertFalse(Attrs.empty().isItalic(), "Empty Attrs should not be isItalic");
    }

    @Test
    void emptyAttrsIsNotUnderline() {
        assertFalse(Attrs.empty().isUnderline(), "Empty Attrs should not be isUnderline");
    }

    @Test
    void emptyAttrsIsNotBlink() {
        assertFalse(Attrs.empty().isBlink(), "Empty Attrs should not be isBlink");
    }

    @Test
    void emptyAttrsIsNotInverse() {
        assertFalse(Attrs.empty().isInverse(), "Empty Attrs should not be isInverse");
    }

    @Test
    void emptyAttrsIsNotStrikethrough() {
        assertFalse(Attrs.empty().isStrikethrough(), "Empty Attrs should not be isStrikethrough");
    }

    @Test
    void boldAttrsIsBold() {
        assertTrue(Attrs.empty().withStyles(Styles.bold()).isBold(), "Bold Attrs should be isBold");
    }

    @Test
    void faintAttrsIsFaint() {
        assertTrue(Attrs.empty().withStyles(Styles.faint()).isFaint(), "Faint Attrs should be isFaint");
    }

    @Test
    void italicAttrsIsItalic() {
        assertTrue(Attrs.empty().withStyles(Styles.italic()).isItalic(), "Italic Attrs should be isItalic");
    }

    @Test
    void underlineAttrsIsUnderline() {
        assertTrue(Attrs.empty().withStyles(Styles.underline()).isUnderline(), "Underline Attrs should be isUnderline");
    }

    @Test
    void blinkAttrsIsBlink() {
        assertTrue(Attrs.empty().withStyles(Styles.blink()).isBlink(), "Blink Attrs should be isBlink");
    }

    @Test
    void inverseAttrsIsInverse() {
        assertTrue(Attrs.empty().withStyles(Styles.inverse()).isInverse(), "Inverse Attrs should be isInverse");
    }

    @Test
    void strikethroughAttrsIsStrikethrough() {
        assertTrue(Attrs.empty().withStyles(Styles.strikethrough()).isStrikethrough(), "Strikethrough Attrs should be isStrikethrough");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Attrs.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        ToStringVerifier.forClass(Attrs.class).verify();
    }

    @Property
    void attrsWithFgColorReturnsFgColor(@ForAll("colors") Color color) {
        Attrs withColor = Attrs.empty().withStyles(Styles.fg(color));
        assertThat(withColor.fgColorType(), is(equalTo(color.colorType())));
        assertThat(withColor.fgColor(), is(equalTo(color)));
    }

    @Property
    void attrsWithBgColorReturnsBgColor(@ForAll("colors") Color color) {
        Attrs withColor = Attrs.empty().withStyles(Styles.bg(color));
        assertThat(withColor.bgColorType(), is(equalTo(color.colorType())));
        assertThat(withColor.bgColor(), is(equalTo(color)));
    }

    @Property
    void attrsCanSetFgBgColorIndependently(@ForAll("colors") Color fgColor, @ForAll("colors") Color bgColor) {
        Attrs withColor = Attrs.empty().withStyles(Styles.fg(fgColor), Styles.bg(bgColor));

        assertThat(withColor.fgColorType(), is(equalTo(fgColor.colorType())));
        assertThat(withColor.fgColor(), is(equalTo(fgColor)));

        assertThat(withColor.bgColorType(), is(equalTo(bgColor.colorType())));
        assertThat(withColor.bgColor(), is(equalTo(bgColor)));
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
                        .map(Color::xterm256),

                Arbitraries.integers().between(0, 255)
                        .tuple3().map(t -> Color.rgb(t.get1(), t.get2(), t.get3()))
        );
    }
}
