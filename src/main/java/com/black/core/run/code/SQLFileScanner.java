package com.black.core.run.code;

import java.io.InputStream;

public interface SQLFileScanner {

    InputStream getInputStream(Configuration configuration, String path);

}
