package com.black.netty;

public interface SessionFactory<S extends Session> {

    /**
     * Open a session, and the specific type
     * of the session is defined by the implementation class
     * @return session
     */
    S openSession();

    /**
     * get global configuration
     * @return config
     */
    Configuration getConfiguration();
}
