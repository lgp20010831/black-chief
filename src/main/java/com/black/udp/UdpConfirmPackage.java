package com.black.udp;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class UdpConfirmPackage implements Serializable {

    private final String id;

    public UdpConfirmPackage(String id) {
        this.id = id;
    }


}
