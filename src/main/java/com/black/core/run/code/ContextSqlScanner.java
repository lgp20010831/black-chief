package com.black.core.run.code;

import java.io.InputStream;

public class ContextSqlScanner implements SQLFileScanner{

    @Override
    public InputStream getInputStream(Configuration configuration, String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}
