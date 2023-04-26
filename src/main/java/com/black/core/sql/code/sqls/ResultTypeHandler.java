package com.black.core.sql.code.sqls;

import java.sql.ResultSet;

public interface ResultTypeHandler {

    boolean support(ResultType resultType);

    Object resolver(ResultSet resultSet);
}
