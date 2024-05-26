/*
 * SPDX-FileCopyrightText:  © 2022-2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j;

import com.opencastsoftware.prettier4j.ansi.Attrs;
import com.opencastsoftware.prettier4j.ansi.AttrsStack;
import com.opencastsoftware.prettier4j.ansi.Styles;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Implements the algorithm described in Philip Wadler's "A prettier printer", a
 * pretty printing algorithm for laying out hierarchical documents as text.
 * <p>
 * To construct a document, see the static methods of
 * {@link com.opencastsoftware.prettier4j.Doc Doc}, especially
 * {@link com.opencastsoftware.prettier4j.Doc#text(String) text},
 * {@link com.opencastsoftware.prettier4j.Doc#empty() empty},
 * {@link com.opencastsoftware.prettier4j.Doc#line() line} and its related
 * methods.
 * <p>
 * To concatenate documents, see
 * {@link com.opencastsoftware.prettier4j.Doc#append(Doc) append} and its
 * related instance methods.
 * <p>
 * To declare groups of content which should be collapsed onto one line if
 * possible, see the static method
 * {@link com.opencastsoftware.prettier4j.Doc#group(Doc) group}.
 * <p>
 * To style a {@link Doc} with ANSI escape codes, see the instance method
 * {@link Doc#styled(Styles.StylesOperator...)} or static method
 * {@link Doc#styled(Doc, Styles.StylesOperator...)}.
 * <p>
 * To render documents to an {@link Appendable} output, see the instance method
 * {@link Doc#render(int, Appendable)} or static method
 * {@link Doc#render(int, Doc, Appendable)}.
 * <p>
 * To render documents to {@link String}, see the instance method
 * {@link Doc#render(int) render} or static method
 * {@link Doc#render(int, Doc) render}.
 *
 * @see <a href=
 *      "https://homepages.inf.ed.ac.uk/wadler/papers/prettier/prettier.pdf">A
 *      prettier printer</a>
 */
public abstract class Doc {
    /**
     * Returns a flattened layout for the current
     * {@link com.opencastsoftware.prettier4j.Doc Doc}.
     *
     * @return the flattened document.
     */
    abstract Doc flatten();

    /**
     * Append the {@code other} {@link com.opencastsoftware.prettier4j.Doc Doc} to
     * this one.
     *
     * @param other the other document.
     * @return the concatenated {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public Doc append(Doc other) {
        return new Append(this, other);
    }

    /**
     * Append the {@code other} {@link com.opencastsoftware.prettier4j.Doc Doc} to
     * this one, separated by a space character.
     *
     * @param other the other document.
     * @return the concatenated {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public Doc appendSpace(Doc other) {
        return this
                .append(text(" "))
                .append(other);
    }

    /**
     * Append the {@code other} {@link com.opencastsoftware.prettier4j.Doc Doc} to
     * this one, separated by a line break which cannot be flattened.
     *
     * @param other the other document.
     * @return the concatenated {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public Doc appendLine(Doc other) {
        return this
                .append(line())
                .append(other);
    }

    /**
     * Append the {@code other} {@link com.opencastsoftware.prettier4j.Doc Doc} to
     * this one, separated by a line break which may be flattened into a space
     * character.
     *
     * @param other the other document.
     * @return the concatenated {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public Doc appendLineOrSpace(Doc other) {
        return this
                .append(lineOrSpace())
                .append(other);
    }

    /**
     * Append the {@code other} {@link com.opencastsoftware.prettier4j.Doc Doc} to
     * this one, separated by a line break which may be flattened into an empty
     * document.
     *
     * @param other the other document.
     * @return the concatenated {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public Doc appendLineOrEmpty(Doc other) {
        return this
                .append(lineOrEmpty())
                .append(other);
    }

    /**
     * Append the {@code other} {@link com.opencastsoftware.prettier4j.Doc Doc} to
     * this one, separated by a line break which may be flattened into the
     * {@code altText} String.
     *
     * @param altText the alternative text to display if the line break is
     *                flattened.
     * @param other   the other document.
     * @return the concatenated {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public Doc appendLineOr(String altText, Doc other) {
        return this.appendLineOr(text(altText), other);
    }

    /**
     * Append the {@code other} {@link com.opencastsoftware.prettier4j.Doc Doc} to
     * this one, separated by a line break which may be flattened into the
     * {@code altDoc} document.
     *
     * @param altDoc the alternative document to display if the line break is
     *               flattened.
     * @param other  the other document.
     * @return the concatenated {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public Doc appendLineOr(Doc altDoc, Doc other) {
        return this
                .append(lineOr(altDoc))
                .append(other);
    }

    /**
     * Indent the current {@link com.opencastsoftware.prettier4j.Doc Doc} by
     * {@code indent} spaces.
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
        return group(
                left
                        .append(lineDoc.append(this).indent(indent))
                        .append(lineDoc.append(right)));
    }

    /**
     * Styles the current {@link com.opencastsoftware.prettier4j.Doc Doc} using the styles
     * provided via {@code styles}.
     *
     * @param styles the styles to use to decorate the input {@code doc}.
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} decorated with the ANSI styles provided.
     * @see Styles
     * @see com.opencastsoftware.prettier4j.ansi.Color Color
     */
    public final Doc styled(Styles.StylesOperator...styles) {
        return styled(this, styles);
    }

    /**
     * Renders the current {@link com.opencastsoftware.prettier4j.Doc Doc} into a
     * {@link java.lang.String String}, aiming to lay out the document with at most
     * {@code width} characters on each line.
     *
     * @param width the preferred maximum rendering width.
     * @return the document laid out as a {@link java.lang.String String}.
     */
    public String render(int width) {
        return render(width, this);
    }

    /**
     * Renders the current {@link com.opencastsoftware.prettier4j.Doc Doc} into an
     * {@link java.lang.Appendable Appendable}, aiming to lay out the document with at most
     * {@code width} characters on each line.
     *
     * @param width the preferred maximum rendering width.
     * @param output the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    public void render(int width, Appendable output) throws IOException {
        render(width, this, output);
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
        Doc flatten() {
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
     * Represents the concatenation of two
     * {@link com.opencastsoftware.prettier4j.Doc Doc}s.
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
     * single {@link com.opencastsoftware.prettier4j.Doc Doc}.
     * <p>
     * We must maintain two invariants in constructing this class:
     *
     * <ul>
     * <li>{@link com.opencastsoftware.prettier4j.Doc.Alternatives#left left} and
     * {@link com.opencastsoftware.prettier4j.Doc.Alternatives#right right} must
     * flatten to the same layout</li>
     * <li>the first line of
     * {@link com.opencastsoftware.prettier4j.Doc.Alternatives#left left} must not
     * be shorter than the first line of
     * {@link com.opencastsoftware.prettier4j.Doc.Alternatives#right right}</li>
     * </ul>
     *
     * As long as these invariants are preserved recursively, we know that we can
     * always choose the shorter layout for this document by choosing
     * {@link com.opencastsoftware.prettier4j.Doc.Alternatives#right right}.
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
     * Represents an indented {@link com.opencastsoftware.prettier4j.Doc Doc}.
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
        public String toString() {
            return "Line []";
        }
    }

    /** Represents a line break which can be flattened into an empty document. */
    public static class LineOrEmpty extends LineOr {
        private static final LineOrEmpty INSTANCE = new LineOrEmpty();

        static LineOrEmpty getInstance() {
            return INSTANCE;
        }

        public LineOrEmpty() {
            super(empty());
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
        public String toString() {
            return "LineOrSpace []";
        }
    }

    /**
     * Represents a line break which can be flattened into an alternative document
     * {@link com.opencastsoftware.prettier4j.Doc.LineOr#altDoc altDoc}.
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
            return altDoc;
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
     * Represents an empty {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public static class Empty extends Doc {
        private static final Empty INSTANCE = new Empty();

        static Empty getInstance() {
            return INSTANCE;
        }

        Empty() {
        }

        @Override
        Doc flatten() {
            return this;
        }

        @Override
        public String toString() {
            return "Empty []";
        }
    }

    /**
     * Represents a {@link com.opencastsoftware.prettier4j.Doc Doc} styled with ANSI escape codes.
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
     * Represents the end of scope of some ANSI text styling.
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
        public String toString() {
            return "Reset []";
        }
    }

    /**
     * Construct a {@link com.opencastsoftware.prettier4j.Doc Doc} from the
     * {@code text}.
     *
     * @param text the input String.
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} representing that
     *         String.
     */
    public static Doc text(String text) {
        return new Text(text);
    }

    /**
     * Construct a {@link com.opencastsoftware.prettier4j.Doc Doc} representing two
     * alternative layouts for a document.
     *
     * @param left  the flattened layout for the document.
     * @param right the expanded layout for the document.
     * @return the aggregate document representing a choice between the two layouts.
     */
    static Doc alternatives(Doc left, Doc right) {
        return new Alternatives(left, right);
    }

    /**
     * Indent the input {@link com.opencastsoftware.prettier4j.Doc Doc} by
     * {@code indent} spaces.
     *
     * @param indent the number of spaces of indent to apply.
     * @param doc    the input document
     * @return the indented document.
     */
    public static Doc indent(int indent, Doc doc) {
        return new Indent(indent, doc);
    }

    /**
     * Creates a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a
     * line break which cannot be flattened.
     *
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a line
     *         break which cannot be flattened.
     */
    public static Doc line() {
        return Line.getInstance();
    }

    /**
     * Creates a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a
     * line break which may be flattened into an empty document.
     *
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a line
     *         break which may be flattened into an empty document.
     */
    public static Doc lineOrEmpty() {
        return LineOrEmpty.getInstance();
    }

    /**
     * Creates a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a
     * line break which may be flattened into a single space character.
     *
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a line
     *         break which may be flattened into a single space character.
     */
    public static Doc lineOrSpace() {
        return LineOrSpace.getInstance();
    }

    /**
     * Creates an empty {@link com.opencastsoftware.prettier4j.Doc Doc}.
     *
     * @return an empty {@link com.opencastsoftware.prettier4j.Doc Doc}.
     */
    public static Doc empty() {
        return Empty.getInstance();
    }

    /**
     * Creates a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a line
     * break which may be flattened into an alternative document {@code altDoc}.
     *
     * @param altDoc the alternative document to use if the line break is flattened.
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a line
     *         break which may be flattened into an alternative document
     *         {@code altDoc}.
     */
    public static Doc lineOr(Doc altDoc) {
        return new LineOr(altDoc);
    }

    /**
     * Creates a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a line
     * break which may be flattened into the alternative text {@code altText}.
     *
     * @param altText the alternative text to use if the line break is flattened.
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} representing a line
     *         break which may be flattened into the alternative text
     *         {@code altText}.
     */
    public static Doc lineOr(String altText) {
        return new LineOr(text(altText));
    }

    /**
     * Styles the input {@link com.opencastsoftware.prettier4j.Doc Doc} using the styles
     * provided via {@code styles}.
     *
     * @param doc the input document.
     * @param styles the styles to use to decorate the input {@code doc}.
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} decorated with the ANSI styles provided.
     * @see Styles
     * @see com.opencastsoftware.prettier4j.ansi.Color Color
     */
    public static Doc styled(Doc doc, Styles.StylesOperator ...styles) {
        return new Styled(doc, styles);
    }

    /**
     * Reduce a collection of documents using the binary operator {@code fn},
     * returning an empty document if the collection is empty.
     *
     * @param documents the collection of documents.
     * @param fn        the binary operator for combining documents.
     * @return a document built by reducing the {@code documents} using the operator
     *         {@code fn}.
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
     * @return a document built by reducing the {@code documents} using the operator
     *         {@code fn}.
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
     *         separated by the {@code separator}.
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
     *         separated by the {@code separator}.
     */
    public static Doc intersperse(Doc separator, Stream<Doc> documents) {
        return Doc.fold(documents, (left, right) -> {
            return left.append(separator).append(right);
        });
    }

    /**
     * Creates a {@link com.opencastsoftware.prettier4j.Doc Doc} which represents a
     * group that can be flattened into a more compact layout.
     *
     * @param doc the document which is declared as a group which may be flattened.
     * @return a {@link com.opencastsoftware.prettier4j.Doc Doc} which represents a
     *         group that can be flattened into a more compact layout.
     */
    public static Doc group(Doc doc) {
        return alternatives(doc.flatten(), doc);
    }

    /**
     * Inspects the remaining space on the current line and the entries in the
     * current layout to see whether they fit onto the current line.
     * <p>
     * It's only necessary to inspect entries up to the next line break.
     *
     * @param remaining the remaining space on the current line.
     * @param entries   the entries we'd like to fit onto this line
     * @return true if we can fit all
     *         {@link com.opencastsoftware.prettier4j.Doc.Text entries} up to the
     *         next line break into the remaining characters of the current line.
     */
    static boolean fits(int remaining, Deque<Map.Entry<Integer, Doc>> entries) {
        if (remaining < 0)
            return false;

        for (Map.Entry<Integer, Doc> entry : entries) {
            Doc entryDoc = entry.getValue();

            // normalization reduces Doc to Text, LineOr, Escape and Reset
            if (entryDoc instanceof Text) {
                Text textDoc = (Text) entryDoc;
                remaining -= textDoc.text().length();
                if (remaining < 0)
                    return false;
            } else if (entryDoc instanceof LineOr) {
                return true;
            } // No need to handle Escape or Reset here
        }

        return true;
    }

    /**
     * Choose between two layouts at the current rendering width, indentation level
     * and line position, choosing the {@code left} document if it fits and the
     * {@code right} document otherwise.
     *
     * @param width    the preferred maximum width for rendering.
     * @param indent   the current indentation level.
     * @param position the position in the current line.
     * @param left     the preferred compact layout.
     * @param right    the expanded layout.
     * @return the entries of the chosen layout.
     */
    static Deque<Map.Entry<Integer, Doc>> chooseLayout(int width, int indent, int position, Doc left, Doc right) {
        Deque<Map.Entry<Integer, Doc>> leftEntries = normalize(width, indent, position, left);
        return fits(width - position, leftEntries) ? leftEntries : normalize(width, indent, position, right);
    }

    /**
     * Traverse the input {@code doc} recursively, eliminating all nodes except for
     * {@link com.opencastsoftware.prettier4j.Doc.Text Text} and subtypes of
     * {@link com.opencastsoftware.prettier4j.Doc.LineOr LineOr}, and producing a
     * queue of entries to be rendered.
     *
     * @param width    the preferred maximum width for rendering.
     * @param indent   the current indentation level.
     * @param position the current position in the line.
     * @param doc      the document to be rendered.
     * @return a queue of entries to be rendered.
     */
    static Deque<Map.Entry<Integer, Doc>> normalize(int width, int indent, int position, Doc doc) {
        // Not yet normalized entries
        Deque<Map.Entry<Integer, Doc>> inQueue = new ArrayDeque<>();

        // Normalized entries
        Deque<Map.Entry<Integer, Doc>> outQueue = new ArrayDeque<>();

        // Start with the outer Doc
        inQueue.add(new SimpleEntry<>(indent, doc));

        while (!inQueue.isEmpty()) {
            Map.Entry<Integer, Doc> topEntry = inQueue.removeFirst();

            int entryIndent = topEntry.getKey();
            Doc entryDoc = topEntry.getValue();

            if (entryDoc instanceof Append) {
                // Eliminate Append
                Append appendDoc = (Append) entryDoc;
                // Note reverse order
                inQueue.addFirst(new SimpleEntry<>(entryIndent, appendDoc.right()));
                inQueue.addFirst(new SimpleEntry<>(entryIndent, appendDoc.left()));
            } else if (entryDoc instanceof Styled) {
                // Eliminate Styled
                Styled styledDoc = (Styled) entryDoc;
                // Note reverse order
                inQueue.addFirst(new SimpleEntry<>(entryIndent, Reset.getInstance()));
                inQueue.addFirst(new SimpleEntry<>(entryIndent, styledDoc.doc()));
                inQueue.addFirst(new SimpleEntry<>(entryIndent, new Escape(styledDoc.styles())));
            } else if (entryDoc instanceof Indent) {
                // Eliminate Indent
                Indent indentDoc = (Indent) entryDoc;
                int newIndent = entryIndent + indentDoc.indent();
                inQueue.addFirst(new SimpleEntry<>(newIndent, indentDoc.doc()));
            } else if (entryDoc instanceof Alternatives) {
                // Eliminate Alternatives
                Alternatives altDoc = (Alternatives) entryDoc;
                // These entries are already normalized
                Deque<Map.Entry<Integer, Doc>> chosenEntries = chooseLayout(
                        width, entryIndent, position, altDoc.left(), altDoc.right());
                // Note reverse order
                chosenEntries.descendingIterator().forEachRemaining(inQueue::addFirst);
            } else if (entryDoc instanceof Text) {
                Text textDoc = (Text) entryDoc;
                // Keep track of line length
                position += textDoc.text().length();
                outQueue.addLast(topEntry);
            } else if (entryDoc instanceof LineOr) {
                // Reset line length
                position = entryIndent;
                outQueue.addLast(topEntry);
            } else if (entryDoc instanceof Escape) {
                outQueue.addLast(topEntry);
            } else if (entryDoc instanceof Reset) {
                outQueue.addLast(topEntry);
            }
            // Eliminate Empty
        }

        return outQueue;
    }

    /**
     * Renders the input {@link com.opencastsoftware.prettier4j.Doc Doc} into an
     * {@link java.lang.Appendable Appendable}, aiming to lay out the document with at most
     * {@code width} characters on each line.
     *
     * @param width  the preferred maximum rendering width.
     * @param doc    the document to be rendered.
     * @param output the output to render into.
     * @throws IOException if the {@link Appendable} {@code output} throws when {@link Appendable#append(CharSequence) append}ed.
     */
    public static void render(int width, Doc doc, Appendable output) throws IOException {
        Deque<Map.Entry<Integer, Doc>> renderQueue = normalize(width, 0, 0, doc);
        AttrsStack attrsStack = new AttrsStack();

        for (Map.Entry<Integer, Doc> entry : renderQueue) {
            int entryIndent = entry.getKey();
            Doc entryDoc = entry.getValue();

            // normalization reduces Doc to Text, LineOr, Escape and Reset
            if (entryDoc instanceof Text) {
                Text textDoc = (Text) entryDoc;
                output.append(textDoc.text());
            } else if (entryDoc instanceof LineOr) {
                output.append(System.lineSeparator());
                for (int i = 0; i < entryIndent; i++) {
                    output.append(" ");
                }
            } else if (entryDoc instanceof Reset) {
                long resetAttrs = attrsStack.popLast();
                long prevAttrs = attrsStack.peekLast();
                output.append(Attrs.transition(resetAttrs, prevAttrs));
            } else if (entryDoc instanceof Escape) {
                Escape escapeDoc = (Escape) entryDoc;
                long prevAttrs = attrsStack.peekLast();
                if (prevAttrs == Attrs.NULL) { prevAttrs = Attrs.EMPTY; }
                long newAttrs = Attrs.withStyles(prevAttrs, escapeDoc.styles());
                attrsStack.pushLast(newAttrs);
                output.append(Attrs.transition(prevAttrs, newAttrs));
            }
        }
    }

    /**
     * Renders the input {@link com.opencastsoftware.prettier4j.Doc Doc} into a
     * {@link java.lang.String String}, aiming to lay out the document with at most
     * {@code width} characters on each line.
     *
     * @param width  the preferred maximum rendering width.
     * @param doc    the document to be rendered.
     * @return the document laid out as a {@link java.lang.String String}.
     */
    public static String render(int width, Doc doc) {
        StringBuilder output = new StringBuilder();

        try {
            render(width, doc, output);
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }

        return output.toString();
    }
}
