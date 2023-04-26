package com.black.io.out;

import com.black.io.in.ObjectInputStream;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class ObjectOutputStream <T> implements Iterable<T>{

    protected T[] buf;

    protected int count;

    public ObjectOutputStream(){
        this(25);
    }

    public ObjectOutputStream(int size){
        this.buf = (T[]) new Object[size];
    }

    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0)
            grow(minCapacity);
    }


    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        buf = Arrays.copyOf(buf, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    public void write(T[] b){
        write(b, 0, b.length);
    }

    public void write(T[] b, int off, int len){
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    public synchronized void write(T t){
        ensureCapacity(count + 1);
        buf[count] = t;
        count += 1;
    }

    public void reset(){
        count = 0;
    }

    public T[] toBuffer(){
        return Arrays.copyOf(buf, count);
    }

    public int size(){
        return count;
    }

    public void flush(){

    }

    public void close(){

    }

    public ObjectInputStream<T> getInputStream(){
        return new ObjectInputStream<T>(toBuffer());
    }

    public void writeTo(ObjectOutputStream<T> outputStream){
        outputStream.write(buf, 0, count);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new ObjectInputStream.OISItr(getInputStream());
    }

}
