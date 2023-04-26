package com.black.ods;

import java.sql.SQLException;

public interface JdbcActuator {

    OdsExecuteResult execute(OdsExecuteResult result, OdsChain chain) throws SQLException;

}
