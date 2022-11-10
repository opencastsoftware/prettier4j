package com.opencastsoftware.prettier4j;

import static com.opencastsoftware.prettier4j.Doc.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.jparams.verifier.tostring.ToStringVerifier;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import nl.jqno.equalsverifier.EqualsVerifier;

public class DocTest {
    /** The example tree data structure from the original paper */
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
        String actual = group(text("functionCall")
                .appendLineOrEmpty(
                        Doc.intersperse(
                                Doc.text(",").append(Doc.lineOrSpace()),
                                Arrays.asList("a", "b", "c").stream().map(Doc::text))
                                .bracket(2, Doc.lineOrEmpty(), Doc.text("("), Doc.text(")"))))
                .render(80);

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    void testBracketStringOverload() {
        String expected = "functionCall(a, b, c)";
        String actual = group(text("functionCall")
                .appendLineOrEmpty(
                        Doc.intersperse(
                                Doc.text(",").append(Doc.lineOrSpace()),
                                Arrays.asList("a", "b", "c").stream().map(Doc::text))
                                .bracket(2, Doc.lineOrEmpty(), "(", ")")))
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
                (l, r) -> l.appendSpace(r)).render(80);
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
                (l, r) -> l.appendLine(r)).render(80);
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
            @ForAll("docs") Doc doc) {
        String appended = doc.append(empty()).render(width);
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
            @ForAll("docs") Doc doc) {
        String appended = empty().append(doc).render(width);
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
            @ForAll("docs") Doc x,
            @ForAll("docs") Doc y,
            @ForAll("docs") Doc z) {
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
        String emptyDoc = empty().render(width);
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
            @ForAll("docs") Doc doc) {
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
            @ForAll("docs") Doc doc) {
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
            @ForAll("docs") Doc left,
            @ForAll("docs") Doc right) {
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
        String indentedEmpty = empty().indent(indent).render(width);
        String noIndentEmpty = empty().render(width);
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

    @Test
    void testEquals() {
        Doc left = docs().sample();

        Doc right = docs().sampleStream()
                .filter(r -> !left.equals(r))
                .findFirst().get();

        // EqualsVerifier doesn't work with singletons,
        // so we can't test Line, LineOrSpace, LineOrEmpty or Empty:
        // it requires prefab values for recursive data types and
        // those prefab values must not be equal to each other
        EqualsVerifier
                .forClasses(
                        Text.class, Append.class,
                        Alternatives.class, Indent.class,
                        LineOr.class)
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
                        LineOr.class, Empty.class)
                .withPrefabValue(Doc.class, docs().sample())
                .verify();

        ToStringVerifier
                .forClasses(Line.class, LineOrSpace.class, LineOrEmpty.class)
                .withIgnoredFields("altDoc")
                .verify();
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
                () -> docs().map(Doc::group));
    }
}
