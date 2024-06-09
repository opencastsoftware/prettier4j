/*
 * SPDX-FileCopyrightText:  © 2024 Opencast Software Europe Ltd <https://opencastsoftware.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j.ansi;

import org.apiguardian.api.API;

import java.net.URI;

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
    /**
     * The OSC (Operating System Command) escape sequence.
     */
    public static final String OSC = "\u001b]";
    /**
     * The ST (String Terminator) escape sequence.
     */
    public static final String ST = "\u001b\\";
    /**
     * The escape sequence for opening a hyperlink.
     * <p>
     * This sequence must be followed by the {@link URI} of the hyperlink, then the {@link AnsiConstants#ST} sequence.
     */
    public static final String OPEN_LINK = OSC + 8 + ";;";
    /**
     * The escape sequence for closing a hyperlink.
     */
    public static final String CLOSE_LINK = OSC + 8 + ";;" + ST;

    private AnsiConstants() {}
}
