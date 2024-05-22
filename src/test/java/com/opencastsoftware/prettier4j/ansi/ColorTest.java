/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class ColorTest {
    @Test
    void testEquals() {
        EqualsVerifier.forClasses(
                Color.class, Color16.class,
                Color256.class, ColorRgb.class)
            .usingGetClass().verify();
    }

    @Test
    void testToString() {
        ToStringVerifier.forClasses(Color16.class, Color256.class, ColorRgb.class).verify();
    }
}
