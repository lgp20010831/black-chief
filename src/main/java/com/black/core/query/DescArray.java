package com.black.core.query;

import lombok.NonNull;

public class DescArray<E> extends AscArray<E>{

    public DescArray(@NonNull E[] array) {
        super(array);
    }

    @Override
    public E get(int i) {
        if (i < 0 || i >= array.length){
            throw new IndexOutOfBoundsException("" + i);
        }
        return array[length() - (i + 1)];
    }
}
