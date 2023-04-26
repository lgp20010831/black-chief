package com.black.pattern;

import java.util.concurrent.ArrayBlockingQueue;

public class Caretaker<M> {

    private final ArrayBlockingQueue<M> queue;

    private M current;

    private final int cap;

    public Caretaker(int cap) {
        this.cap = cap;
        queue = new ArrayBlockingQueue<M>(cap);
    }

    public void setCurrent(M current) {
        this.current = current;
    }

    public M getCurrent() {
        return current;
    }

    public ArrayBlockingQueue<M> getQueue() {
        return queue;
    }

    public void record(M m){
        if (m != null){
            if (queue.size() == cap){
                throwfirst();
            }
            queue.add(m);
        }
    }

    public M throwfirst(){
        return queue.poll();
    }
}
