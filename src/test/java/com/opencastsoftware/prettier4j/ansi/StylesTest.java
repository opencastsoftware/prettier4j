/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import com.jparams.verifier.tostring.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

public class StylesTest {
    @Test
    void testEquals() {
        EqualsVerifier.forClasses(Styles.Fg.class, Styles.Bg.class)
                .usingGetClass()
                .verify();
    }

    @Test
    void testToString() {
        ToStringVerifier.forClasses(
                Styles.Fg.class, Styles.Bg.class, Styles.Bold.class,
                Styles.Faint.class, Styles.Italic.class, Styles.Underline.class,
                Styles.Blink.class, Styles.Inverse.class, Styles.Strikethrough.class)
            .withIgnoredFields("shiftValue").verify();
    }
}
