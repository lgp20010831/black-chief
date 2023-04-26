package com.black.core.io.bio;

import com.black.netty.Session;

public interface BioSession extends Session {

    Connection getConnection();

    void restart();

    boolean isVaild();
}
