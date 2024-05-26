/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import org.apiguardian.api.API;

import java.util.Arrays;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * A simple stack of {@code long}s, stored in a backing array.
 * <p>
 * When there is no longer any space in the backing array, a new array of twice the size is allocated.
 * <p>
 * No effort is made to reduce the size of this array: each {@link AttrsStack} is used only for the duration
 * of a single {@link com.opencastsoftware.prettier4j.Doc#render render}.
 */
@API(status = INTERNAL, consumers = {"com.opencastsoftware.prettier4j"})
public class AttrsStack {
    private static final int INITIAL_CAPACITY = 16;
    private long[] stack;
    private int size;

    public AttrsStack(int initialCapacity) {
        this.stack = new long[initialCapacity];
        this.size = 0;
    }

    public AttrsStack() {
        this(INITIAL_CAPACITY);
    }

    private void ensureCapacity() {
        if (size == stack.length) {
            stack = Arrays.copyOf(stack, stack.length * 2);
        }
    }

    public long peekLast() {
        if (size > 0) {
            return stack[size - 1];
        } else {
            return Attrs.NULL;
        }
    }

    public void pushLast(long attrs) {
        ensureCapacity();
        stack[size++] = attrs;
    }

    public long popLast() {
        if (size > 0) {
            return stack[--size];
        } else {
            return Attrs.NULL;
        }
    }
}
