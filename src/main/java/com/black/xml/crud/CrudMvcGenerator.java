package com.black.xml.crud;

import com.black.xml.servlet.MvcGenerator;

/**
 * @author 李桂鹏
 * @create 2023-06-02 10:47
 */
@SuppressWarnings("all")
public class CrudMvcGenerator {

    private final CrudGeneratorConfiguration configuration;

    private final MvcGenerator mvcGenerator;

    public CrudMvcGenerator(CrudGeneratorConfiguration configuration, MvcGenerator mvcGenerator) {
        this.configuration = configuration;
        this.mvcGenerator = mvcGenerator;
    }

    public CrudGeneratorConfiguration getConfiguration() {
        return configuration;
    }

    public MvcGenerator getMvcGenerator() {
        return mvcGenerator;
    }

    public void generator(){

    }
}
