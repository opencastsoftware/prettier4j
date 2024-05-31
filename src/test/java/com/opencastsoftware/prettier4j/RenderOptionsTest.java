/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class RenderOptionsTest {
    @Test
    void testEquals() {
        EqualsVerifier.forClass(RenderOptions.class).usingGetClass().verify();
    }

    @Test
    void testToString() {
        ToStringVerifier.forClass(RenderOptions.class).verify();
    }
}
