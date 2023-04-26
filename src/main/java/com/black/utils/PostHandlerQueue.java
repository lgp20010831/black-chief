package com.black.utils;

import lombok.NonNull;

import java.util.AbstractList;
import java.util.LinkedList;

public class PostHandlerQueue<T> extends AbstractList<T> {


    protected final LinkedList<T> queue;

    public PostHandlerQueue() {
        this.queue = new LinkedList<>();
    }
    
    public interface AddAppoint<T>{

        /** 传递第一个元素和下一个元素, 如果返回 true, 则会将值插入到 preElement 后面 */
        boolean appoint(T preElement, T postElemnt);
    }

    public void addFirst(T argParser){
        queue.addFirst(argParser);
    }

    public void accurateAdd(@NonNull T postArgParser, @NonNull AddAppoint<T> argParserAddAppoint){
        for (int i = 0; i < queue.size(); i++) {
            if (i != queue.size() - 1){
                if (argParserAddAppoint.appoint(get(i), get(i + 1))) {
                    add(i + 1, postArgParser);
                    return;
                }
            }
        }
    }

    @Override
    public T get(int index) {
        return queue.get(index);
    }

    @Override
    public int size() {
        return queue.size();
    }


    @Override
    public void add(int index, T element) {
        queue.add(index, element);
    }

    @Override
    public T set(int index, T element) {
        return queue.set(index, element);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return queue.toString();
    }
}
