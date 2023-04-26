package com.black.nio.code;

import lombok.extern.log4j.Log4j2;
import sun.nio.ch.SelectorImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class SelectorWrapper extends Selector{

    //可能会更换
    private Selector selector;

    private final Set<SelectionKey> keySetWrapper;

    private AtomicBoolean runningState = new AtomicBoolean(true);

    public SelectorWrapper() throws IOException {
        keySetWrapper = createKeySet();
        selector = SelectorProvider.provider().openSelector();
        Class<SelectorImpl> selectorClass = SelectorImpl.class;
        try {

            Field selectedKeysField = selectorClass.getDeclaredField("selectedKeys");
            Field publicSelectedKeysField = selectorClass.getDeclaredField("publicSelectedKeys");
            selectedKeysField.setAccessible(true);
            publicSelectedKeysField.setAccessible(true);
            selectedKeysField.set(selector, keySetWrapper);
            publicSelectedKeysField.set(selector, keySetWrapper);
        }catch (Throwable e){
            selector.close();
            throw new AttysNioException("无法增强 selector", e);
        }
    }

    public AtomicBoolean getRunningState() {
        return runningState;
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public boolean isOpen() {
        return selector.isOpen();
    }

    @Override
    public SelectorProvider provider() {
        return selector.provider();
    }

    @Override
    public Set<SelectionKey> keys() {
        return selector.keys();
    }

    @Override
    public Set<SelectionKey> selectedKeys() {
        return selector.selectedKeys();
    }

    @Override
    public int selectNow() throws IOException {
        keySetWrapper.clear();
        runningState.set(true);
        try {
            return selector.selectNow();
        }finally {
            runningState.compareAndSet(true, false);
        }
    }

    @Override
    public int select(long timeout) throws IOException {
        keySetWrapper.clear();
        runningState.set(true);
        try {
            return selector.select(timeout);
        }finally {
            runningState.compareAndSet(true, false);
        }
    }

    @Override
    public int select() throws IOException {
        return select(0L);
    }

    @Override
    public Selector wakeup() {
        return selector.wakeup();
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }

    protected Set<SelectionKey> createKeySet(){
        return new HashSet<>();
    }

}
