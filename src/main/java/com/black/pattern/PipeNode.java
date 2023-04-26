package com.black.pattern;

public abstract class PipeNode<A, B> {

    PipeNode<A, B> next;

    PipeNode<A, B> prev;

    Pipeline<? extends PipeNode<A, B>, A, B> pipeline;

    public PipeNode<A, B> next(){
        return next;
    }

    public PipeNode<A, B> prev(){
        return prev;
    }

    public void headfireRunnable(PipeNode<A, B> current, A arg){
        current.headfire(current, arg);
    }

    public void tailfireRunnable(PipeNode<A, B> current, B arg){
        current.tailfire(current, arg);
    }

    public void setPipeline(Pipeline<? extends PipeNode<A, B>, A, B> pipeline) {
        this.pipeline = pipeline;
    }

    public Pipeline<? extends PipeNode<A, B>, A, B> getPipeline() {
        return pipeline;
    }

    public void headfire(PipeNode<A, B> node, A arg){
        PipeNode<A, B> next = node.next();
        if (next != null){
            next.headfireRunnable(next, arg);
        }
    }

    public void tailfire(PipeNode<A, B> node, B arg){
        PipeNode<A, B> prev = node.prev();
        if (prev != null){
            prev.tailfireRunnable(prev, arg);
        }
    }
}
