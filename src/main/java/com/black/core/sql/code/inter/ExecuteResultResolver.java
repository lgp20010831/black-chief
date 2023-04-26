package com.black.core.sql.code.inter;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.config.Configuration;
import com.black.core.sql.code.pattern.ExecuteBody;
import com.black.core.sql.code.session.SQLMethodType;

import java.sql.SQLException;

public interface ExecuteResultResolver {

    boolean support(SQLMethodType type, MethodWrapper mw);

    Object doResolver(ExecuteBody body, Configuration configuration, MethodWrapper mw, boolean skip) throws SQLException;
}
