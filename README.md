# prettier4j

[![CI](https://github.com/opencastsoftware/prettier4j/actions/workflows/ci.yml/badge.svg)](https://github.com/opencastsoftware/prettier4j/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/opencastsoftware/prettier4j/branch/main/graph/badge.svg?token=JHVF151VM1)](https://codecov.io/gh/opencastsoftware/prettier4j)
[![Maven Central](https://img.shields.io/maven-central/v/com.opencastsoftware/prettier4j)](https://search.maven.org/search?q=g%3Acom.opencastsoftware+AND+a%3Aprettier4j)
[![javadoc](https://javadoc.io/badge2/com.opencastsoftware/prettier4j/javadoc.svg)](https://javadoc.io/doc/com.opencastsoftware/prettier4j)
[![License](https://img.shields.io/github/license/opencastsoftware/prettier4j?color=blue)](https://spdx.org/licenses/Apache-2.0.html)

A Java implementation of Philip Wadler's ["A prettier printer"](https://homepages.inf.ed.ac.uk/wadler/papers/prettier/prettier.pdf), a pretty-printing algorithm for laying out hierarchical documents as text.

This algorithm is particularly suitable for formatting source code (see for example [Prettier](https://prettier.io/)).

## Installation

*prettier4j* is published for Java 8 and above.

Gradle (build.gradle / build.gradle.kts):
```groovy
implementation("com.opencastsoftware:prettier4j:0.3.0")
```

Maven (pom.xml):
```xml
<dependency>
    <groupId>com.opencastsoftware</groupId>
    <artifactId>prettier4j</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Usage

### Basics

To render documents using this library you must use [com.opencastsoftware.prettier4j.Doc](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html).

In order to create documents, check out the static methods of that class, especially:

* [empty()](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#empty()) - creates an empty `Doc`.
* [text(String)](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#text(java.lang.String)) - creates a `Doc` from a `String`. These are used as the atomic text nodes of a document.

To render documents, the [render(int)](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#render(int)) instance method is provided. The argument to this method declares a target line width when laying out the document.

It's not always possible for documents to fit within this target width. For example, a single `Doc.text` node may be longer than the target width if the argument `String` is long enough.

To concatenate documents, the [append(Doc)](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#append(com.opencastsoftware.prettier4j.Doc)) instance method and related methods providing different separators are provided.

As a general rule, the best way to construct documents using this algorithm is to construct your document by concatenating text nodes, while declaring each place where a line break could be added if necessary.

The layout algorithm uses the concept of "flattened" layouts - layouts which are used when they are able to fit within the remaining space on the current line. In other words, they are "flattened" onto a single line.

The [lineOrSpace()](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#lineOrSpace()), [lineOrEmpty()](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#lineOrEmpty()) and related static methods are used to declare line breaks which may be replaced with alternative content if the current `Doc` is flattened.

The [line()](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#line()) static method creates a line break which may not be flattened.

However, none of these primitives create flattened layouts on their own.

In order to declare how documents can be flattened, you must declare groups within a document which are all flattened together.

For example, the following documents each render to the same content:

```java
Doc.text("one")
    .appendLineOrSpace(Doc.text("two"))
    .appendLineOrSpace(Doc.text("three"))
    .render(30);

// ===> "one\ntwo\nthree"

Doc.text("one")
    .appendLineOrSpace(Doc.text("two"))
    .appendLineOrSpace(Doc.text("three"))
    .render(5);

// ===> "one\ntwo\nthree"
```

However, if we declare each of those documents as a group using the static method [group(Doc)](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#group(com.opencastsoftware.prettier4j.Doc)), they are rendered differently:

```java
Doc.group(
    Doc.text("one")
        .appendLineOrSpace(Doc.text("two"))
        .appendLineOrSpace(Doc.text("three")))
    .render(30);

// ===> "one two three"

Doc.group(
    Doc.text("one")
        .appendLineOrSpace(Doc.text("two"))
        .appendLineOrSpace(Doc.text("three")))
    .render(5);

// ===> "one\ntwo\nthree"
```

By declaring a group, we have specified that the contents of each group can be flattened onto a single line if there is enough space.

However, if there is not enough space for all three words on the line, they must be rendered using their expanded layout.

As a result, the first call to `render` renders a space-separated list, whereas the second call renders as a newline separated list. The width of 5 characters provided to the render method in the second call does not allow enough space for the entire group to render on a single line.

### ANSI styled text

As of version 0.2.0, there is support for rendering text with ANSI escape code sequences.

This enables text styles like foreground and background colours, underlines and bold font styling to be applied to a `Doc`.

To do this, the [styled(Styles.StylesOperator...)](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#styled(com.opencastsoftware.prettier4j.ansi.Styles.StylesOperator...)) method of the `Doc` class can be used.

The styles that can be applied can be found in the [Styles](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/ansi/Styles.html) class.

For example:

```java
Doc.text("one").styled(Style.fg(Color.red()))
    .appendLineOrSpace(Doc.text("two").styled(Style.fg(Color.green())))
    .appendLineOrSpace(Doc.text("three").styled(Style.fg(Color.blue())))
    .render(30);

// ===> "\u001b[31mone\u001b[0m \u001b[32mtwo\u001b[0m \u001b[34mthree\u001b[0m"
```

### Parameterized documents

As of version 0.3.0, there is support for declaring parameters in documents via the [param(String)](https://www.javadoc.io/static/com.opencastsoftware/prettier4j/0.3.0/com/opencastsoftware/prettier4j/Doc.html#param(java.lang.String)) method of the `Doc` class.

Parameters are named, and a named parameter may appear multiple times in the same document.

All parameters *must* be bound to a `Doc` value before rendering.

Binding parameters is exactly equivalent to inlining the argument values into the original document.

For example:

```java
Doc.param("one")
    .appendLineOrSpace(Doc.param("two"))
    .appendLineOrSpace(Doc.param("three"))
    .bind(
        "one", Doc.text("1"),
        "two", Doc.text("2"),
        "three", Doc.text("3"))
    .render(30);

// ===> "1 2 3"
```

is exactly equivalent to:

```java
Doc.text("1")
    .appendLineOrSpace(Doc.text("2"))
    .appendLineOrSpace(Doc.text("3"))
    .render(30);

// ===> "1 2 3"
```

## Acknowlegements

The code in this repository is a pretty direct port of the paper's Haskell code to Java.

However, the names relating to line breaks (`lineOrSpace`, `lineOrEmpty` etc.) in this project are inspired by those used in [typelevel/paiges](https://github.com/typelevel/paiges), an excellent Scala port of the same algorithm.

## License

All code in this repository is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE).
