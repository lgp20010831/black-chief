package com.black.core.chain;

public interface Order {

    default int getOrder(){
        return 0;
    }
}
