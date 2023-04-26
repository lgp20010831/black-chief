package com.black.nio.group;

public interface GioChainResolver extends GioResolver{


    default void fire(){
        throw new IllegalStateException("do not realization");
    }

}
