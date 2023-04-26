package com.black.core.run.code;

import com.black.core.util.CentralizedExceptionHandling;
import com.black.core.sql.SQLSException;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

public class IbtaisSqlExecute implements SQLFileExecute{

    private ScriptRunner runner;

    @Override
    public void execute(Configuration configuration, InputStream in, Connection connection) {
        if (runner == null){
            try {
                runner = new ScriptRunner(connection);
                runner.setStopOnError(configuration.stopOnSentenceError);
            } catch (Throwable e) {
                CentralizedExceptionHandling.handlerException(e);
                throw new SQLSException("error get connection", e);
            }
        }

        runner.runScript(new InputStreamReader(in));
    }

    @Override
    public void close(Configuration configuration) {
        runner.closeConnection();
    }
}
