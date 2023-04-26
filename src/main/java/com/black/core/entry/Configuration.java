package com.black.core.entry;

import java.util.Collection;

public interface Configuration {

    String getItem();

    Object invoke(Object[] args);

    Collection<String> getParamterNameList();
}
