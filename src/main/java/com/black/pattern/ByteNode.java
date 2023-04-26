package com.black.pattern;

public class ByteNode extends PipeNode<Byte, Byte>{


    @Override
    public void headfireRunnable(PipeNode<Byte, Byte> current, Byte arg) {
        super.headfireRunnable(current, arg);
    }

    @Override
    public void tailfireRunnable(PipeNode<Byte, Byte> current, Byte arg) {
        super.tailfireRunnable(current, arg);
    }
}
