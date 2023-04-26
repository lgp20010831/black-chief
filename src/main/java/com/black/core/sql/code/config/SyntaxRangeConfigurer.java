package com.black.core.sql.code.config;

import lombok.Getter;

@Getter @SuppressWarnings("all")
public class SyntaxRangeConfigurer {

    private final SyntaxConfigurer configurer;

    private volatile int validReferences = 1;

    private volatile int references = 0;

    private final String alias;

    public SyntaxRangeConfigurer(SyntaxConfigurer configurer, String alias) {
        this.configurer = configurer;
        this.alias = alias;
    }

    public void setValidReferences(int validReferences) {
        this.validReferences = validReferences;
    }

    public SyntaxRangeConfigurer addReference(){
        references = references + 1;
        return this;
    }

    public boolean referencesOrNot(){
        return validReferences == references;
    }
}
