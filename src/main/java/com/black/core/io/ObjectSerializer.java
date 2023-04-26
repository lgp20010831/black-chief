package com.black.core.io;

public interface ObjectSerializer extends ObjectWriter, ObjectReader {

    <T> T copyObject(T obj);
}
