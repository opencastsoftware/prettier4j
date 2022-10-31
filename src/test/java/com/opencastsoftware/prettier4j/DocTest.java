package com.opencastsoftware.prettier4j;

import static com.opencastsoftware.prettier4j.Doc.empty;
import static com.opencastsoftware.prettier4j.Doc.text;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

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
        String nestedIndent = doc.indent(i).indent(j).render(width);
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

    @Provide
    Arbitrary<Doc> docs() {
        return Arbitraries.lazyOf(
                // Text
                () -> Arbitraries.strings().ofMaxLength(100).map(Doc::text),
                // Line
                () -> Arbitraries.just(Doc.line()),
                // LineOr
                () -> Arbitraries.chars().map(ch -> String.valueOf(ch)).map(Doc::lineOr),
                // Empty
                () -> Arbitraries.just(Doc.empty()),
                // Append
                () -> docs().tuple2().map(tuple -> tuple.get1().append(tuple.get2())),
                // Alternatives
                () -> docs().map(Doc::group));
    }
}
