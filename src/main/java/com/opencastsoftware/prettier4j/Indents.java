package com.opencastsoftware.prettier4j;

import java.util.*;

/**
 * Holds a cache of indent {@link String}s.
 * <p>
 * Most source code is indented by 20 or fewer spaces, so we cache
 * indent {@link String}s of up to 20 spaces to avoid allocating them
 * repeatedly during rendering.
 */
final class Indents {
    private static final int CACHED_INDENTS = 20;
    private static final String[] indents = new String[CACHED_INDENTS];

    static {
        for (int i = 1; i <= CACHED_INDENTS; i++) {
            indents[i - 1] = makeIndent(i).intern();
        }
    }

    private static String makeIndent(int i) {
        char[] indentChars = new char[i];
        Arrays.fill(indentChars, ' ');
        return new String(indentChars);
    }

    public static String get(int i) {
        if (i > CACHED_INDENTS) {
            return makeIndent(i);
        } else {
            return indents[i - 1];
        }
    }

    private Indents() {}
}
