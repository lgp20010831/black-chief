package com.black.out_resolve;

import java.io.OutputStream;

public interface OutputStreamResolver {


    boolean support(Object rack);

    void doResolve(OutputStream outputStream, Object rack, Object value) throws Throwable;

}
