/*
 * SPDX-FileCopyrightText:  Copyright 2022-2023 Opencast Software Europe Ltd
 * SPDX-License-Identifier: Apache-2.0
 */
package com.opencastsoftware.prettier4j;

import java.util.List;

public class Node {
    private String data;
    private List<Node> subTrees;

    public Node(String data, List<Node> subTrees) {
        this.data = data;
        this.subTrees = subTrees;
    }

    public Doc show() {
        return Doc.group(
                Doc.text(data).append(
                        subTrees.isEmpty() ? Doc.empty()
                                : Doc.text("[")
                                        .append(
                                                Doc.intersperse(
                                                        Doc.text(",").append(Doc.lineOrSpace()),
                                                        subTrees.stream().map(Node::show)).indent(1))
                                        .append(Doc.text("]"))
                                        .indent(data.length())));
    }

    public Doc showPrime() {
        return Doc.text(data)
                .append(
                        subTrees.isEmpty() ? Doc.empty()
                                : Doc.intersperse(
                                        Doc.text(",").append(Doc.lineOrSpace()),
                                        subTrees.stream().map(Node::showPrime))
                                        .bracket(2, "[", "]"));
    }
}
