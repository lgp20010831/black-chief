package com.black.pattern;

import java.io.IOException;
import java.io.OutputStream;

public class BytePipeline extends Pipeline<ByteNode, Byte, Byte>{

    OutputStream out;

    public BytePipeline(OutputStream out){
        super(new ByteNode(){
            @Override
            public void tailfireRunnable(PipeNode<Byte, Byte> current, Byte arg) {
                if (out != null){
                    try {
                        out.write(arg);
                    } catch (IOException e) {
                        throw new PipelinesException(e);
                    }
                }
                super.tailfireRunnable(current, arg);
            }
        }, new ByteNode(){
            @Override
            public void headfireRunnable(PipeNode<Byte, Byte> current, Byte arg) {
                if (out != null){
                    try {
                        out.write(arg);
                    } catch (IOException e) {
                        throw new PipelinesException(e);
                    }
                }
                super.headfireRunnable(current, arg);
            }
        });
        this.out = out;
    }

    public void write(int b){
        headfire((byte) b);
    }

    public void write(byte[] b){
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len){
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

    public void flush() {
        if (out != null){
            try {
                out.flush();
            } catch (IOException e) {
                throw new PipelinesException(e);
            }
        }
    }

    public void close(){
        if (out != null){
            try {
                out.close();
            } catch (IOException e) {
                throw new PipelinesException(e);
            }
        }
    }

}
