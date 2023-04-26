package com.black.pattern;

import java.util.Collection;

public class Originator<M> {

    final Caretaker<M> caretaker;

    public Originator(int cap) {
        caretaker = new Caretaker<>(cap);
    }

    public void setMessage(M m){
        caretaker.record(m);
    }

    public void setMessageAndRecord(M m){
        caretaker.record(m);
        caretaker.setCurrent(m);
    }

    public M getCurrent(){
        return caretaker.getCurrent();
    }

    public Collection<M> getHistory(){
        return caretaker.getQueue();
    }
}
