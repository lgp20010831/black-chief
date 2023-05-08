package com.black.result_set;

import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.session.SQLMethodType;
import com.black.core.sql.xml.PrepareSource;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 李桂鹏
 * @create 2023-05-08 10:16
 */
@SuppressWarnings("all")
public interface ResultSetHandler {


    boolean support(SQLMethodType type, MethodWrapper mw);

    Object resolve(ResultSet resultSet, Class<?> returnType, MethodWrapper mw,
                   PrepareSource prepareSource, SQLMethodType methodType) throws SQLException;

}
