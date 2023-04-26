package com.black.core.spring.pureness;

import com.black.core.spring.ChiefApplicationConfigurer;

public class DefaultChiefApplicationConfigurer implements ChiefApplicationConfigurer {

    @Override
    public String[] scanPackages() {
        return new String[]{"com.example.springautothymeleaf"};
    }

}
