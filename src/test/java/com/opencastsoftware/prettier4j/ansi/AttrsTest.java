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
        assertTrue(Attrs.empty().withStyles(Styles.underline()).isUnderline(), "Italic Attrs should be isItalic");
    }

    @Test
    void blinkAttrsIsBlink() {
        assertTrue(Attrs.empty().withStyles(Styles.blink()).isBlink(), "Italic Attrs should be isItalic");
    }

    @Test
    void inverseAttrsIsInverse() {
        assertTrue(Attrs.empty().withStyles(Styles.inverse()).isInverse(), "Italic Attrs should be isItalic");
    }

    @Test
    void strikethroughAttrsIsStrikethrough() {
        assertTrue(Attrs.empty().withStyles(Styles.strikethrough()).isStrikethrough(), "Italic Attrs should be isItalic");
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
                Arbitraries.of(Color16.class),
                Arbitraries.integers().between(0, 255)
                        .map(Color::xterm256),
                Arbitraries.integers().between(0, 255)
                        .tuple3().map(t -> Color.rgb(t.get1(), t.get2(), t.get3()))
        );
    }
}
