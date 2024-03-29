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
implementation("com.opencastsoftware:prettier4j:0.2.0")
```

Maven (pom.xml):
```xml
<dependency>
    <groupId>com.opencastsoftware</groupId>
    <artifactId>prettier4j</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Usage

To render documents using this library you must use `com.opencastsoftware.prettier4j.Doc`.

In order to create documents, check out the static methods of that class, especially:

* `empty()` - creates an empty `Doc`.
* `text(String)` - creates a `Doc` from a `String`. These are used as the atomic text nodes of a document.

To render documents, the `render(int)` instance method is provided. The argument to this method declares a target line width when laying out the document.

It's not always possible for documents to fit within this target width. For example, a single `Doc.text` node may be longer than the target width if the argument `String` is long enough.

To concatenate documents, the `append(Doc)` instance method and related methods providing different separators are provided.

As a general rule, the best way to construct documents using this algorithm is to construct your document by concatenating text nodes, while declaring each place where a line break could be added if necessary.

The `lineOrSpace()`, `lineOrEmpty()` and related static methods are used to declare line breaks which may be replaced with alternative content if the current `Doc` is flattened.

The `line()` static method creates a line break which may not be flattened.

However, none of these primitives create flattened layouts on their own.

In order to declare how documents can be flattened, you must declare groups within a document.

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

However, if we declare each of those documents as a group using the static method `group(Doc)`, they are rendered differently:

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

As a result, the first call to `render(int)` renders a space-separated list, whereas the second call renders as a newline separated list. The width of 5 characters provided to the render method in the second call does not allow enough space for the entire group to render on a single line.

## Acknowlegements

The code in this repository is a pretty direct port of the paper's Haskell code to Java.

However, the names relating to line breaks (`lineOrSpace`, `lineOrEmpty` etc.) in this project are inspired by those used in [typelevel/paiges](https://github.com/typelevel/paiges), an excellent Scala port of the same algorithm.

## License

All code in this repository is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE).
