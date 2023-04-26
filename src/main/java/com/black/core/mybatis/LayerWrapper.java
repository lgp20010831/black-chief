package com.black.core.mybatis;

import java.util.Collection;

public interface LayerWrapper {

    Collection<String> aliases();

    IbatisIntercept getTarget();
}
