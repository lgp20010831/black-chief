package com.black.core.sql.code.sqls;

import com.black.core.sql.SQLSException;
import com.black.core.sql.code.util.SQLUtils;
import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class GeneratedKeysResultSetHandler implements ResultTypeHandler{
    @Override
    public boolean support(ResultType resultType) {
        return resultType == ResultType.GeneratedKeys;
    }

    @Override
    public List<String> resolver(ResultSet resultSet) {
        List<String> list = new ArrayList<>();
        try {
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
        }catch (SQLException e){
            throw new SQLSException(e);
        }finally {
            SQLUtils.closeResultSet(resultSet);
        }
        return list;
    }
}
