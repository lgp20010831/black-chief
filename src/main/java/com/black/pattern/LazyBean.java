package com.black.pattern;

import java.util.function.Supplier;

public class LazyBean {

    private final Supplier<Object> supplier;


    public LazyBean(Supplier<Object> supplier) {
        this.supplier = supplier;
    }

    public Object getBean(){
        return supplier.get();
    }
}
