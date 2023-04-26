package com.black.udp;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class StablePackage implements Serializable {

    private final String id;

    private final byte[] bytes;

    private long savePoint;

    public StablePackage(byte[] bytes) {
        this.bytes = bytes;
        id = UUID.randomUUID().toString();
        savePoint = System.currentTimeMillis();
    }

    public void setSavePoint(long savePoint) {
        this.savePoint = savePoint;
    }
}
