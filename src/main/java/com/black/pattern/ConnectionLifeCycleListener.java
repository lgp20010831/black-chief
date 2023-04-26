package com.black.pattern;

import java.sql.Connection;

public interface ConnectionLifeCycleListener {

    void abandonConnection(String alias, Connection connection);

    void createNewConnection(String alias, Connection connection);
}
