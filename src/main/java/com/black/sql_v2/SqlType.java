package com.black.sql_v2;

public enum SqlType {

    INSERT("insert"), WHERE("where"), SET("set"), INSERT_SET("insert_set");

    String name;

    SqlType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
