package com.black.core.io.bio;


import lombok.Getter;
import lombok.Setter;

//bio configuration
@Setter @Getter
public class Configuration {

    private String ip = "localhost";

    private int port = 8888;

    private int coreSize = 4;

    private int workCoreSize = 2;

    private int alive = 500;
}
