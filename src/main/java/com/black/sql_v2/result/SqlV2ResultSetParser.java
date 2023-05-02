package com.black.sql_v2.result;

import com.alibaba.fastjson.JSONObject;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StreamUtils;
import com.black.sql.QueryResultSetParser;
import com.black.sql.SqlOutStatement;
import com.black.sql_v2.SqlExecutor;
import com.black.sql_v2.SqlV2Pack;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Getter @Setter @SuppressWarnings("all")
public class SqlV2ResultSetParser extends QueryResultSetParser {

    private final SqlV2Pack v2Pack;

    private SqlOutStatement statement;

    public SqlV2ResultSetParser(ResultSet resultSet, SqlV2Pack v2Pack) {
        super(resultSet);
        this.v2Pack = v2Pack;
    }

    @Override
    public List<Map<String, Object>> list() {
        try {
            List<Map<String, Object>> mapList = SQLUtils.parseJavaResult(resultSet, convertHandler);
            handlerResult(mapList);
            return mapList;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            end();
        }
    }

    @Override
    public List<JSONObject> jsonList() {
        try {
            List<JSONObject> jsonObjects =  SQLUtils.parseJavaJsonResult(resultSet, convertHandler);
            handlerResult(jsonObjects);
            return jsonObjects;
        } catch (SQLException e) {
            throw new SQLSException(e);
        } finally {
            end();
        }
    }

    @Override
    public <T> List<T> javaList(Class<T> type) {
        List<JSONObject> list = jsonList();
        return StreamUtils.mapList(list, json -> v2Pack.getExecutor().deserialize(json, type));
    }

    protected <T extends Map<String, Object>> void handlerResult(List<T> resultList){
        SqlExecutor executor = v2Pack.getExecutor();
        executor.handlerResult((List<Map<String, Object>>) resultList, statement, v2Pack);
    }
}
