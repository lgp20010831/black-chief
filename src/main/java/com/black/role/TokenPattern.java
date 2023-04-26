package com.black.role;



import com.black.role.impl.def.DefaultLog;
import com.black.role.impl.def.DefaultTokenCacher;
import com.black.role.impl.def.DefaultTokenCreator;
import com.black.role.impl.def.DefaultTokenResolver;

import java.util.function.Consumer;

public enum TokenPattern {

    DEFAULT(configuration -> {
        configuration.setLog(new DefaultLog());
        configuration.setCacher(new DefaultTokenCacher());
        configuration.setCreator(new DefaultTokenCreator());
        configuration.setResolver(new DefaultTokenResolver());
    });

    Consumer<Configuration> pattern;

    TokenPattern(Consumer<Configuration> pattern) {
        this.pattern = pattern;
    }

    public Consumer<Configuration> getPattern() {
        return pattern;
    }
}
