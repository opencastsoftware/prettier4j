/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import net.jqwik.api.*;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ColorTypeTest {
    @Property
    void colorTypeRoundTripsViaCode(@ForAll ColorType initial) {
        ColorType roundtrip = ColorType.withCode(initial.code());
        assertThat(roundtrip, is(equalTo(initial)));
    }

    @Property
    void colorTypeIsNullForInvalidCode(@ForAll("invalidColorTypeCodes") int code) {
        assertThat(ColorType.withCode(code), is(nullValue()));
    }

    @Provide
    Arbitrary<Integer> invalidColorTypeCodes() {
        Set<Integer> colorTypeCodes = Arrays.stream(ColorType.values())
                .map(ColorType::code)
                .collect(Collectors.toSet());
        return Arbitraries.integers().filter(i -> !colorTypeCodes.contains(i));
    }
}
