package com.black.http;

import java.io.IOException;

public interface HttpTransitExecutor {

    boolean support(AimType aimType);

    byte[] send(byte[] request, Configuration configuration) throws IOException;
}
