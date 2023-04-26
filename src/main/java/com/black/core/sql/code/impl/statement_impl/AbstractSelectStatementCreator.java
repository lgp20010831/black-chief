package com.black.core.sql.code.impl.statement_impl;

import com.black.core.sql.annotation.SelectCount;
import com.black.core.sql.code.config.Configuration;

import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.config.SyntaxManager;
import com.black.core.util.Utils;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;

import java.util.List;

public abstract class AbstractSelectStatementCreator {

    public static SqlOutStatement doCreateSelectStatement(Configuration configuration, boolean alias){
        boolean count = configuration.getMethodWrapper().hasAnnotation(SelectCount.class);
        SqlOutStatement statement;
        if (count){
            statement = SqlWriter.selectCount(configuration.getTableName(), alias);
        }else {
            String[] rt = null;
            List<String> localSyntax = SyntaxManager.localSyntax(configuration, SyntaxConfigurer::getReturnColumns);
            if (!Utils.isEmpty(localSyntax)){
                rt = localSyntax.toArray(new String[0]);
            }

            if (rt == null){
                rt = configuration.getReturnColumns();
            }
            statement = SqlWriter.select(configuration.getTableName(), alias, rt);
        }
        return statement;
    }

    public SqlOutStatement createSelectStatement(Configuration configuration){
        return doCreateSelectStatement(configuration, false);
    }
}
