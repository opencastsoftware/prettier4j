/*
 * SPDX-FileCopyrightText:  © 2022-2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j;

import com.jparams.verifier.tostring.ToStringVerifier;
import com.opencastsoftware.prettier4j.ansi.AnsiConstants;
import com.opencastsoftware.prettier4j.ansi.Color;
import com.opencastsoftware.prettier4j.ansi.Styles;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.UnaryOperator;

import static com.opencastsoftware.prettier4j.Doc.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DocTest {
    /**
     * The example tree data structure from the original paper
     */
    private Node exampleTree = new Node("aaa", Arrays.asList(
            new Node("bbbbb", Arrays.asList(
                    new Node("ccc", Collections.emptyList()),
                    new Node("dd", Collections.emptyList()))),
            new Node("eee", Collections.emptyList()),
            new Node("ffff", Arrays.asList(
                    new Node("gg", Collections.emptyList()),
                    new Node("hhh", Collections.emptyList()),
                    new Node("ii", Collections.emptyList())))));

    /**
     * The example tree from the original paper, rendered with the showTree function
     * at width 30
     */
    @Test
    void showsExampleTree() {
        String expected = "aaa[bbbbb[ccc, dd],\n" +
                "    eee,\n" +
                "    ffff[gg, hhh, ii]]";

        assertThat(exampleTree.show().render(30), is(equalTo(expected)));
    }

    /**
     * The example tree from the original paper, rendered with the showTree'
     * function at width 30
     */
    @Test
    void showsPrimeExampleTree() {
        String expected = "aaa[\n" +
                "  bbbbb[ ccc, dd ],\n" +
                "  eee,\n" +
                "  ffff[ gg, hhh, ii ]\n" +
                "]";

        assertThat(exampleTree.showPrime().render(30), is(equalTo(expected)));
    }

    @Test
    void testAppendSpace() {
        String expected = "one two three";
        String actual = group(text("one")
                .appendSpace(text("two"))
                .appendSpace(text("three")))
                .render(30);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendSpaceFlattening() {
        String expected = "one two three";
        String actual = group(text("one")
                .appendSpace(text("two"))
                .appendSpace(text("three")))
                .render(5);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLine() {
        String expected = "one\ntwo\nthree";
        String actual = group(text("one")
                .appendLine(text("two"))
                .appendLine(text("three")))
                .render(30);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLineFlattening() {
        String expected = "one\ntwo\nthree";
        String actual = group(text("one")
                .appendLine(text("two"))
                .appendLine(text("three")))
                .render(5);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLineOrSpace() {
        String expected = "one two three";
        String actual = group(text("one")
                .appendLineOrSpace(text("two"))
                .appendLineOrSpace(text("three")))
                .render(30);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLineOrSpaceFlattening() {
        String expected = "one\ntwo\nthree";
        String actual = group(text("one")
                .appendLineOrSpace(text("two"))
                .appendLineOrSpace(text("three")))
                .render(5);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLineOrEmpty() {
        String expected = "onetwothree";
        String actual = group(text("one")
                .appendLineOrEmpty(text("two"))
                .appendLineOrEmpty(text("three")))
                .render(30);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLineOrEmptyFlattening() {
        String expected = "one\ntwo\nthree";
        String actual = group(text("one")
                .appendLineOrEmpty(text("two"))
                .appendLineOrEmpty(text("three")))
                .render(5);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLineOr() {
        String expected = "one; two; three";
        String altText = "; ";
        String actual = group(text("one")
                .appendLineOr(altText, text("two"))
                .appendLineOr(altText, text("three")))
                .render(30);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testAppendLineOrFlattening() {
        String expected = "one\ntwo\nthree";
        String altText = "; ";
        String actual = group(text("one")
                .appendLineOr(altText, text("two"))
                .appendLineOr(altText, text("three")))
                .render(5);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testBracket() {
        String expected = "functionCall(a, b, c)";
        String actual = text("functionCall")
                .append(
                        Doc.intersperse(
                                        Doc.text(",").append(Doc.lineOrSpace()),
                                        Arrays.asList("a", "b", "c").stream().map(Doc::text))
                                .bracket(2, Doc.lineOrEmpty(), Doc.text("("), Doc.text(")")))
                .render(80);

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testBracketStringOverload() {
        String expected = "functionCall(a, b, c)";
        String actual = text("functionCall")
                .append(
                        Doc.intersperse(
                                        Doc.text(",").append(Doc.lineOrSpace()),
                                        Arrays.asList("a", "b", "c").stream().map(Doc::text))
                                .bracket(2, Doc.lineOrEmpty(), "(", ")"))
                .render(80);

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testBracketFlattening() {
        String expected = "functionCall(\n  a,\n  b,\n  c\n)";
        String actual = text("functionCall")
                .append(
                        Doc.intersperse(
                                        Doc.text(",").append(Doc.lineOrSpace()),
                                        Arrays.asList("a", "b", "c").stream().map(Doc::text))
                                .bracket(2, Doc.lineOrEmpty(), Doc.text("("), Doc.text(")")))
                .render(10);

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testIntersperse() {
        String expected = "a, b, c";
        String actual = Doc.intersperse(
                Doc.text(", "),
                Arrays.asList(Doc.text("a"), Doc.text("b"), Doc.text("c"))).render(80);
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testBoldDisplayStyle() {
        String expected = sgrCode(1) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.bold()).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedBoldStyle() {
        String expected =
                sgrCode(7) + '(' +
                        sgrCode(1) + 'a' +
                        sgrCode(22) + ", " +
                        sgrCode(1) + 'b' +
                        sgrCode(22) + ')' + AnsiConstants.RESET;

        // (a, b) should fit into 6 chars ignoring ANSI escapes
        String actual = text("a").styled(Styles.bold())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.bold()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.inverse())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testFaintDisplayStyle() {
        String expected = sgrCode(2) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.faint()).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedFaintStyle() {
        String expected =
                sgrCode(9) + '(' +
                        sgrCode(2) + 'a' +
                        sgrCode(22) + ", " +
                        sgrCode(2) + 'b' +
                        sgrCode(22) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.faint())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.faint()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.strikethrough())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testItalicDisplayStyle() {
        String expected = sgrCode(3) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.italic()).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedItalicStyle() {
        String expected =
                sgrCode(1) + '(' +
                        sgrCode(3) + 'a' +
                        sgrCode(23) + ", " +
                        sgrCode(3) + 'b' +
                        sgrCode(23) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.italic())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.italic()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.bold())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testUnderlineDisplayStyle() {
        String expected = sgrCode(4) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.underline()).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedUnderlineStyle() {
        String expected =
                sgrCode(5) + '(' +
                        sgrCode(4) + 'a' +
                        sgrCode(24) + ", " +
                        sgrCode(4) + 'b' +
                        sgrCode(24) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.underline())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.underline()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.blink())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testBlinkDisplayStyle() {
        String expected = sgrCode(5) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.blink()).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedBlinkStyle() {
        String expected =
                sgrCode(2) + '(' +
                        sgrCode(5) + 'a' +
                        sgrCode(25) + ", " +
                        sgrCode(5) + 'b' +
                        sgrCode(25) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.blink())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.blink()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.faint())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testInverseDisplayStyle() {
        String expected = sgrCode(7) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.inverse()).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedInverseStyle() {
        String expected =
                sgrCode(3) + '(' +
                        sgrCode(7) + 'a' +
                        sgrCode(27) + ", " +
                        sgrCode(7) + 'b' +
                        sgrCode(27) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.inverse())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.inverse()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.italic())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testStrikethroughDisplayStyle() {
        String expected = sgrCode(9) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.strikethrough()).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedStrikethroughStyle() {
        String expected =
                sgrCode(1) + '(' +
                        sgrCode(9) + 'a' +
                        sgrCode(29) + ", " +
                        sgrCode(9) + 'b' +
                        sgrCode(29) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.strikethrough())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.strikethrough()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.bold())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNullFgStyle() {
        String expected = "a";
        String actual = text("a").styled(Styles.fg(null)).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedNullFgStyle() {
        String expected =
                sgrCode(37) + '(' +
                        sgrCode(39) + 'a' +
                        sgrCode(37) + ", " +
                        sgrCode(39) + 'b' +
                        sgrCode(37) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.fg(null))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.fg(null)))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.white()))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void test16ColorFgStyle() {
        String expected = sgrCode(32) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.fg(Color.green())).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testPartial16ColorFgStyle() {
        String expected =
                    '(' +
                        sgrCode(32) + 'a' +
                        AnsiConstants.RESET + ", " +
                        sgrCode(31) + 'b' +
                        AnsiConstants.RESET + ')';

        String actual = text("a").styled(Styles.fg(Color.green()))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.fg(Color.red())))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testPartial16ColorFgStyleWithDisplayStyle() {
        String expected =
                sgrCode(3) + '(' +
                        sgrCode(32) + 'a' +
                        sgrCode(39) + ", " +
                        sgrCode(31) + 'b' +
                        sgrCode(39) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.fg(Color.green()))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.fg(Color.red())))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.italic())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested16ColorFgStyle() {
        String expected =
                sgrCode(37) + '(' +
                        sgrCode(32) + 'a' +
                        sgrCode(37) + ", " +
                        sgrCode(31) + 'b' +
                        sgrCode(37) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.fg(Color.green()))
                        .append(text(","))
                        .appendSpace(text("b").styled(Styles.fg(Color.red())))
                        .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                        .styled(Styles.fg(Color.white()))
                        .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested16ColorFgStyleFlattening() {
        String expected =
                sgrCode(37) + "(\n" +
                        "  " + sgrCode(32) + 'a' + sgrCode(37) + ",\n" +
                        "  " + sgrCode(31) + 'b' + sgrCode(37) + '\n' +
                        ')' + AnsiConstants.RESET;

        // expanded layout should still break over multiple lines
        String actual = text("a").styled(Styles.fg(Color.green()))
                .append(text(","))
                .appendLineOrSpace(text("b").styled(Styles.fg(Color.red())))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.white()))
                .render(1);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void test256ColorFgStyle() {
        String expected = xtermFgCode(128) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.fg(Color.xterm(128))).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested256ColorFgStyle() {
        String expected =
                xtermFgCode(255) + '(' +
                        xtermFgCode(128) + 'a' +
                        xtermFgCode(255) + ", " +
                        xtermFgCode(37) + 'b' +
                        xtermFgCode(255) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.fg(Color.xterm(128)))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.fg(Color.xterm(37))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.xterm(255)))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested256ColorFgStyleFlattening() {
        String expected =
                xtermFgCode(255) + "(\n" +
                        "  " + xtermFgCode(128) + 'a' + xtermFgCode(255) + ",\n" +
                        "  " + xtermFgCode(37) + 'b' + xtermFgCode(255) + '\n' +
                        ')' + AnsiConstants.RESET;

        // expanded layout should still break over multiple lines
        String actual = text("a").styled(Styles.fg(Color.xterm(128)))
                .append(text(","))
                .appendLineOrSpace(text("b").styled(Styles.fg(Color.xterm(37))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.xterm(255)))
                .render(1);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testRgbColorFgStyle() {
        String expected = rgbFgCode(220, 118, 51) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.fg(Color.rgb(220, 118, 51))).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedRgbColorFgStyle() {
        String expected =
                rgbFgCode(20, 143, 119) + '(' +
                        rgbFgCode(220, 118, 51) + 'a' +
                        rgbFgCode(20, 143, 119) + ", " +
                        rgbFgCode(91, 44, 111) + 'b' +
                        rgbFgCode(20, 143, 119) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.fg(Color.rgb(220, 118, 51)))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.fg(Color.rgb(91, 44, 111))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.rgb(20, 143, 119)))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedRgbColorFgStyleFlattening() {
        String expected =
                rgbFgCode(20, 143, 119) + "(\n" +
                        "  " + rgbFgCode(220, 118, 51) + 'a' + rgbFgCode(20, 143, 119) + ",\n" +
                        "  " + rgbFgCode(91, 44, 111) + 'b' + rgbFgCode(20, 143, 119) + '\n' +
                        ')' + AnsiConstants.RESET;

        // expanded layout should still break over multiple lines
        String actual = text("a").styled(Styles.fg(Color.rgb(220, 118, 51)))
                .append(text(","))
                .appendLineOrSpace(text("b").styled(Styles.fg(Color.rgb(91, 44, 111))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.rgb(20, 143, 119)))
                .render(1);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNullBgStyle() {
        String expected = "a";
        String actual = text("a").styled(Styles.bg(null)).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedNullBgStyle() {
        String expected =
                sgrCode(47) + '(' +
                        sgrCode(49) + 'a' +
                        sgrCode(47) + ", " +
                        sgrCode(49) + 'b' +
                        sgrCode(47) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.bg(null))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.bg(null)))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.bg(Color.white()))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void test16ColorBgStyle() {
        String expected = sgrCode(42) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.bg(Color.green())).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testPartial16ColorBgStyle() {
        String expected =
                '(' +
                        sgrCode(42) + 'a' +
                        AnsiConstants.RESET + ", " +
                        sgrCode(41) + 'b' +
                        AnsiConstants.RESET + ')';

        String actual = text("a").styled(Styles.bg(Color.green()))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.bg(Color.red())))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testPartial16ColorBgStyleWithDisplayStyle() {
        String expected =
                sgrCode(3) + '(' +
                        sgrCode(42) + 'a' +
                        sgrCode(49) + ", " +
                        sgrCode(41) + 'b' +
                        sgrCode(49) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.bg(Color.green()))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.bg(Color.red())))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.italic())
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested16ColorBgStyle() {
        String expected =
                sgrCode(47) + '(' +
                        sgrCode(42) + 'a' +
                        sgrCode(47) + ", " +
                        sgrCode(41) + 'b' +
                        sgrCode(47) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.bg(Color.green()))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.bg(Color.red())))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.bg(Color.white()))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested16ColorBgStyleFlattening() {
        String expected =
                sgrCode(47) + "(\n" +
                        "  " + sgrCode(42) + 'a' + sgrCode(47) + ",\n" +
                        "  " + sgrCode(41) + 'b' + sgrCode(47) + '\n' +
                        ')' + AnsiConstants.RESET;

        // expanded layout should still break over multiple lines
        String actual = text("a").styled(Styles.bg(Color.green()))
                .append(text(","))
                .appendLineOrSpace(text("b").styled(Styles.bg(Color.red())))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.bg(Color.white()))
                .render(1);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void test256ColorBgStyle() {
        String expected = xtermBgCode(128) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.bg(Color.xterm(128))).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested256ColorBgStyle() {
        String expected =
                xtermBgCode(255) + '(' +
                        xtermBgCode(128) + 'a' +
                        xtermBgCode(255) + ", " +
                        xtermBgCode(37) + 'b' +
                        xtermBgCode(255) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.bg(Color.xterm(128)))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.bg(Color.xterm(37))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.bg(Color.xterm(255)))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNested256ColorBgStyleFlattening() {
        String expected =
                xtermFgCode(255) + "(\n" +
                        "  " + xtermFgCode(128) + 'a' + xtermFgCode(255) + ",\n" +
                        "  " + xtermFgCode(37) + 'b' + xtermFgCode(255) + '\n' +
                        ')' + AnsiConstants.RESET;

        // expanded layout should still break over multiple lines
        String actual = text("a").styled(Styles.fg(Color.xterm(128)))
                .append(text(","))
                .appendLineOrSpace(text("b").styled(Styles.fg(Color.xterm(37))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.xterm(255)))
                .render(1);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testRgbColorBgStyle() {
        String expected = rgbBgCode(220, 118, 51) + "a" + AnsiConstants.RESET;
        String actual = text("a").styled(Styles.bg(Color.rgb(220, 118, 51))).render(80);
        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();
        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedRgbColorBgStyle() {
        String expected =
                rgbBgCode(20, 143, 119) + '(' +
                        rgbBgCode(220, 118, 51) + 'a' +
                        rgbBgCode(20, 143, 119) + ", " +
                        rgbBgCode(91, 44, 111) + 'b' +
                        rgbBgCode(20, 143, 119) + ')' + AnsiConstants.RESET;

        String actual = text("a").styled(Styles.bg(Color.rgb(220, 118, 51)))
                .append(text(","))
                .appendSpace(text("b").styled(Styles.bg(Color.rgb(91, 44, 111))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.bg(Color.rgb(20, 143, 119)))
                .render(6);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testNestedRgbColorBgStyleFlattening() {
        String expected =
                rgbFgCode(20, 143, 119) + "(\n" +
                        "  " + rgbFgCode(220, 118, 51) + 'a' + rgbFgCode(20, 143, 119) + ",\n" +
                        "  " + rgbFgCode(91, 44, 111) + 'b' + rgbFgCode(20, 143, 119) + '\n' +
                        ')' + AnsiConstants.RESET;

        // expanded layout should still break over multiple lines
        String actual = text("a").styled(Styles.fg(Color.rgb(220, 118, 51)))
                .append(text(","))
                .appendLineOrSpace(text("b").styled(Styles.fg(Color.rgb(91, 44, 111))))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.fg(Color.rgb(20, 143, 119)))
                .render(1);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testRenderToAppendable() throws IOException {
        Doc doc = text("a");
        Writer writer = new StringWriter();
        doc.render(80, writer);
        assertThat(writer.toString(), is("a"));
    }

    @Test
    void testRenderToAppendableDefaultOptions() throws IOException {
        Doc doc = text("a");
        Writer writer = new StringWriter();
        doc.render(writer);
        assertThat(writer.toString(), is("a"));
    }

    @Test
    void testRenderAnsiDisabled() {
        RenderOptions options = RenderOptions.builder()
                .lineWidth(6)
                .emitAnsiEscapes(false)
                .build();

        String expected = "(a, b)";

        String actual = text("a").styled(Styles.blink())
                .append(text(","))
                .appendSpace(text("b").styled(Styles.blink()))
                .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
                .styled(Styles.faint())
                .render(options);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = actual.toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testRenderToAppendableAnsiDisabled() throws IOException {
        Writer writer = new StringWriter();

        RenderOptions options = RenderOptions.builder()
                .lineWidth(6)
                .emitAnsiEscapes(false)
                .build();

        String expected = "(a, b)";

        text("a").styled(Styles.blink())
            .append(text(","))
            .appendSpace(text("b").styled(Styles.blink()))
            .bracket(2, Doc.lineOrEmpty(), text("("), text(")"))
            .styled(Styles.faint())
            .render(options, writer);

        char[] expectedChars = expected.toCharArray();
        char[] actualChars = writer.toString().toCharArray();

        assertThat(actualChars, is(equalTo(expectedChars)));
    }

    @Test
    void testRenderWithUnboundParam() {
        Doc unbound = Doc.param("a");
        assertThrows(IllegalStateException.class, unbound::render);
    }

    @Test
    void testRenderWithParamBoundToItself() {
        Doc unbound = Doc.param("a");
        Doc bound = unbound.bind("a", unbound);
        assertThrows(IllegalStateException.class, bound::render);
    }

    @Test
    void testRenderWithMultipleParams() {
        Doc unbound = param("a")
                .append(text(","))
                .appendLineOrSpace(param("b"))
                .bracket(2, lineOrEmpty(), text("("), text(")"));

        String rendered = unbound.bind(
                        "a", text("1"),
                        "b", text("2"))
                .render(80);

        assertThat(rendered, is(equalTo("(1, 2)")));
    }

    @Test
    void testRenderWithOddParams() {
        Doc unbound = param("a")
                .append(text(","))
                .appendLineOrSpace(param("b"))
                .bracket(2, lineOrEmpty(), text("("), text(")"));

        assertThrows(IllegalArgumentException.class, () -> {
            unbound.bind(
                    "a", text("1"),
                    "b");
        });
    }

    @Test
    void testRenderWithIllTypedParamKey() {
        Doc unbound = param("a")
                .append(text(","))
                .appendLineOrSpace(param("b"))
                .bracket(2, lineOrEmpty(), text("("), text(")"));

        assertThrows(IllegalArgumentException.class, () -> {
            unbound.bind(
                    "a", text("1"),
                    1, text("2"));
        });
    }

    @Test
    void testRenderWithIllTypedParamValue() {
        Doc unbound = param("a")
                .append(text(","))
                .appendLineOrSpace(param("b"))
                .bracket(2, lineOrEmpty(), text("("), text(")"));

        assertThrows(IllegalArgumentException.class, () -> {
            unbound.bind(
                    "a", text("1"),
                    "b", "2");
        });
    }

    /**
     * This tests the {@code spread} operator of the original paper, implemented via
     * the Haskell functions:
     *
     * <pre>{@code
     * x <+> y = x <> text " " <> y
     * spread = folddoc (<+>)
     * }</pre>
     */
    @Test
    void testSpreadOperator() {
        String expected = "a b c";
        String actual = Doc.fold(
                Arrays.asList(Doc.text("a"), Doc.text("b"), Doc.text("c")),
                Doc::appendSpace).render(80);
        assertThat(actual, is(equalTo(expected)));
    }

    /**
     * This tests the {@code stack} operator of the original paper, implemented via
     * the Haskell functions:
     *
     * <pre>{@code
     * x </> y = x <> line <> y
     * stack = folddoc (</>)
     * }</pre>
     */
    @Test
    void testStackOperator() {
        String expected = "a\nb\nc";
        String actual = Doc.fold(
                Arrays.asList(Doc.text("a"), Doc.text("b"), Doc.text("c")),
                Doc::appendLine).render(80);
        assertThat(actual, is(equalTo(expected)));
    }

    /**
     * This property represents the observation that an atomic token must render
     * as nothing more than its text content.
     */
    @Property
    void textRenderEquivalentToIdentity(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll String text) {
        String rendered = text(text).render(width);
        assertThat(rendered, is(equalTo(text)));
    }

    /**
     * This property represents the observation that grouping does
     * not affect text nodes.
     */
    @Property
    void groupTextEquivalentToIdentity(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll String text) {
        String rendered = group(text(text)).render(width);
        assertThat(rendered, is(equalTo(text)));
    }

    /**
     * This property observes the left unit law mentioned
     * in the original paper:
     *
     * <pre>
     * x <> nil = x
     * </pre>
     */
    @Property
    void leftUnitLaw(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll("noParamDocs") Doc doc) {
        String appended = doc.append(Doc.empty()).render(width);
        String original = doc.render(width);
        assertThat(appended, is(equalTo(original)));
    }

    /**
     * This property observes the right unit law mentioned
     * in the original paper:
     *
     * <pre>
     * nil <> x = x
     * </pre>
     */
    @Property
    void rightUnitLaw(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll("noParamDocs") Doc doc) {
        String appended = Doc.empty().append(doc).render(width);
        String original = doc.render(width);
        assertThat(appended, is(equalTo(original)));
    }

    /**
     * This property observes the associativity law mentioned
     * in the original paper:
     *
     * <pre>
     * x <> (y <> z) = (x <> y) <> z
     * </pre>
     */
    @Property
    void associativityLaw(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll("noParamDocs") Doc x,
            @ForAll("noParamDocs") Doc y,
            @ForAll("noParamDocs") Doc z) {
        String leftAssociated = x.append(y).append(z).render(width);
        String rightAssociated = x.append(y.append(z)).render(width);
        assertThat(leftAssociated, is(equalTo(rightAssociated)));
    }

    /**
     * This property corresponds to the original paper's law:
     *
     * <pre>
     * text (s ++ t) = text s <> text t
     * </pre>
     */
    @Property
    void appendEquivalentToStringConcat(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll String left,
            @ForAll String right) {
        String appended = text(left).append(text(right)).render(width);
        String concatenated = text(left + right).render(width);
        assertThat(appended, is(equalTo(concatenated)));
    }

    /**
     * This property corresponds to the original paper's law:
     *
     * <pre>
     * text "" = nil
     * </pre>
     */
    @Property
    void emptyEquivalentToEmptyText(@ForAll @IntRange(min = 5, max = 200) int width) {
        String emptyText = text("").render(width);
        String emptyDoc = Doc.empty().render(width);
        assertThat(emptyText, is(equalTo(emptyDoc)));
    }

    /**
     * This property corresponds to the original paper's law:
     *
     * <pre>
     * nest (i+j) x = nest i (nest j x)
     * </pre>
     */
    @Property
    void nestedIndentEquivalentToSumIndent(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll @IntRange(min = 0, max = 200) int i,
            @ForAll @IntRange(min = 0, max = 200) int j,
            @ForAll("noParamDocs") Doc doc) {
        String sumIndent = doc.indent(i + j).render(width);
        String nestedIndent = doc.indent(j).indent(i).render(width);
        assertThat(sumIndent, is(equalTo(nestedIndent)));
    }

    /**
     * This property corresponds to the original paper's law:
     *
     * <pre>
     * nest 0 x = x
     * </pre>
     */
    @Property
    void indentZeroEquivalentToNoIndent(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll("noParamDocs") Doc doc) {
        String zeroIndent = doc.indent(0).render(width);
        String noIndent = doc.render(width);
        assertThat(zeroIndent, is(equalTo(noIndent)));
    }

    /**
     * This property corresponds to the original paper's law:
     *
     * <pre>
     * nest i (x <> y) = nest i x <> nest i y
     * </pre>
     */
    @Property
    void indentDistributesOverAppend(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll @IntRange(min = 0, max = 200) int indent,
            @ForAll("noParamDocs") Doc left,
            @ForAll("noParamDocs") Doc right) {
        String indentedAppend = left.append(right).indent(indent).render(width);
        String appendedIndents = left.indent(indent).append(right.indent(indent)).render(width);
        assertThat(indentedAppend, is(equalTo(appendedIndents)));
    }

    /**
     * This property corresponds to the original paper's law:
     *
     * <pre>
     * nest i nil = nil
     * </pre>
     */
    @Property
    void emptyUnaffectedByIndent(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll @IntRange(min = 0, max = 200) int indent) {
        String indentedEmpty = Doc.empty().indent(indent).render(width);
        String noIndentEmpty = Doc.empty().render(width);
        assertThat(indentedEmpty, is(equalTo(noIndentEmpty)));
    }

    /**
     * This property corresponds to the original paper's law:
     *
     * <pre>
     * nest i (text s) = text s
     * </pre>
     */
    @Property
    void topLevelIndentEquivalentToNoIndent(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll @IntRange(min = 0, max = 200) int indent,
            @ForAll String text) {
        String topLevelIndent = text(text).indent(indent).render(width);
        String noIndent = text(text).render(width);
        assertThat(topLevelIndent, is(equalTo(noIndent)));
    }

    @Property
    void paramHasParams(@ForAll String paramName) {
        Doc boundDoc = Doc.param(paramName);
        assertThat(boundDoc.hasParams(), is(true));
    }

    @Property
    void paramBindingEliminatesParam(
            @ForAll String paramName,
            @ForAll("noParamDocs") Doc argDoc
    ) {
        Doc boundDoc = Doc.param(paramName)
                .bind(paramName, argDoc);
        assertThat(boundDoc.hasParams(), is(false));
    }

    @Property
    void paramBindingWrongNameDoesNothing(
            @ForAll String paramName,
            @ForAll String unrelatedName,
            @ForAll("noParamDocs") Doc argDoc
    ) {
        Assume.that(!paramName.equals(unrelatedName));

        Doc paramDoc = Doc.param(paramName);

        Doc boundDoc = paramDoc.bind(unrelatedName, argDoc);

        assertThat(boundDoc, is(sameInstance(paramDoc)));

        assertThat(boundDoc.hasParams(), is(true));
    }

    @Property
    void paramBindingWrongNameMapDoesNothing(
            @ForAll String paramName,
            @ForAll String unrelatedName1,
            @ForAll String unrelatedName2,
            @ForAll("noParamDocs") Doc argDoc
    ) {
        Assume.that(!paramName.equals(unrelatedName1));
        Assume.that(!paramName.equals(unrelatedName2));
        Assume.that(!unrelatedName1.equals(unrelatedName2));

        Doc paramDoc = Doc.param(paramName);

        Doc boundDoc = paramDoc.bind(
                unrelatedName1, argDoc,
                unrelatedName2, argDoc);

        assertThat(boundDoc, is(sameInstance(paramDoc)));

        assertThat(boundDoc.hasParams(), is(true));
    }

    @Property
    void bindingTopLevelParamEquivalentToArgDoc(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll String paramName,
            @ForAll("noParamDocs") Doc argDoc
    ) {
        String renderedArg = argDoc.render(width);

        String boundParam = Doc.param(paramName)
                .bind(paramName, argDoc)
                .render(width);

        assertThat(boundParam, is(equalTo(renderedArg)));
    }

    @Property
    void bindingTopLevelParamWithStringEquivalentToText(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll String paramName,
            @ForAll String paramValue
    ) {
        String renderedText = Doc.text(paramValue).render(width);

        String boundParam = Doc.param(paramName)
                .bind(paramName, paramValue)
                .render(width);

        assertThat(boundParam, is(equalTo(renderedText)));
    }

    @Property
    void bindingDocWithoutParamsDoesNothing(
            @ForAll("noParamDocs") Doc doc,
            @ForAll String paramName,
            @ForAll("noParamDocs") Doc argDoc
    ) {
        Doc boundDoc = doc.bind(paramName, argDoc);
        assertThat(boundDoc, is(equalTo(doc)));
    }

    @Property
    void paramIsEquivalentToInlining(
            @ForAll @IntRange(min = 5, max = 200) int width,
            @ForAll("unaryDocs") UnaryOperator<Doc> unaryDoc,
            @ForAll String paramName,
            @ForAll("noParamDocs") Doc argDoc
    ) {
        String inlined = unaryDoc.apply(argDoc).render(width);

        String parameterized = unaryDoc
                .apply(param(paramName))
                .bind(paramName, argDoc)
                .render(width);

        assertThat(parameterized, is(equalTo(inlined)));
    }

    @Test
    void testEquals() {
        Doc left = docs().sample();

        Doc right = docs().sampleStream()
                .filter(r -> !left.equals(r))
                .findFirst().get();

        // EqualsVerifier doesn't work with singletons,
        // so we can't test Reset, Line, LineOrSpace, LineOrEmpty or Empty:
        // it requires prefab values for recursive data types and
        // those prefab values must not be equal to each other
        EqualsVerifier
                .forClasses(
                        Text.class, Append.class, Param.class,
                        Alternatives.class, Indent.class,
                        LineOr.class, Escape.class, Styled.class)
                .usingGetClass()
                .withPrefabValues(Doc.class, left, right)
                .verify();
    }

    @Test
    void testToString() {
        ToStringVerifier
                .forClasses(
                        Text.class, Append.class,
                        Alternatives.class, Indent.class,
                        LineOr.class, Empty.class, Escape.class,
                        Reset.class, Styled.class, Param.class)
                .withPrefabValue(Doc.class, docs().sample())
                .verify();

        ToStringVerifier
                .forClasses(Line.class, LineOrSpace.class, LineOrEmpty.class)
                .withIgnoredFields("altDoc")
                .verify();
    }

    @Provide
    Arbitrary<Doc> paramDocs() {
        return docs().filter(Doc::hasParams);
    }

    @Provide
    Arbitrary<Doc> noParamDocs() {
        return docs().filter(doc -> !doc.hasParams());
    }

    @Provide
    Arbitrary<UnaryOperator<Doc>> unaryDocs() {
        return Arbitraries.lazyOf(
            () -> Arbitraries.of(Doc::group),
            () -> Arbitraries.of(Doc::lineOr),
            () -> Arbitraries.of(doc -> doc.indent(2)),
            () -> Arbitraries.of(doc -> doc.bracket(2, lineOrEmpty(), text("["), text("]"))),
            () -> noParamDocs().map(doc1 -> doc1::append),
            () -> noParamDocs().map(doc1 -> doc2 -> doc2.append(doc1)),
            () -> unaryDocs().tuple2().map(tuple -> doc -> tuple.get1().andThen(tuple.get2()).apply(doc))
        );
    }

    @Provide
    Arbitrary<Styles.StylesOperator> styles() {
        return Arbitraries.lazyOf(
            () -> colors().map(Styles::fg),
            () -> colors().map(Styles::bg),
            () -> Arbitraries.of(Styles.bold()),
            () -> Arbitraries.of(Styles.faint()),
            () -> Arbitraries.of(Styles.italic()),
            () -> Arbitraries.of(Styles.underline()),
            () -> Arbitraries.of(Styles.blink()),
            () -> Arbitraries.of(Styles.inverse()),
            () -> Arbitraries.of(Styles.strikethrough())
        );
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

    @Provide
    Arbitrary<Doc> docs() {
        return Arbitraries.lazyOf(
                // Text
                () -> Arbitraries.strings().ofMaxLength(100).map(Doc::text),
                // Line
                () -> Arbitraries.just(Doc.line()),
                // LineOrSpace
                () -> Arbitraries.just(Doc.lineOrSpace()),
                // LineOrEmpty
                () -> Arbitraries.just(Doc.lineOrEmpty()),
                // LineOr
                () -> docs().map(Doc::lineOr),
                () -> Arbitraries.strings().ofMaxLength(10).map(Doc::lineOr),
                // Empty
                () -> Arbitraries.just(Doc.empty()),
                // Append
                () -> docs().tuple2().map(tuple -> tuple.get1().append(tuple.get2())),
                // Indent
                () -> docs().map(doc -> doc.indent(2)),
                // Bracketing
                () -> docs().map(doc -> doc.bracket(2, Doc.lineOrEmpty(), Doc.text("["), Doc.text("]"))),
                // Alternatives
                () -> docs().map(Doc::group),
                // Param
                () -> Arbitraries.strings().map(Doc::param),
                // Styled
                () -> styles().array(Styles.StylesOperator[].class).flatMap(styles -> {
                    return docs().map(doc -> doc.styled(styles));
                })
        );
    }

    String sgrCode(int code) {
        return AnsiConstants.CSI + code + 'm';
    }

    String xtermFgCode(int code) {
        return AnsiConstants.CSI + "38;5;" + code + 'm';
    }

    String xtermBgCode(int code) {
        return AnsiConstants.CSI + "48;5;" + code + 'm';
    }

    String rgbFgCode(int r, int g, int b) {
        return AnsiConstants.CSI + "38;2;" + r + ';' + g + ';' + b + 'm';
    }

    String rgbBgCode(int r, int g, int b) {
        return AnsiConstants.CSI + "48;2;" + r + ';' + g + ';' + b + 'm';
    }
}
