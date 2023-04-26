package com.black.core.api.handler;

import java.util.Collection;
import java.util.function.Predicate;

public interface ExampleStreamAdapter {

    Collection<String> getExampleMap();

    ExampleStreamAdapter addParam(String param);

    ExampleStreamAdapter addParam(String param, String type);

    ExampleStreamAdapter addListParam(String param);

    ExampleStreamAdapter addAttributeParam(String param, Class<?>... pojoClass);

    ExampleStreamAdapter addAttributeListParam(String param, Class<?>... pojoClass);

    ExampleStreamAdapter removeParam(String param);

    ExampleStreamAdapter removeIf(Predicate<? super String> filter);

    ExampleStreamAdapter rename(String oldParam, String newParam);

    String rewrite();

    void clear();
}
