package com.black.io.in;

import lombok.NonNull;

//以流的形式挨个读取字符串里的数据
public class StringBufferInputStream {

    public static StringBufferInputStream create(String text){
        return new StringBufferInputStream(text);
    }

    protected int pos = 0;

    protected final char[] chars;

    public StringBufferInputStream(@NonNull String text){
        this(text.toCharArray());
    }

    public StringBufferInputStream(char[] chars) {
        this.chars = chars;
    }

    private void check(){
        if (pos >= chars.length){
            throw new IllegalStateException("read end");
        }
    }

    public int available(){
        return chars.length - pos;
    }

    public String readAll(){
        check();
        return reads(available());
    }

    public String readSingle(){
        return reads(1);
    }

    public String reads(int size){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(read());
        }
        return builder.toString();
    }

    public char read(){
        check();
        return chars[pos++];
    }

    public int read(@NonNull char[] b){
        return read(b, 0, b.length);
    }

    public int read(char[] b, int off, int len){
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        char c = read();
        b[off] = c;
        int i = 1;
        for (; i < len ; i++) {
            c = read();
            b[off + i] = c;
        }
        return i;
    }

    public int currentPos(){
        return pos;
    }

    public void skip(int size){
        pos += size;
    }

    public void reset(){
        reset(0);
    }

    public void reset(int newpos){
        if (newpos < 0){
            throw new IllegalArgumentException("new pos must >= 0");
        }
        pos = newpos;
    }

    public void close(){
        //do nothing
    }
}
