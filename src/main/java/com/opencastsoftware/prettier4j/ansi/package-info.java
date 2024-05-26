/**
 * This package contains utilities for producing styled text via ANSI escape codes.
 * <p>
 * The entry point for applying styles is the {@link com.opencastsoftware.prettier4j.Doc#styled(com.opencastsoftware.prettier4j.ansi.Styles.StylesOperator...) styled} method.
 * <p>
 * It can be used in conjunction with the static methods of the {@link com.opencastsoftware.prettier4j.ansi.Styles Styles} class in order to apply visual styles and colors to a {@link com.opencastsoftware.prettier4j.Doc Doc}.
 * <p>
 * Colors are applied using the {@link com.opencastsoftware.prettier4j.ansi.Styles#fg Styles.fg} and {@link com.opencastsoftware.prettier4j.ansi.Styles#bg(com.opencastsoftware.prettier4j.ansi.Color) Styles.bg} methods.
 * <p>
 * The {@link com.opencastsoftware.prettier4j.ansi.Color Color} class contains static methods that can be used to construct the 16 colors of the basic terminal palette, as well as colors in the xterm 256-color palette and 24-bit RGB colors.
 * <p>
 * For example, the following snippet styles a {@link com.opencastsoftware.prettier4j.Doc.Text Doc.Text} with bold font, white foreground text and a red background:
 *
 * <pre class="language-java">
 * {@code Doc.text("a").styled(
 *   bold(),
 *   fg(white()),
 *   bg(red()));}</pre>
 *
 * <p>
 * For more details about the behaviour of the ANSI escape codes, see the <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_(Select_Graphic_Rendition)_parameters">Select Graphic Rendition parameters</a>
 * section of the <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">ANSI Escape code</a> article on Wikipedia.
 */
package com.opencastsoftware.prettier4j.ansi;
