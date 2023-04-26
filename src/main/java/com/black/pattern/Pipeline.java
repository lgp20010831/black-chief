package com.black.pattern;

import java.util.ArrayList;
import java.util.List;

public class Pipeline<P extends PipeNode<A, B>, A, B> {

    final P head;

    final P tail;

    public Pipeline(P head, P tail) {
        head.setPipeline(this);
        tail.setPipeline(this);
        this.head = head;
        this.tail = tail;
        head.next = tail;
        tail.prev = head;
    }

    public P[] toArray(){
        return (P[]) toList().toArray();
    }

    public List<P> toList(){
        P current = head;
        List<P> list = new ArrayList<>();
        while (current != null){
            list.add(current);
            current = (P) current.next;
        }
        return list;
    }


    public Channel<ChannelAdaptation, A> openChannel(){
        throw new UnsupportedOperationException("open channel");
    }

    public P removeFirst(){
        PipeNode<A, B> next = head.next;
        remove((P) next);
        return (P) next;
    }

    public P removeLast(){
        PipeNode<A, B> prev = tail.prev;
        remove((P) prev);
        return (P) prev;
    }

    public Pipeline<P, A, B> remove(P node){
        if (node == null || node == tail || node == head) return this;
        PipeNode<A, B> prev = node.prev;
        PipeNode<A, B> next = node.next;
        if (prev != null){
            prev.next = next;
        }
        if (next != null){
            next.prev = prev;
        }
        return this;
    }

    public Pipeline<P, A, B> addFirst(P node){
        if (node == null) return this;
        node.setPipeline(this);
        PipeNode<A, B> next = head.next;
        next.prev = node;
        head.next = node;
        node.prev = head;
        node.next = next;
        return this;
    }

    public Pipeline<P, A, B> addLast(P node){
        if (node == null) return this;
        node.setPipeline(this);
        PipeNode<A, B> prev = tail.prev;
        prev.next = node;
        node.prev = prev;
        node.next = tail;
        tail.prev = node;
        return this;
    }

    public void headfire(A arg){
        head.headfire(head, arg);
    }

    public void tailfire(B arg){
        tail.tailfire(tail, arg);
    }
}
