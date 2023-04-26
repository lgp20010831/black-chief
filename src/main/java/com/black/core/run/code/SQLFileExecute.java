package com.black.core.run.code;

import java.io.InputStream;
import java.sql.Connection;

public interface SQLFileExecute {

    void execute(Configuration configuration, InputStream in, Connection connection);

    void close(Configuration configuration);
}
