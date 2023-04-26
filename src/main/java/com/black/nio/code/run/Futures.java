package com.black.nio.code.run;

public interface Futures<V> extends Runnable{

    Future<V> getFuture();

}
