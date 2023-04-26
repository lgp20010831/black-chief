package com.black.core.sql.lock;

public enum LockType {

    EXCLUSIVE("EXCLUSIVE");

    String type;

    LockType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
