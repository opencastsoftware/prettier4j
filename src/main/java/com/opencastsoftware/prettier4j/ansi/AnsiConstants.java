/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import org.apiguardian.api.API;

import static org.apiguardian.api.API.Status.INTERNAL;

/**
 * This class declares constants for producing ANSI escape code sequences.
 */
@API(status = INTERNAL, consumers = {"com.opencastsoftware.prettier4j"})
public class AnsiConstants {
    /**
     * The CSI (Control Sequence Introducer) escape sequence.
     */
    public static final String CSI = "\u001b[";
    /**
     * The Reset SGR (Select Graphic Rendition) sequence.
     */
    public static final String RESET = "\u001b[0m";

    private AnsiConstants() {}
}
