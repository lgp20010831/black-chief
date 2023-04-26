package com.black.core.rpc.core;

import com.black.nio.code.Configuration;

import java.util.function.Consumer;

public class NullHook implements Consumer<Configuration> {

    @Override
    public void accept(Configuration configuration) {

    }
}
