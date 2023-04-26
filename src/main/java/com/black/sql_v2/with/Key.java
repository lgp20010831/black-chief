package com.black.sql_v2.with;

/**
 * @author shkstart
 * @create 2023-04-14 14:46
 */
public class Key {

    private final String name;

    public Key(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }


}
