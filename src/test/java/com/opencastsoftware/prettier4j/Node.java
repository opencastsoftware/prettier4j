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
                                                subTrees.stream()
                                                        .map(Node::show)
                                                        .reduce(Doc.empty(), (left, right) -> {
                                                            return left instanceof Doc.Empty ? right
                                                                    : left.append(Doc.text(",")).appendLineOrSpace(right);
                                                        }).indent(1))
                                        .append(Doc.text("]"))
                                        .indent(data.length())));
    }

    public Doc showPrime() {
        return Doc.text(data)
                .append(
                        subTrees.isEmpty() ? Doc.empty()
                                : subTrees.stream()
                                        .map(Node::showPrime)
                                        .reduce(Doc.empty(), (left, right) -> {
                                            return left instanceof Doc.Empty ? right
                                                    : left.append(Doc.text(",")).appendLineOrSpace(right);
                                        })
                                        .bracket(2, "[", "]"));
    }
}
