/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.LongRange;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AttrsStackTest {
    @Test
    void peekingAnEmptyStackReturnsNullAttrs() {
        AttrsStack stack = new AttrsStack();
        assertThat(stack.peekLast(), is(Attrs.NULL));
    }

    @Test
    void poppingAnEmptyStackReturnsNullAttrs() {
        AttrsStack stack = new AttrsStack();
        assertThat(stack.popLast(), is(Attrs.NULL));
    }

    @Property
    void peekingAnElementAfterPushing(@ForAll @LongRange(min = 1L) long attrs) {
        AttrsStack stack = new AttrsStack();
        stack.pushLast(attrs);
        assertThat(stack.peekLast(), is(attrs));
    }

    @Property
    void poppingAnElementAfterPushing(@ForAll @LongRange(min = 1L) long attrs) {
        AttrsStack stack = new AttrsStack();
        stack.pushLast(attrs);
        assertThat(stack.popLast(), is(attrs));
    }

    @Property
    void peekingAnEmptyStackAfterPushingAndPopping(@ForAll @LongRange(min = 1L) long attrs) {
        AttrsStack stack = new AttrsStack();
        stack.pushLast(attrs);
        stack.popLast();
        assertThat(stack.peekLast(), is(Attrs.NULL));
    }

    @Property
    void poppingReturnsTheElementThatWasPeeked(@ForAll @LongRange(min = 1L) long attrs) {
        AttrsStack stack = new AttrsStack();
        stack.pushLast(attrs);
        assertThat(stack.peekLast(), is(stack.popLast()));
    }

    @Property
    void pushingMoreThanInitialCapacity(@ForAll @LongRange(min = 1L) long attrs1, @ForAll @LongRange(min = 1L) long attrs2) {
        AttrsStack stack = new AttrsStack(1);
        stack.pushLast(attrs1);
        stack.pushLast(attrs2);
        assertThat(stack.peekLast(), is(attrs2));
    }

    @Property
    void poppingReturnsElementsInReverseOrderOfPushing(@ForAll @LongRange(min = 1L) long attrs1, @ForAll @LongRange(min = 1L) long attrs2) {
        AttrsStack stack = new AttrsStack();
        stack.pushLast(attrs1);
        stack.pushLast(attrs2);
        assertThat(stack.peekLast(), is(attrs2));
        assertThat(stack.popLast(), is(attrs2));
        assertThat(stack.peekLast(), is(attrs1));
        assertThat(stack.popLast(), is(attrs1));
        assertThat(stack.peekLast(), is(Attrs.NULL));
    }
}
