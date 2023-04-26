package com.black.io.in;

import com.black.throwable.IOSException;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

public class ObjectInputStream<T> implements Iterable<T>{

    protected T[] array;

    protected int count;

    protected int pos;

    public ObjectInputStream(Collection<T> collection){
        this((T[]) collection.toArray());
    }

    public ObjectInputStream(T[] array){
        this.array = array;
        count = array.length;
        pos = 0;
    }

    protected void check(){
        if (pos >= count){
            throw new IOSException("read end");
        }
    }

    public T read(){
        check();
        return array[pos ++];
    }

    public boolean hasMore(){
        return pos < count;
    }

    public void reset(){
        pos = 0;
    }

    public int read(T[] b){
        return read(b, 0, b.length);
    }

    public int read(T[] b, int off, int len){
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        T c = read();
        b[off] = c;
        int i = 1;
        for (; i < len ; i++) {
            c = read();
            b[off + i] = c;
        }
        return i;
    }

    public T[] copyArray(){
        T[] ts = (T[]) new Object[count];
        System.arraycopy(array, 0, ts, 0, count);
        return ts;
    }

    public int available(){
        return count - pos;
    }

    public void close(){

    }

    public static class OISItr<T> implements Iterator<T>{

        private final ObjectInputStream<T> in;

        public OISItr(@NonNull ObjectInputStream<T> in) {
            this.in = in;
        }

        @Override
        public boolean hasNext() {
            return in.hasMore();
        }

        @Override
        public T next() {
            return in.read();
        }
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new OISItr<>(new ObjectInputStream<T>(copyArray()));
    }
}
