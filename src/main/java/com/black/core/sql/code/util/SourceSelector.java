package com.black.core.sql.code.util;

import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.sup.SqlSequencesFactory;
import com.black.core.sql.unc.OperationType;
import com.black.sql.SqlOutStatement;
import com.black.sql.SqlWriter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class SourceSelector {

    final String alias;

    AliasColumnConvertHandler convertHandler = new HumpColumnConvertHandler();

    public SourceSelector(String alias) {
        this.alias = alias;
    }

    public SourceSelector convert(AliasColumnConvertHandler handler){
        if (handler != null){
            convertHandler = handler;
        }
        return this;
    }

    public Map<String, Object> selectSingle(String name, Map<String, Object> conditionMap, String... sqls){
        List<Map<String, Object>> maps = select(name, conditionMap, sqls);
        return maps == null ? null : maps.isEmpty() ? null : maps.get(0);
    }

    public List<Map<String, Object>> select(String name, Map<String, Object> conditionMap, String... sqls){
        Connection connection = ConnectionManagement.getConnection(alias);
        SqlOutStatement statement = getStatement(name, false);
        if (conditionMap != null){
            for (String alias : conditionMap.keySet()) {
                statement.writeEq(convertHandler.convertColumn(alias), SQLUtils.getString(conditionMap.get(alias)));
            }
        }
        Map<String, Object> map = new HashMap<>();
        for (String sql : sqls) {
            SqlSequencesFactory.parseSeq(statement, sql, OperationType.SELECT, map);
        }

        statement.flush();
        String sql = statement.toString();
        if (log.isInfoEnabled()) {
            log.info("==> selector select sql: [{}]", AnsiOutput.toString(AnsiColor.GREEN, sql));
        }
        try {

            return SQLUtils.runJavaSelect(sql, connection, convertHandler);
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
    }

    private SqlOutStatement getStatement(String tableName, boolean count){
        SqlOutStatement statement;
        if (count){
            statement = SqlWriter.selectCount(tableName);
        }else {
            statement = SqlWriter.select(tableName);
        }
        return statement;
    }
}
