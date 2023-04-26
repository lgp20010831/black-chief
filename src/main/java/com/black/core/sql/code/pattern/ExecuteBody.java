package com.black.core.sql.code.pattern;

import com.black.core.sql.code.StatementWrapper;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;

@Getter @Setter
public class ExecuteBody {

    StatementWrapper wrapper;

    ResultSet queryResult;

    int updateCount = -1;

    public ExecuteBody(StatementWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void close(){
        wrapper.close();
    }
}
