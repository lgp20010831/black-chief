package com.black.pattern;

public class NodeAdaptation<A> extends PipeNode<A, A>{

    final A target;

    public NodeAdaptation(A target) {
        this.target = target;
    }
}
