package com.black.core.log;

import java.io.IOException;


public class LogDome {

    public static final IoLog log = LogFactory.getArrayLog();

    public static void main(String[] args) throws IOException, InterruptedException {

        log.info("open session of {}", "master");
        Thread.sleep(1000);
        log.debug("==> commit transaction: [{}]", "master");

    }

}
