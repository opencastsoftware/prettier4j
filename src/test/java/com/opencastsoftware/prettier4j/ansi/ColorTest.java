/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import com.jparams.verifier.tostring.ToStringVerifier;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ColorTest {
    @Test
    void testEquals() {
        EqualsVerifier.forClasses(
                        Color.class, Color16.class,
                        ColorXterm.class, ColorRgb.class)
                .usingGetClass().verify();
    }

    @Test
    void testToString() {
        ToStringVerifier.forClasses(Color16.class, ColorXterm.class, ColorRgb.class).verify();
    }

    @Property
    void color16IsNullForInvalidCode(@ForAll("invalidColor16Codes") int code) {
        assertThat(Color16.withCode(code), is(nullValue()));
    }

    @Property
    void color16RoundTripsViaCode(@ForAll Color16 initial) {
        Color16 roundtrip = Color16.withCode(initial.code());
        assertThat(roundtrip, is(equalTo(initial)));
    }

    @Property
    void colorRgbRoundTripsViaPacked(
            @ForAll @IntRange(min = 0, max = 255) int r,
            @ForAll @IntRange(min = 0, max = 255) int g,
            @ForAll @IntRange(min = 0, max = 255) int b
    ) {
        ColorRgb initial = new ColorRgb(r, g, b);
        ColorRgb roundtrip = ColorRgb.fromPacked(initial.packed());
        assertThat(roundtrip, is(equalTo(initial)));
    }

    @Provide
    Arbitrary<Integer> invalidColor16Codes() {
        Set<Integer> color16Codes = Arrays.stream(Color16.values())
                .map(Color16::code)
                .collect(Collectors.toSet());
        return Arbitraries.integers().filter(i -> !color16Codes.contains(i));
    }
}
