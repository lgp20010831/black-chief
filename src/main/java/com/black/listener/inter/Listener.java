package com.black.listener.inter;

public interface Listener<S> {


    void handlerEvent(S source) throws Throwable;

}
