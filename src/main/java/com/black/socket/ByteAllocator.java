package com.black.socket;

public class ByteAllocator {

    public static final byte[] EMPTY_BUFFER = new byte[0];

    public static byte[] alloc(int size){
        if (size < 0){
            throw new IllegalArgumentException("buffer alloc size < 0");
        }

        if (size == 0){
            return EMPTY_BUFFER;
        }

        return new byte[size];
    }

}
