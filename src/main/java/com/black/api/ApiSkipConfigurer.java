package com.black.api;

public interface ApiSkipConfigurer {


    default String[] skips(){
        return null;
    }

}
