package com.black.core.sql.code.inter;

import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.sqls.BoundStatement;

public interface DefaultSqlStatementCreator {

    boolean support(Configuration configuration);

    BoundStatement createStatement(Configuration configuration);
}
