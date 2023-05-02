package com.black.sql_v2.serialize;

public interface SerializeAdvocate {

    boolean support(Class<?> type);

    String toSerialize(Object value);

    Object deSerialize(String text, Class<?> type);
}
