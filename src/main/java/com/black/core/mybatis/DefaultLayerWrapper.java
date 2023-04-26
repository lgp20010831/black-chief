package com.black.core.mybatis;

import java.util.Arrays;
import java.util.Collection;

public class DefaultLayerWrapper implements LayerWrapper{


    Collection<String> aliases;

    IbatisIntercept ibatisIntercept;

    public DefaultLayerWrapper(String[] aliases, IbatisIntercept ibatisIntercept) {
        this.aliases = Arrays.asList(aliases);
        this.ibatisIntercept = ibatisIntercept;
    }

    @Override
    public Collection<String> aliases() {
        return aliases;
    }

    @Override
    public IbatisIntercept getTarget() {
        return ibatisIntercept;
    }
}
