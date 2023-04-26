package com.black.core.mybatis.source;

import java.util.Collection;

public class ClearCacheWrapper {

    private final Collection<String> aliases;

    private final boolean clear;

    public ClearCacheWrapper(Collection<String> aliases, boolean clear) {
        this.aliases = aliases;
        this.clear = clear;
    }


    public Collection<String> getAliases() {
        return aliases;
    }

    public boolean isClear() {
        return clear;
    }
}
