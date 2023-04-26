package com.black.utils;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class FutureCollection<E> extends AbstractCollection<E> implements Future<Object> {

    protected final Collection<E> collection;

    protected boolean cancel = false;

    protected boolean done = false;

    protected FutureCollection() {
        collection = create();
    }

    abstract Collection<E> create();

    @Override
    public Iterator<E> iterator() {
        for (;;){
            if (isCancelled()){
                throw new IllegalStateException("future is cancel");
            }

            if (isDone()){
                System.out.println("未完成");
                break;
            }
        }
        return collection.iterator();
    }

    @Override
    public int size() {
        return collection.size();
    }

    public void done(){
        done = true;
    }

    @Override
    public boolean add(E e) {
        return collection.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return collection.addAll(c);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return cancel = true;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public boolean isDone() {
        return !isCancelled() && done;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException("get null");
    }

    @Override
    public Object get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("get null");
    }
}
