/*
 * SPDX-FileCopyrightText:  © 2022-2025 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j;

import com.opencastsoftware.prettier4j.ansi.AnsiConstants;
import com.opencastsoftware.prettier4j.ansi.Attrs;
import com.opencastsoftware.prettier4j.ansi.AttrsStack;
import com.opencastsoftware.prettier4j.ansi.Styles;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Implements the algorithm described in Philip Wadler's "A prettier printer", a
 * pretty printing algorithm for laying out hierarchical documents as text.
 * <p>
 * To construct a document, see the static methods of {@link Doc}, especially
 * {@link Doc#text(String) text}, {@link Doc#empty() empty}, {@link Doc#line() line}
 * and its related methods.
 * <p>
 * To concatenate documents, see {@link Doc#append(Doc) append} and its related
 * instance methods.
 * <p>
 * To declare groups of content which should be collapsed onto one line if
 * possible, see the static method {@link Doc#group(Doc) group}.
 * <p>
 * To style a {@link Doc} with ANSI escape codes, see the instance method
 * {@link Doc#styled(Styles.StylesOperator...)} or static method
 * {@link Doc#styled(Doc, Styles.StylesOperator...)}.
 * <p>
 * To render documents to an {@link Appendable} output, see the instance method
 * {@link Doc#render(RenderOptions, Appendable)} or static method
 * {@link Doc#render(Doc, RenderOptions, Appendable)}.
 * <p>
 * To render documents to {@link String}, see the instance method
 * {@link Doc#render(RenderOptions)} or static method
 * {@link Doc#render(Doc, RenderOptions)}.
 *
 * @see <a href="https://web.archive.org/web/20240429003710/https://homepages.inf.ed.ac.uk/wadler/papers/prettier/prettier.pdf">
 *     A prettier printer
 *     </a>
 */
public abstract class Doc {
    private Doc() {}

    /**
     * Returns a flattened layout for the current {@link Doc}.
     *
     * @return the flattened document.
     */
    abstract Doc flatten();

    /**
     * Indicate whether the current {@link Doc}
     * contains any parameters.
     *
     * @return whether this {@link Doc} contains any parameters.
     */
    abstract boolean hasParams();

    /**
     * Indicate whether the current {@link Doc}
     * contains any line separators.
     *
     * @return whether this {@link Doc} contains any line separators.
     */
    abstract boolean hasLineSeparators();

    /**
     * Bind a named parameter to the {@link Doc} provided via {@code value}.
     *
     * @param name  the name of the parameter.
     * @param value the value to use to replace the parameter placeholder.
     * @return this {@link Doc} with all instances of the named parameter replaced by {@code value}.
     */
    public abstract Doc bind(String name, Doc value);

    /**
     * Bind named parameters to the {@link Doc}s provided via {@code bindings}.
     *
     * @param bindings the bindings to use to replace named parameters.
     * @return this {@link Doc} with all matching named parameters replaced by their corresponding values.
     */
    public abstract Doc bind(Map<String, Doc> bindings);

    /**
     * Bind named parameters to the name-to-{@link Doc} pairs provided via {@code bindings}.
     *
     * @param bindings the bindings to use to replace named parameters.
     * @return this {@link Doc} with all matching named parameters replaced by their corresponding values.
     */
    public Doc bind(Object... bindings) {
        if (bindings.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "String-to-Doc pairs of arguments must be provided, but " +
                            bindings.length + " arguments were found.");
        }

        Map<String, Doc> bindingsMap = new HashMap<>();

        for (int i = 0; i < bindings.length; i += 2) {
            if (!(bindings[i] instanceof String)) {
                throw new IllegalArgumentException(
                        "Key type must be String, but was " +
                                bindings[i].getClass().getSimpleName() +
                                " at index " + i + '.');
            }
            if (!(bindings[i + 1] instanceof Doc)) {
                throw new IllegalArgumentException(
                        "Value type must be Doc, but was " +
                                bindings[i + 1].getClass().getSimpleName() +
                                " at index" + (i + 1) + '.');
            }

            bindingsMap.put(
                    (String) bindings[i],
                    (Doc) bindings[i + 1]);
        }

        return bind(bindingsMap);
    }

    /**
     * Bind a named parameter to the {@link String} provided via {@code value}.
     *
     * @return this {@link Doc} with all instances of the named parameter replaced by {@code value}.
     */
    public Doc bind(String name, String value) {
        return bind(name, Doc.text(value));
    }

    /**
     * Append the {@code other} {@link Doc} to this one.
     *
     * @param other the other document.
     * @return the concatenated {@link Doc}.
     */
    public Doc append(Doc other) {
        // By left unit law
        if (other instanceof Empty) { return this; }
        return new Append(this, other);
    }

    /**
     * Append the {@code other} {@link Doc} to this one, separated by a space character.
     *
     * @param other the other document.
     * @return the concatenated {@link Doc}.
     */
    public Doc appendSpace(Doc other) {
        return this
                .append(text(" "))
                .append(other);
    }

    /**
     * Append the {@code other} {@link Doc} to this one,
     * separated by a line break which cannot be flattened.
     *
     * @param other the other document.
     * @return the concatenated {@link Doc}.
     */
    public Doc appendLine(Doc other) {
        return this
                .append(line())
                .append(other);
    }

    /**
     * Append the {@code other} {@link Doc Doc} to this one, separated by
     * a line break which may be flattened into a space character.
     *
     * @param other the other document.
     * @return the concatenated {@link Doc}.
     */
    public Doc appendLineOrSpace(Doc other) {
        return this
                .append(lineOrSpace())
                .append(other);
    }

    /**
     * Append the {@code other} {@link Doc} to this one, separated by
     * a line break which may be flattened into an empty document.
     *
     * @param other the other document.
     * @return the concatenated {@link Doc}.
     */
    public Doc appendLineOrEmpty(Doc other) {
        return this
                .append(lineOrEmpty())
                .append(other);
    }

    /**
     * Append the {@code other} {@link Doc} to this one, separated by
     * a line break which may be flattened into the {@code altText} {@link String}.
     *
     * @param altText the alternative text to display if the line break is
     *                flattened.
     * @param other   the other document.
     * @return the concatenated {@link Doc}.
     */
    public Doc appendLineOr(String altText, Doc other) {
        return this.appendLineOr(text(altText), other);
    }

    /**
     * Append the {@code other} {@link Doc} to this one, separated by
     * a line break which may be flattened into the {@code altDoc} document.
     *
     * @param altDoc the alternative document to display if the line break is
     *               flattened.
     * @param other  the other document.
     * @return the concatenated {@link Doc}.
     */
    public Doc appendLineOr(Doc altDoc, Doc other) {
        return this
                .append(lineOr(altDoc))
                .append(other);
    }

    /**
     * Indent the current {@link Doc} by {@code indent} spaces.
     *
     * @param indent the number of spaces of indent to apply.
     * @return the indented document.
     */
    public Doc indent(int indent) {
        return indent(indent, this);
    }

    /**
     * Bracket the current document by the {@code left} and {@code right} Strings,
     * indented by {@code indent} spaces.
     * <p>
     * When collapsed, line separators are replaced by spaces.
     *
     * @param indent the number of spaces of indent to apply.
     * @param left   the left-hand bracket String.
     * @param right  the right-hand bracket String.
     * @return the bracketed document.
     */
    public Doc bracket(int indent, String left, String right) {
        return bracket(indent, text(left), text(right));
    }

    /**
     * Bracket the current document by the {@code left} and {@code right} documents,
     * indented by {@code indent} spaces.
     * <p>
     * When collapsed, line separators are replaced by spaces.
     *
     * @param indent the number of spaces of indent to apply.
     * @param left   the left-hand bracket document.
     * @param right  the right-hand bracket document.
     * @return the bracketed document.
     */
    public Doc bracket(int indent, Doc left, Doc right) {
        return bracket(indent, Doc.lineOrSpace(), left, right);
    }

    /**
     * Bracket the current document by the {@code left} and {@code right} Strings,
     * indented by {@code indent} spaces.
     * <p>
     * When collapsed, line separators are replaced by the {@code lineDoc}.
     *
     * @param indent  the number of spaces of indent to apply.
     * @param lineDoc the line separator document.
     * @param left    the left-hand bracket document.
     * @param right   the right-hand bracket document.
     * @return the bracketed document.
     */
    public Doc bracket(int indent, Doc lineDoc, String left, String right) {
        return bracket(indent, lineDoc, Doc.text(left), Doc.text(right));
    }

    /**
     * Bracket the current document by the {@code left} and {@code right} documents,
     * indented by {@code indent} spaces.
     * <p>
     * When collapsed, line separators are replaced by the {@code lineDoc}.
     *
     * @param indent  the number of spaces of indent to apply.
     * @param lineDoc the line separator document.
     * @param left    the left-hand bracket document.
     * @param right   the right-hand bracket document.
     * @return the bracketed document.
     */
    public Doc bracket(int indent, Doc lineDoc, Doc left, Doc right) {
        return bracket(indent, lineDoc, empty(), left, right);
    }

    /**
     * Bracket the current document by the {@code left} and {@code right} documents,
     * indented by {@code indent} spaces, applying the margin document {@code margin}.
     * <p>
     * When collapsed, line separators are replaced by the {@code lineDoc}.
     *
     * @param indent    the number of spaces of indent to apply.
     * @param lineDoc   the line separator document.
     * @param marginDoc the margin document.
     * @param left      the left-hand bracket document.
     * @param right     the right-hand bracket document.
     * @return the bracketed document.
     */
    public Doc bracket(int indent, Doc lineDoc, Doc marginDoc, Doc left, Doc right) {
        return group(
                left
                        .append(lineDoc.append(this).indent(indent).margin(marginDoc))
                        .append(lineDoc.append(right)));
    }

    /**
     * Apply the margin document {@code margin} to the current {@link Doc}, emitting the
     * margin at the start of every new line from the start of this document until the
     * end of the document.
     * <p>
     * Note that line separators are forbidden inside the margin document.
     * <p>
     * This is because each line separator causes the margin document to be produced.
     * <p>
     * If the margin document in turn contained a line separator, rendering would never terminate.
     *
     * @param margin the margin document to apply at the start of every line.
     * @return a document which prefixes every new line with the {@code margin} document.
     * @throws IllegalArgumentException if the margin document contains line separators.
     */
    public Doc margin(Doc margin) {
        return margin(margin, this);
    }

    /**
     * Styles the current {@link Doc} using the styles provided via {@code styles}.
     *
     * @param styles the styles to use to decorate the input {@code doc}.
     * @return a {@link Doc} decorated with the ANSI styles provided.
     * @see Styles
     * @see com.opencastsoftware.prettier4j.ansi.Color Color
     */
    public final Doc styled(Styles.StylesOperator... styles) {
        return styled(this, styles);
    }

    /**
     * Converts the current {@link Doc} into a hyperlink to the {@code uri} provided.
     *
     * @param uri the {@link URI} to link to.
     * @return a {@link Doc} which acts as a hyperlink to the {@code uri}.
     * @implNote This implementation does not currently handle nested hyperlinks, so
     * all hyperlinks will be closed after the end of the first nested hyperlink.
     * @see <a href="https://web.archive.org/web/20240525100920/https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda">Hyperlinks (a.k.a. HTML-like anchors) in terminal emulators</a>
     */
    public final Doc link(URI uri) {
        return link(this, uri);
    }

    /**
     * Renders the current {@link Doc} into a {@link String}, attempting to lay out the document
     * according to the {@link RenderOptions#defaults() default} rendering options.
     *
     * @return the document laid out as a {@link String}.
     */
    public String render() {
        return render(this);
    }

    /**
     * Renders the current {@link Doc} into a {@link String}, attempting to lay out the document
     * according to the rendering {@code options}.
     *
     * @param options the options to use for rendering.
     * @return the document laid out as a {@link String}.
     */
    public String render(RenderOptions options) {
        return render(this, options);
    }

    /**
     * Renders the current {@link Doc Doc} into an {@link Appendable}, attempting to lay out the document
     * according to the {@link RenderOptions#defaults() default} rendering options.
     *
     * @param output the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    public void render(Appendable output) throws IOException {
        render(this, output);
    }

    /**
     * Renders the current {@link Doc} into a {@link String}, attempting to lay out the document with at
     * most {@code width} characters on each line.
     *
     * @param width the preferred maximum rendering width.
     * @return the document laid out as a {@link String}.
     */
    public String render(int width) {
        return render(this, new RenderOptions(width, true));
    }

    /**
     * Renders the current {@link Doc} into an {@link Appendable}, attempting to lay out the document
     * with at most {@code width} characters on each line.
     *
     * @param width  the preferred maximum rendering width.
     * @param output the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    public void render(int width, Appendable output) throws IOException {
        render(this, new RenderOptions(width, true), output);
    }

    /**
     * Renders the current {@link Doc} into an {@link Appendable}, attempting to lay out the document
     * according to the rendering {@code options}.
     *
     * @param options the options to use for rendering.
     * @param output  the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    public void render(RenderOptions options, Appendable output) throws IOException {
        render(this, options, output);
    }

    /**
     * Represents an atomic piece of text.
     */
    public static class Text extends Doc {
        private final String text;

        Text(String text) {
            this.text = text;
        }

        public String text() {
            return text;
        }

        @Override
        public Doc append(Doc other) {
            // By string concat equivalency law
            if (other instanceof Text) {
                Text otherText = (Text) other;
                return text(this.text() + otherText.text());
            } else if (other instanceof Empty) {
                // By left unit law
                return this;
            }

            return new Append(this, other);
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        boolean hasParams() {
            return false;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((text == null) ? 0 : text.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Text other = (Text) obj;
            if (text == null) {
                if (other.text != null)
                    return false;
            } else if (!text.equals(other.text))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Text [text=" + text + "]";
        }
    }

    /**
     * Represents a long text string which may be wrapped to fit within
     * the preferred rendering width.
     */
    public static class WrapText extends Doc {
        private final String text;
        private final int offset;

        WrapText(String text, int offset) {
            this.text = text;
            this.offset = offset;
        }

        public String text() {
            return text;
        }

        public int offset() {
            return offset;
        }

        public Doc atOffset(int offset) {
            return new WrapText(this.text, offset);
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        boolean hasParams() {
            return false;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WrapText wrapText = (WrapText) o;
            return offset == wrapText.offset && Objects.equals(text, wrapText.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, offset);
        }

        @Override
        public String toString() {
            return "WrapText[" +
                    "text='" + text + '\'' +
                    ", offset=" + offset +
                    ']';
        }
    }

    /**
     * Represents the concatenation of two {@link Doc}s.
     */
    public static class Append extends Doc {
        private final Doc left;
        private final Doc right;

        Append(Doc left, Doc right) {
            this.left = left;
            this.right = right;
        }

        public Doc left() {
            return left;
        }

        public Doc right() {
            return right;
        }

        @Override
        Doc flatten() {
            return left.flatten().append(right.flatten());
        }

        @Override
        boolean hasParams() {
            return left.hasParams() || right.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return left.hasLineSeparators() || right.hasLineSeparators();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new Append(left.bind(name, value), right.bind(name, value));
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new Append(left.bind(bindings), right.bind(bindings));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((left == null) ? 0 : left.hashCode());
            result = prime * result + ((right == null) ? 0 : right.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Append other = (Append) obj;
            if (left == null) {
                if (other.left != null)
                    return false;
            } else if (!left.equals(other.left))
                return false;
            if (right == null) {
                if (other.right != null)
                    return false;
            } else if (!right.equals(other.right))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Append [left=" + left + ", right=" + right + "]";
        }
    }

    /**
     * Represents a choice between a flattened and expanded layout for a
     * single {@link Doc}.
     * <p>
     * We must maintain two invariants in constructing this class:
     *
     * <ul>
     *
     * <li>
     * {@link Doc.Alternatives#left() left} and {@link Doc.Alternatives#right() right}
     * must flatten to the same layout.
     * </li>
     *
     * <li>
     * the first line of {@link Doc.Alternatives#left() left} must not be shorter
     * than the first line of {@link Doc.Alternatives#right() right}
     * </li>
     *
     * </ul>
     * <p>
     * As long as these invariants are preserved recursively, we know that we can
     * always choose the shorter layout for this document by choosing
     * {@link Alternatives#right() right}.
     */
    public static class Alternatives extends Doc {
        private final Doc left;
        private final Doc right;

        Alternatives(Doc left, Doc right) {
            this.left = left;
            this.right = right;
        }

        public Doc left() {
            return left;
        }

        public Doc right() {
            return right;
        }

        @Override
        Doc flatten() {
            return left.flatten();
        }

        @Override
        boolean hasParams() {
            return left.hasParams() || right.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return left.hasLineSeparators() || right.hasLineSeparators();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new Alternatives(left.bind(name, value), right.bind(name, value));
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new Alternatives(left.bind(bindings), right.bind(bindings));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((left == null) ? 0 : left.hashCode());
            result = prime * result + ((right == null) ? 0 : right.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Alternatives other = (Alternatives) obj;
            if (left == null) {
                if (other.left != null)
                    return false;
            } else if (!left.equals(other.left))
                return false;
            if (right == null) {
                if (other.right != null)
                    return false;
            } else if (!right.equals(other.right))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Alternatives [left=" + left + ", right=" + right + "]";
        }
    }

    /**
     * Represents an indented {@link Doc}.
     */
    public static class Indent extends Doc {
        private final int indent;
        private final Doc doc;

        Indent(int indent, Doc doc) {
            this.indent = indent;
            this.doc = doc;
        }

        public int indent() {
            return indent;
        }

        public Doc doc() {
            return doc;
        }

        @Override
        Doc flatten() {
            return doc.flatten().indent(indent);
        }

        @Override
        boolean hasParams() {
            return doc.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return doc.hasLineSeparators();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new Indent(indent, doc.bind(name, value));
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new Indent(indent, doc.bind(bindings));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + indent;
            result = prime * result + ((doc == null) ? 0 : doc.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Indent other = (Indent) obj;
            if (indent != other.indent)
                return false;
            if (doc == null) {
                if (other.doc != null)
                    return false;
            } else if (!doc.equals(other.doc))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Indent [indent=" + indent + ", doc=" + doc + "]";
        }
    }

    /**
     * Represents an aligned {@link Doc}.
     *
     * Sets the indentation for line breaks within its inner {@link Doc} at the current line position.
     */
    public static class Align extends Doc {
        private final Doc doc;

        Align(Doc doc) {
            this.doc = doc;
        }

        public Doc doc() {
            return doc;
        }

        @Override
        Doc flatten() {
            return new Align(doc.flatten());
        }

        @Override
        boolean hasParams() {
            return doc.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return doc.hasLineSeparators();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new Align(doc.bind(name, value));
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new Align(doc.bind(bindings));
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Align align = (Align) o;
            return Objects.equals(doc, align.doc);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(doc);
        }

        @Override
        public String toString() {
            return "Align[" +
                    "doc=" + doc +
                    ']';
        }
    }

    /**
     * Represents a line break which cannot be flattened into a more compact layout.
     */
    public static class Line extends LineOr {
        private static final Line INSTANCE = new Line();

        static Line getInstance() {
            return INSTANCE;
        }

        public Line() {
            // Create a LineOr that flattens to itself
            super();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public String toString() {
            return "Line []";
        }
    }

    /**
     * Represents a line break which can be flattened into an empty document.
     */
    public static class LineOrEmpty extends LineOr {
        private static final LineOrEmpty INSTANCE = new LineOrEmpty();

        static LineOrEmpty getInstance() {
            return INSTANCE;
        }

        public LineOrEmpty() {
            super(empty());
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public String toString() {
            return "LineOrEmpty []";
        }
    }

    /**
     * Represents a line break which can be flattened into a single space character.
     */
    public static class LineOrSpace extends LineOr {
        private static final LineOrSpace INSTANCE = new LineOrSpace();

        static LineOrSpace getInstance() {
            return INSTANCE;
        }

        LineOrSpace() {
            super(text(" "));
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public String toString() {
            return "LineOrSpace []";
        }
    }

    /**
     * Represents a line break which can be flattened into an alternative document
     * {@code altDoc}.
     */
    public static class LineOr extends Doc {
        private final Doc altDoc;

        protected LineOr() {
            this.altDoc = this;
        }

        protected LineOr(Doc altDoc) {
            this.altDoc = altDoc;
        }

        @Override
        Doc flatten() {
            return altDoc != this ? altDoc.flatten() : altDoc;
        }

        @Override
        boolean hasParams() {
            return altDoc != this && altDoc.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return true;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new LineOr(altDoc.bind(name, value));
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new LineOr(altDoc.bind(bindings));
        }

        @Override
        public String toString() {
            return "LineOr [altDoc=" + altDoc + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            /*
             * `this == altDoc` condition is added to the IDE-generated hashCode
             * implementation to deal with the Line subtype
             * which flattens to itself.
             */
            result = prime * result + ((altDoc == null || this == altDoc) ? 0 : altDoc.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LineOr other = (LineOr) obj;
            if (altDoc == null) {
                if (other.altDoc != null)
                    return false;
            } else if (this == altDoc) {
                /*
                 * This condition is added to the IDE-generated equals
                 * implementation to deal with the Line subtype
                 * which flattens to itself.
                 * We have already compared `this` to `obj` so
                 * we know that equals is false if `altDoc` is `this`.
                 */
                return false;
            } else if (!altDoc.equals(other.altDoc))
                return false;
            return true;
        }
    }

    /**
     * Represents a {@link Doc} within which every new line is prefixed by a margin.
     */
    public static class Margin extends Doc {
        private final Doc margin;
        private final Doc doc;

        protected Margin(Doc margin, Doc doc) {
            this.margin = margin;
            this.doc = doc;
        }

        public Doc margin() {
            return margin;
        }

        public Doc doc() {
            return doc;
        }

        @Override
        Doc flatten() {
            return new Margin(margin, doc.flatten());
        }

        @Override
        boolean hasParams() {
            return margin.hasParams() || doc.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return margin.hasLineSeparators() || doc.hasLineSeparators();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new Margin(margin.bind(name, value), doc.bind(name, value));
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new Margin(margin.bind(bindings), doc.bind(bindings));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Margin margin1 = (Margin) o;
            return Objects.equals(margin, margin1.margin) && Objects.equals(doc, margin1.doc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(margin, doc);
        }

        @Override
        public String toString() {
            return "Margin[" +
                    "margin=" + margin +
                    ", doc=" + doc +
                    ']';
        }
    }

    /**
     * Represents an empty {@link Doc}.
     */
    public static class Empty extends Doc {
        private static final Empty INSTANCE = new Empty();

        static Empty getInstance() {
            return INSTANCE;
        }

        Empty() {
        }

        @Override
        public Doc append(Doc other) {
            // By right unit law
            return other;
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        boolean hasParams() {
            return false;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public String toString() {
            return "Empty []";
        }
    }

    /**
     * Represents a {@link Doc} styled with ANSI escape codes.
     */
    public static class Styled extends Doc {
        private final Doc doc;
        private final Styles.StylesOperator[] styles;

        Styled(Doc doc, Styles.StylesOperator[] styles) {
            this.doc = doc;
            this.styles = styles;
        }

        public Doc doc() {
            return doc;
        }

        public Styles.StylesOperator[] styles() {
            return styles;
        }

        @Override
        Doc flatten() {
            return new Styled(doc.flatten(), styles);
        }

        @Override
        boolean hasParams() {
            return doc.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return doc.hasLineSeparators();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new Styled(doc.bind(name, value), styles);
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new Styled(doc.bind(bindings), styles);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Styled styled = (Styled) o;
            return Objects.equals(doc, styled.doc) && Objects.deepEquals(styles, styled.styles);
        }

        @Override
        public int hashCode() {
            return Objects.hash(doc, Arrays.hashCode(styles));
        }

        @Override
        public String toString() {
            return "Styled [" +
                    "doc=" + doc +
                    ", styles=" + Arrays.toString(styles) +
                    ']';
        }
    }

    /**
     * Represents a {@link Doc} that acts as a hyperlink to a given {@link URI}.
     *
     * @see <a href="https://web.archive.org/web/20240525100920/https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda">Hyperlinks (a.k.a. HTML-like anchors) in terminal emulators</a>
     */
    public static class Link extends Doc {
        private final URI uri;
        private final Doc doc;

        public Link(URI uri, Doc doc) {
            this.uri = uri;
            this.doc = doc;
        }

        public URI uri() {
            return this.uri;
        }

        public Doc doc() {
           return this.doc;
        }

        @Override
        Doc flatten() {
            return new Link(uri, doc.flatten());
        }

        @Override
        boolean hasParams() {
            return doc.hasParams();
        }

        @Override
        boolean hasLineSeparators() {
            return doc.hasLineSeparators();
        }

        @Override
        public Doc bind(String name, Doc value) {
            return new Link(uri, doc.bind(name, value));
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return new Link(uri, doc.bind(bindings));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Link link = (Link) o;
            return Objects.equals(uri, link.uri) && Objects.equals(doc, link.doc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, doc);
        }

        @Override
        public String toString() {
            return "Link[" +
                    "uri=" + uri +
                    ", doc=" + doc +
                    ']';
        }
    }

    /**
     * Represents an ANSI escape code sequence.
     */
    public static class Escape extends Doc {
        private final Styles.StylesOperator[] styles;

        public Escape(Styles.StylesOperator[] styles) {
            this.styles = styles;
        }

        public Styles.StylesOperator[] styles() {
            return this.styles;
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        boolean hasParams() {
            return false;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Escape escape = (Escape) o;
            return Objects.deepEquals(styles, escape.styles);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(styles);
        }

        @Override
        public String toString() {
            return "Escape [" +
                    "styles=" + Arrays.toString(styles) +
                    ']';
        }
    }

    /**
     * Represents the end of a {@link Doc} that is {@link Doc#styled(Styles.StylesOperator...) styled} with an ANSI escape code sequence.
     */
    public static class Reset extends Doc {
        private static final Reset INSTANCE = new Reset();

        static Reset getInstance() {
            return INSTANCE;
        }

        Reset() {
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        boolean hasParams() {
            return false;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public String toString() {
            return "Reset []";
        }
    }

    /**
     * Represents the ANSI escape code sequence for opening a hyperlink.
     *
     * @see <a href="https://web.archive.org/web/20240525100920/https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda">Hyperlinks (a.k.a. HTML-like anchors) in terminal emulators</a>
     */
    public static class OpenLink extends Doc {
        private final URI uri;

        public OpenLink(URI uri) {
            this.uri = uri;
        }

        public URI uri() {
            return this.uri;
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        boolean hasParams() {
            return false;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OpenLink link = (OpenLink) o;
            return Objects.equals(uri, link.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(uri);
        }

        @Override
        public String toString() {
            return "OpenLink[" +
                    "uri=" + uri +
                    ']';
        }
    }

    /**
     * Represents the ANSI escape code sequence for closing a hyperlink.
     *
     * @see <a href="https://web.archive.org/web/20240525100920/https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda">Hyperlinks (a.k.a. HTML-like anchors) in terminal emulators</a>
     */
    public static class CloseLink extends Doc {
        private static final CloseLink INSTANCE = new CloseLink();

        static CloseLink getInstance() {
            return INSTANCE;
        }

        CloseLink() {
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        boolean hasParams() {
            return false;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            return this;
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            return this;
        }

        @Override
        public String toString() {
            return "CloseLink []";
        }
    }

    /**
     * Represents a placeholder for a {@link Doc} that will be provided as a parameter.
     */
    public static class Param extends Doc {
        private final String name;
        private final boolean flattened;

        private Param(String name, boolean flattened) {
            this.name = name;
            this.flattened = flattened;
        }

        Param(String name) {
            this(name, false);
        }

        public String name() {
            return this.name;
        }

        @Override
        Doc flatten() {
            return new Param(name, true);
        }

        @Override
        boolean hasParams() {
            return true;
        }

        @Override
        boolean hasLineSeparators() {
            return false;
        }

        @Override
        public Doc bind(String name, Doc value) {
            if (this.name.equals(name)) {
                return flattened ? value.flatten() : value;
            } else {
                return this;
            }
        }

        @Override
        public Doc bind(Map<String, Doc> bindings) {
            Doc value = bindings.getOrDefault(this.name, this);
            if (value != this) {
                return flattened ? value.flatten() : value;
            } else {
                return this;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Param param = (Param) o;
            return flattened == param.flattened && Objects.equals(name, param.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, flattened);
        }

        @Override
        public String toString() {
            return "Param[" +
                    "name='" + name + '\'' +
                    ", flattened=" + flattened +
                    ']';
        }
    }

    /**
     * Construct a {@link Doc} from the {@code text}.
     *
     * @param text the input String.
     * @return a {@link Doc Doc} representing that {@link String}.
     */
    public static Doc text(String text) {
        // By empty text equivalency law
        if (text.isEmpty()) { return empty(); }
        return new Text(text);
    }

    /**
     * Construct a {@link Doc} which attempts to wrap the input {@code text}
     * to fit within the preferred rendering width.
     *
     * @param text the input String.
     * @return a {@link Doc Doc} representing that {@link String}.
     */
    public static Doc wrapText(String text) {
        // By empty text equivalency law
        if (text.isEmpty()) { return empty(); }
        return new WrapText(text, 0);
    }

    /**
     * Construct a {@link Doc Doc} representing two alternative layouts for a document.
     *
     * @param left  the flattened layout for the document.
     * @param right the expanded layout for the document.
     * @return the aggregate document representing a choice between the two layouts.
     */
    static Doc alternatives(Doc left, Doc right) {
        return new Alternatives(left, right);
    }

    /**
     * Indent the input {@link Doc} by {@code indent} spaces.
     *
     * @param indent the number of spaces of indent to apply.
     * @param doc    the input document
     * @return the indented document.
     */
    public static Doc indent(int indent, Doc doc) {
        // By zero indent equivalency law
        if (indent == 0) { return doc; }
        return new Indent(indent, doc);
    }

    /**
     * Align subsequent lines of the current {@link Doc} to the current position in the line.
     *
     * @param doc the input document
     * @return the aligned document.
     */
    public static Doc align(Doc doc) {
        return new Align(doc);
    }

    /**
     * Apply the margin document {@code margin} to the current {@link Doc}, emitting the
     * margin at the start of every new line from the start of this document until the
     * end of the document.
     * <p>
     * Note that line separators are forbidden inside the margin document.
     * <p>
     * This is because each line separator causes the margin document to be produced.
     * <p>
     * If the margin document in turn contained a line separator, rendering would never terminate.
     *
     * @param margin the margin document to apply at the start of every line.
     * @param doc the input document.
     * @return a document which prefixes every new line with the {@code margin} document.
     * @throws IllegalArgumentException if the margin document contains line separators.
     */
    public static Doc margin(Doc margin, Doc doc) {
        // By empty margin equivalency law
        if (margin instanceof Empty) {
            return doc;
        }
        if (margin.hasLineSeparators()) {
            throw new IllegalArgumentException("The margin document contains line separators.");
        }
        // By nested margin concat law
        if (doc instanceof Margin) {
            Margin marginDoc = (Margin) doc;
            return new Margin(margin.append(marginDoc.margin()), marginDoc.doc());
        } else {
            return new Margin(margin, doc);
        }
    }

    /**
     * Creates a {@link Doc} representing a line break which cannot be flattened.
     *
     * @return a {@link Doc} representing a line break which cannot be flattened.
     */
    public static Doc line() {
        return Line.getInstance();
    }

    /**
     * Creates a {@link Doc} representing a line break which may be flattened into an empty document.
     *
     * @return a {@link Doc} representing a line break which may be flattened into an empty document.
     */
    public static Doc lineOrEmpty() {
        return LineOrEmpty.getInstance();
    }

    /**
     * Creates a {@link Doc} representing a line break which may be flattened into a single space character.
     *
     * @return a {@link Doc} representing a line break which may be flattened into a single space character.
     */
    public static Doc lineOrSpace() {
        return LineOrSpace.getInstance();
    }

    /**
     * Creates an empty {@link Doc}.
     *
     * @return an empty {@link Doc}.
     */
    public static Doc empty() {
        return Empty.getInstance();
    }

    /**
     * Creates a {@link Doc} representing a line break which may be flattened into
     * an alternative document {@code altDoc}.
     *
     * @param altDoc the alternative document to use if the line break is flattened.
     * @return a {@link Doc} representing a line break which may be flattened into
     * an alternative document {@code altDoc}.
     */
    public static Doc lineOr(Doc altDoc) {
        return new LineOr(altDoc);
    }

    /**
     * Creates a {@link Doc} representing a line break which may be flattened into
     * the alternative text {@code altText}.
     *
     * @param altText the alternative text to use if the line break is flattened.
     * @return a {@link Doc} representing a line break which may be flattened into
     * the alternative text {@code altText}.
     */
    public static Doc lineOr(String altText) {
        return new LineOr(text(altText));
    }

    /**
     * Styles the input {@link Doc} using the styles provided via {@code styles}.
     *
     * @param doc    the input document.
     * @param styles the styles to use to decorate the input {@code doc}.
     * @return a {@link Doc} decorated with the ANSI styles provided.
     * @see Styles
     * @see com.opencastsoftware.prettier4j.ansi.Color Color
     */
    public static Doc styled(Doc doc, Styles.StylesOperator... styles) {
        // By empty styles equivalency law
        if (styles.length == 0) { return doc; }
        return new Styled(doc, styles);
    }

    /**
     * Converts the input {@link Doc} into a hyperlink to the {@code uri} provided.
     *
     * @param doc the input document.
     * @param uri the {@link URI} to link to.
     * @return a {@link Doc} that acts as a hyperlink to the {@code uri}.
     * @implNote This implementation does not currently handle nested hyperlinks, so
     * all hyperlinks will be closed after the end of the first nested hyperlink.
     * @see <a href="https://web.archive.org/web/20240525100920/https://gist.github.com/egmontkob/eb114294efbcd5adb1944c9f3cb5feda">Hyperlinks (a.k.a. HTML-like anchors) in terminal emulators</a>
     */
    public static Doc link(Doc doc, URI uri) {
        return new Link(uri, doc);
    }

    /**
     * Creates a {@link Doc} which acts as a placeholder for an argument {@link Doc} that will be provided
     * by {@link Doc#bind(String, Doc) binding} parameters prior to {@link Doc#render(int) render}ing.
     *
     * @param name the name of the parameter.
     * @return a parameter {@link Doc}.
     */
    public static Doc param(String name) {
        return new Param(name);
    }

    /**
     * Reduce a collection of documents using the binary operator {@code fn},
     * returning an empty document if the collection is empty.
     *
     * @param documents the collection of documents.
     * @param fn        the binary operator for combining documents.
     * @return a document built by reducing the {@code documents} using the operator {@code fn}.
     */
    public static Doc fold(Collection<Doc> documents, BinaryOperator<Doc> fn) {
        return fold(documents.stream(), fn);
    }

    /**
     * Reduce a stream of documents using the binary operator {@code fn}, returning
     * an empty document if the stream is empty.
     *
     * @param documents the stream of documents.
     * @param fn        the binary operator for combining documents.
     * @return a document built by reducing the {@code documents} using the operator {@code fn}.
     */
    public static Doc fold(Stream<Doc> documents, BinaryOperator<Doc> fn) {
        return documents.reduce(fn).orElse(Doc.empty());
    }

    /**
     * Intersperse a {@code separator} document in between the elements of a
     * collection of documents.
     *
     * @param separator the separator document.
     * @param documents the collection of documents.
     * @return a document containing the concatenation of {@code documents}
     * separated by the {@code separator}.
     */
    public static Doc intersperse(Doc separator, Collection<Doc> documents) {
        return intersperse(separator, documents.stream());
    }

    /**
     * Intersperse a {@code separator} document in between the elements of a stream
     * of documents.
     *
     * @param separator the separator document.
     * @param documents the stream of documents.
     * @return a document containing the concatenation of {@code documents}
     * separated by the {@code separator}.
     */
    public static Doc intersperse(Doc separator, Stream<Doc> documents) {
        return Doc.fold(documents, (left, right) -> {
            return left.append(separator).append(right);
        });
    }

    /**
     * Creates a {@link Doc} which represents a group that can be flattened into a more compact layout.
     *
     * @param doc the document which is declared as a group which may be flattened.
     * @return a {@link Doc} which represents a group that can be flattened into a more compact layout.
     */
    public static Doc group(Doc doc) {
        return alternatives(doc.flatten(), doc);
    }

    /**
     * A render queue entry which keeps track of the indentation and margin of a given {@link Doc}.
     */
    static final class Entry {
        private final int indent;
        private final Doc margin;
        private final Doc doc;

        private Entry(int indent, Doc margin, Doc doc) {
            this.indent = indent;
            this.margin = margin;
            this.doc = doc;
        }

        int indent() {
            return indent;
        }

        Doc margin() {
            return margin;
        }

        Doc doc() {
            return doc;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return indent == entry.indent && Objects.equals(margin, entry.margin) && Objects.equals(doc, entry.doc);
        }

        @Override
        public int hashCode() {
            return Objects.hash(indent, margin, doc);
        }

        @Override
        public String toString() {
            return "Entry[" +
                    "indent=" + indent +
                    ", margin=" + margin +
                    ", doc=" + doc +
                    ']';
        }
    }

    /**
     * Construct a render queue entry.
     *
     * @param indent the indentation of the {@code doc}.
     * @param margin the margin document for the {@code doc}.
     * @param doc the document to be rendered.
     * @return a render queue entry.
     */
    private static Entry entry(int indent, Doc margin, Doc doc) {
        return new Entry(indent, margin, doc);
    }

    /**
     * Choose between two layouts at the current rendering width, indentation level
     * and line position, choosing the {@code left} document if it fits and the
     * {@code right} document otherwise.
     *
     * @param left     the preferred compact layout.
     * @param right    the expanded layout.
     * @param options  the options to use for rendering.
     * @param margin   the current margin.
     * @param indent   the current indentation level.
     * @param position the position in the current line.
     * @return the entries of the chosen layout.
     */
    private static Queue<Entry> chooseLayout(Doc left, Doc right, RenderOptions options, Doc margin, int indent, int position) {
        Deque<Entry> inQueue = new ArrayDeque<>();
        Queue<Entry> outQueue = new ArrayDeque<>();

        inQueue.add(entry(indent, margin, left));

        int leftPosition = position;

        while (!inQueue.isEmpty()) {
            Entry topEntry = inQueue.removeFirst();
            leftPosition = layoutEntry(options, inQueue, outQueue, topEntry, leftPosition);

            int remaining = options.lineWidth() - leftPosition;

            if (remaining < 0) {
                // The new entry doesn't fit, so use the right layout
                return layout(right, options, margin, indent, position);
            }
        }

        return outQueue;
    }

    /**
     * Traverses the input {@code doc} recursively, eliminating all nodes except for
     * {@link Text}, {@link Escape}, {@link Reset}, {@link OpenLink}, {@link CloseLink}
     * and subtypes of {@link LineOr}, and produces a queue of entries to be rendered.
     *
     * @param doc      the document to be rendered.
     * @param options  the options to use for rendering.
     * @param margin   the current margin.
     * @param indent   the current indentation level.
     * @param position the current position in the line.
     * @return a queue of entries to be rendered.
     */
    private static Queue<Entry> layout(Doc doc, RenderOptions options, Doc margin, int indent, int position) {
        Deque<Entry> inQueue = new ArrayDeque<>();
        Queue<Entry> outQueue = new ArrayDeque<>();

        inQueue.add(entry(indent, margin, doc));

        while (!inQueue.isEmpty()) {
            Entry topEntry = inQueue.removeFirst();
            position = layoutEntry(options, inQueue, outQueue, topEntry, position);
        }

        return outQueue;
    }

    /**
     * Attempts to wrap a line of the {@code wrapDoc} text so that it fits within the rendering width defined in the {@code options}.
     * <p>
     * If the very first word found on any line of the {@code wrapDoc} is longer than the current rendering width, then it
     * will be emitted anyway in order to make progress in laying out the document.
     * <p>
     * We do not attempt to lay out more than a single line on each invocation of this method.
     * <p>
     * This is because we do not know the length of the current margin document in advance: it is an arbitrary {@link Doc} whose only
     * constraint is that it must not contain any line separators.
     *
     * @param options the options to use for rendering.
     * @param inQueue the layout input queue.
     * @param entry the current queue entry which contained the {@code wrapDoc}.
     * @param wrapDoc the {@link WrapText} document to be rendered.
     * @param position the current position in the line.
     * @return the new position on the line after consuming the first line of the {@code wrapDoc}.
     */
    private static int wrapText(RenderOptions options, Deque<Entry> inQueue, Entry entry, WrapText wrapDoc, int position) {
        int entryIndent = entry.indent();
        Doc entryMargin = entry.margin();

        String wrapText = wrapDoc.text();
        int textLength = wrapText.length();
        if (textLength == 0) { return position; }

        StringBuilder wrapped = new StringBuilder(options.lineWidth());

        int textOffset = wrapDoc.offset();
        int wordStart = -1;
        for (; textOffset < textLength; textOffset++) {
            char currentChar = wrapText.charAt(textOffset);

            boolean isInWord = wordStart >= 0;
            boolean isWhitespace = Character.isWhitespace(currentChar);
            boolean isStartOfWord = !isWhitespace && !isInWord;

            if (isStartOfWord) {
                wordStart = textOffset;
                isInWord = true;
            }

            boolean isLastChar = textOffset == textLength - 1;
            boolean isEndOfWord = (isWhitespace || isLastChar) && isInWord;
            boolean isFirstWord = wrapped.length() == 0;

            if (isEndOfWord) {
                int precedingSpaces = isFirstWord ? 0 : 1;
                int wordEnd = isLastChar ? textLength : textOffset;
                int wordLength = wordEnd - wordStart + precedingSpaces;
                int remaining = options.lineWidth() - position;
                if (remaining < wordLength) {
                    if (isFirstWord) {
                        // It's a really long word, so send it out to make progress
                        wrapped.append(wrapText, wordStart, wordEnd);
                        position += wordLength;
                        wordStart = -1;
                    }
                    if (!isLastChar) break;
                } else {
                    if (!isFirstWord) { wrapped.append(' '); }
                    wrapped.append(wrapText, wordStart, wordEnd);
                    position += wordLength;
                    wordStart = -1;
                }
            }
        }

        // Skip trailing whitespace
        while (textOffset < textLength && Character.isWhitespace(wrapText.charAt(textOffset))) {
            textOffset++;
        }

        int restOffset = wordStart > 0 ? wordStart : textOffset;
        int remainingChars = textLength - restOffset;
        if (remainingChars > 0) {
            // Send out remainder prefixed by line separator
            Doc remainingDoc = wrapDoc.atOffset(restOffset);
            inQueue.addFirst(entry(entryIndent, entryMargin, remainingDoc));
            inQueue.addFirst(entry(entryIndent, entryMargin, line()));
        }

        // Send out the wrapped line
        inQueue.addFirst(entry(entryIndent, entryMargin, text(wrapped.toString())));

        return position;
    }

    /**
     * Select a layout for the {@code topEntry} render queue entry, eliminating all nodes except for
     * {@link Text}, {@link Escape}, {@link Reset}, {@link OpenLink}, {@link CloseLink}
     * and subtypes of {@link LineOr}, and produces a queue of entries to be rendered.
     *
     * @param options the options to use for rendering.
     * @param inQueue the layout input queue.
     * @param outQueue the rendering output queue.
     * @param topEntry the top entry in the rendering queue.
     * @param position the current position in the line.
     * @return the new position on the line after choosing a layout for the {@code topEntry}.
     */
    private static int layoutEntry(RenderOptions options, Deque<Entry> inQueue, Queue<Entry> outQueue, Entry topEntry, int position) {
        int entryIndent = topEntry.indent();
        Doc entryMargin = topEntry.margin();
        Doc entryDoc = topEntry.doc();

        if (entryDoc instanceof Append) {
            // Eliminate Append
            Append appendDoc = (Append) entryDoc;
            // Note reverse order
            inQueue.addFirst(entry(entryIndent, entryMargin, appendDoc.right()));
            inQueue.addFirst(entry(entryIndent, entryMargin, appendDoc.left()));
        } else if (entryDoc instanceof Styled) {
            // Eliminate Styled
            Styled styledDoc = (Styled) entryDoc;
            if (options.emitAnsiEscapes()) {
                // Note reverse order
                inQueue.addFirst(entry(entryIndent, entryMargin, Reset.getInstance()));
                inQueue.addFirst(entry(entryIndent, entryMargin, styledDoc.doc()));
                inQueue.addFirst(entry(entryIndent, entryMargin, new Escape(styledDoc.styles())));
            } else {
                // Ignore styles and emit the underlying Doc
                inQueue.addFirst(entry(entryIndent, entryMargin, styledDoc.doc()));
            }
        } else if (entryDoc instanceof Link) {
            // Eliminate Link
            Link linkDoc = (Link) entryDoc;
            if (options.emitAnsiEscapes()) {
                // Note reverse order
                inQueue.addFirst(entry(entryIndent, entryMargin, CloseLink.getInstance()));
                inQueue.addFirst(entry(entryIndent, entryMargin, linkDoc.doc()));
                inQueue.addFirst(entry(entryIndent, entryMargin, new OpenLink(linkDoc.uri())));
            } else {
                // Ignore link and emit the underlying Doc
                inQueue.addFirst(entry(entryIndent, entryMargin, linkDoc.doc()));
            }
        } else if (entryDoc instanceof Indent) {
            // Eliminate Indent
            Indent indentDoc = (Indent) entryDoc;
            int newIndent = entryIndent + indentDoc.indent();
            inQueue.addFirst(entry(newIndent, entryMargin, indentDoc.doc()));
        } else if (entryDoc instanceof Align) {
            // Eliminate Align
            Align alignDoc = (Align) entryDoc;
            int newIndent = Math.max(0, position - entryIndent);
            inQueue.addFirst(entry(newIndent, entryMargin, alignDoc.doc()));
        } else if (entryDoc instanceof Margin) {
            // Eliminate Margin
            Margin marginDoc = (Margin) entryDoc;
            // Note reverse order
            Doc newMargin = entryMargin.append(marginDoc.margin());
            inQueue.addFirst(entry(entryIndent, newMargin, marginDoc.doc()));
        } else if (entryDoc instanceof Alternatives) {
            // Eliminate Alternatives
            Alternatives altDoc = (Alternatives) entryDoc;
            // These entries are already laid out
            outQueue.addAll(chooseLayout(
                altDoc.left(), altDoc.right(),
                options, entryMargin, entryIndent, position));
        } else if (entryDoc instanceof WrapText) {
            // Eliminate WrapText
            WrapText wrapDoc = (WrapText) entryDoc;
            position = wrapText(options, inQueue, topEntry, wrapDoc, position);
        } else if (entryDoc instanceof Text) {
            Text textDoc = (Text) entryDoc;
            // Keep track of line length
            position += textDoc.text().length();
            outQueue.add(topEntry);
        } else if (entryDoc instanceof LineOr) {
            // Reset line length
            position = entryIndent;
            // Note reverse order
            if (entryIndent > 0) {
                // Send out the indent spaces
                String indentSpaces = Indents.get(entryIndent);
                inQueue.addFirst(entry(entryIndent, entryMargin, text(indentSpaces)));
            }
            // Send out the current margin
            inQueue.addFirst(entry(entryIndent, entryMargin, entryMargin));
            outQueue.add(topEntry);
        } else if (entryDoc instanceof OpenLink) {
            outQueue.add(topEntry);
        } else if (entryDoc instanceof CloseLink) {
            outQueue.add(topEntry);
        } else if (entryDoc instanceof Escape) {
            outQueue.add(topEntry);
        } else if (entryDoc instanceof Reset) {
            outQueue.add(topEntry);
        }
        // Eliminate Empty

        return position;
    }

    /**
     * Flush the rendering queue {@code outQueue} to the {@link Appendable} {@code output}, taking care of
     * transitioning between different ANSI display attributes according to each {@link Escape} and {@link Reset} token
     * and opening and closing hyperlinks according to each {@link OpenLink} and {@link CloseLink} token.
     *
     * @param outQueue the rendering output queue.
     * @param attrsStack a stack of display attributes that are in effect.
     * @param output the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    private static void flushToOutput(Queue<Entry> outQueue, AttrsStack attrsStack, Appendable output) throws IOException {
        while (!outQueue.isEmpty()) {
            Entry entry = outQueue.remove();
            Doc entryDoc = entry.doc();
            // layout reduces Doc to Text, LineOr, OpenLink, CloseLink, Escape and Reset
            if (entryDoc instanceof Text) {
                Text textDoc = (Text) entryDoc;
                output.append(textDoc.text());
            } else if (entryDoc instanceof LineOr) {
                output.append(System.lineSeparator());
            } else if (entryDoc instanceof OpenLink) {
                OpenLink openLinkDoc = (OpenLink) entryDoc;
                output.append(AnsiConstants.OPEN_LINK);
                output.append(openLinkDoc.uri().toASCIIString());
                output.append(AnsiConstants.ST);
            } else if (entryDoc instanceof CloseLink) {
                output.append(AnsiConstants.CLOSE_LINK);
            } else if (entryDoc instanceof Reset) {
                long resetAttrs = attrsStack.popLast();
                long prevAttrs = attrsStack.peekLast();
                Attrs.transition(output, resetAttrs, prevAttrs);
            } else if (entryDoc instanceof Escape) {
                Escape escapeDoc = (Escape) entryDoc;
                long prevAttrs = attrsStack.peekLast();
                if (prevAttrs == Attrs.NULL) { prevAttrs = Attrs.EMPTY; }
                long newAttrs = Attrs.withStyles(prevAttrs, escapeDoc.styles());
                attrsStack.pushLast(newAttrs);
                Attrs.transition(output, prevAttrs, newAttrs);
            }
        }
    }

    /**
     * Renders the input {@link Doc} into an {@link Appendable}, attempting to lay out the document
     * according to the rendering {@code options}.
     *
     * @param doc     the document to be rendered.
     * @param options the options to use for rendering.
     * @param output  the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    public static void render(Doc doc, RenderOptions options, Appendable output) throws IOException {
        if (doc.hasParams()) { throw new IllegalStateException("This Doc contains unbound parameters"); }

        int position = 0;
        Deque<Entry> inQueue = new ArrayDeque<>();
        Queue<Entry> outQueue = new ArrayDeque<>();
        AttrsStack attrsStack = new AttrsStack();

        inQueue.add(entry(0, empty(), doc));

        while (!inQueue.isEmpty()) {
            Entry topEntry = inQueue.removeFirst();
            position = layoutEntry(options, inQueue, outQueue, topEntry, position);
            flushToOutput(outQueue, attrsStack, output);
        }
    }

    /**
     * Renders the input {@link Doc} into an {@link Appendable}, attempting to lay out the document
     * according to the {@link RenderOptions#defaults() default} rendering options.
     *
     * @param doc    the document to be rendered.
     * @param output the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    public static void render(Doc doc, Appendable output) throws IOException {
        render(doc, RenderOptions.defaults(), output);
    }

    /**
     * Renders the input {@link Doc} into a {@link String}, attempting to lay out the document
     * according to the rendering {@code options}.
     *
     * @param doc     the document to be rendered.
     * @param options the options to use for rendering.
     * @return the document laid out as a {@link String}.
     */
    public static String render(Doc doc, RenderOptions options) {
        StringBuilder output = new StringBuilder();

        try {
            render(doc, options, output);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

        return output.toString();
    }

    /**
     * Renders the input {@link Doc} into a {@link String}, attempting to lay out the document
     * according to the {@link RenderOptions#defaults() default} rendering options.
     *
     * @param doc the document to be rendered.
     * @return the document laid out as a {@link String}.
     */
    public static String render(Doc doc) {
        return render(doc, RenderOptions.defaults());
    }
}
