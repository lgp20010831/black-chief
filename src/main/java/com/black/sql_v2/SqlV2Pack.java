package com.black.sql_v2;

import com.black.utils.IdUtils;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.util.Map;

@Getter @Setter
public class SqlV2Pack {

    private final String passID;

    private Connection connection;

    private Map<String, Object> env;

    private SqlExecutor executor;

    private Object attachment;

    private Object[] params;

    private Environment environment;

    public SqlV2Pack(){
        passID = IdUtils.createShort8Id();
    }
}
