package com.black.sql_v2.serialize;

public interface SqlSerialize {

    String toSerialize();

    Object deserialize(String text);
}
