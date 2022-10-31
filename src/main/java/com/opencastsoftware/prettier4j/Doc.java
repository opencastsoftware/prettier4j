package com.opencastsoftware.prettier4j;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

public abstract class Doc {
    abstract Doc flatten();

    public Doc append(Doc other) {
        return new Append(this, other);
    }

    public Doc appendSpace(Doc other) {
        return this
                .append(text(" "))
                .append(other);
    }

    public Doc appendLine(Doc other) {
        return this
                .append(line())
                .append(other);
    }

    public Doc appendLineOr(String altText, Doc other) {
        return this
                .append(group(lineOr(altText)))
                .append(other);
    }

    public Doc indent(int spaces) {
        return indent(spaces, this);
    }

    public Doc bracket(int indent, String left, String right) {
        return group(
                text(left)
                        .append(line().append(this).indent(indent))
                        .appendLine(text(right)));
    }

    public String render(int width) {
        return render(width, this);
    }

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

    public static class Line extends Doc {
        Line() {
        }

        @Override
        Doc flatten() {
            return text(" ");
        }

        @Override
        public String toString() {
            return "Line []";
        }
    }

    public static class LineOr extends Doc {
        private String altText;

        LineOr(String altText) {
            this.altText = altText;
        }

        @Override
        Doc flatten() {
            return text(altText);
        }

        @Override
        public String toString() {
            return "LineOr [altText=" + altText + "]";
        }
    }

    public static class Empty extends Doc {
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

    public static Doc text(String text) {
        return new Text(text);
    }

    static Doc alternatives(Doc left, Doc right) {
        return new Alternatives(left, right);
    }

    public static Doc indent(int indent, Doc doc) {
        return new Indent(indent, doc);
    }

    public static Doc line() {
        return new Line();
    }

    public static Doc lineOr(String altText) {
        return new LineOr(altText);
    }

    public static Doc empty() {
        return new Empty();
    }

    public static Doc group(Doc doc) {
        return alternatives(doc.flatten(), doc);
    }

    static boolean fits(int remaining, Deque<Map.Entry<Integer, Doc>> entries) {
        if (remaining < 0)
            return false;

        for (Map.Entry<Integer, Doc> entry : entries) {
            Doc entryDoc = entry.getValue();

            // normalization reduces Doc to Text, Line and LineOr
            if (entryDoc instanceof Text) {
                Text textDoc = (Text) entryDoc;
                remaining -= textDoc.text().length();
                if (remaining < 0)
                    return false;
            } else if (entryDoc instanceof Line || entryDoc instanceof LineOr) {
                return true;
            }
        }

        return true;
    }

    static Deque<Map.Entry<Integer, Doc>> bestLayout(int width, int indent, int position, Doc left, Doc right) {
        Deque<Map.Entry<Integer, Doc>> leftEntries = normalize(width, indent, position, left);
        return fits(width - position, leftEntries) ? leftEntries : normalize(width, indent, position, right);
    }

    static Deque<Map.Entry<Integer, Doc>> normalize(int width, int indent, int position, Doc doc) {
        // Not yet normalized entries
        Deque<Map.Entry<Integer, Doc>> inQueue = new ArrayDeque<>();

        // Normalized entries
        Deque<Map.Entry<Integer, Doc>> outQueue = new ArrayDeque<>();

        // Start with the outer Doc
        inQueue.add(new SimpleEntry<Integer, Doc>(indent, doc));

        while (!inQueue.isEmpty()) {
            Map.Entry<Integer, Doc> topEntry = inQueue.removeFirst();

            int entryIndent = topEntry.getKey();
            Doc entryDoc = topEntry.getValue();

            if (entryDoc instanceof Append) {
                // Eliminate Append
                Append appendDoc = (Append) entryDoc;
                // Note reverse order
                inQueue.addFirst(new SimpleEntry<Integer, Doc>(entryIndent, appendDoc.right()));
                inQueue.addFirst(new SimpleEntry<Integer, Doc>(entryIndent, appendDoc.left()));
            } else if (entryDoc instanceof Indent) {
                // Eliminate Indent
                Indent indentDoc = (Indent) entryDoc;
                inQueue.addFirst(new SimpleEntry<Integer, Doc>(entryIndent + indentDoc.indent(), indentDoc.doc()));
            } else if (entryDoc instanceof Alternatives) {
                // Eliminate Alternatives
                Alternatives altDoc = (Alternatives) entryDoc;
                Deque<Map.Entry<Integer, Doc>> chosenEntries = bestLayout(
                        width, entryIndent, position, altDoc.left(), altDoc.right());
                Iterator<Map.Entry<Integer, Doc>> entriesIterator = chosenEntries.descendingIterator();
                while (entriesIterator.hasNext()) {
                    inQueue.addFirst(entriesIterator.next());
                }
            } else if (entryDoc instanceof Text) {
                Text textDoc = (Text) entryDoc;
                // Keep track of line length
                position += textDoc.text().length();
                outQueue.addLast(topEntry);
            } else if (entryDoc instanceof Line || entryDoc instanceof LineOr) {
                // Reset line length
                position = entryIndent;
                outQueue.addLast(topEntry);
            }
            // Eliminate Empty
        }

        return outQueue;
    }

    public static String render(int width, Doc doc) {
        StringBuilder output = new StringBuilder();

        Deque<Map.Entry<Integer, Doc>> renderStack = normalize(width, 0, 0, doc);

        for (Map.Entry<Integer, Doc> entry : renderStack) {
            int entryIndent = entry.getKey();
            Doc entryDoc = entry.getValue();

            // normalization reduces Doc to Text, Line and LineOr
            if (entryDoc instanceof Text) {
                Text textDoc = (Text) entryDoc;
                String text = textDoc.text();
                output.append(text);
            } else if (entryDoc instanceof Line || entryDoc instanceof LineOr) {
                output.append(System.lineSeparator());
                for (int i = 0; i < entryIndent; i++) {
                    output.append(" ");
                }
            }
        }

        return output.toString();
    }
}
