package com.black.sql_v2.period;

/**
 * @author 李桂鹏
 * @create 2023-05-12 11:51
 */
@SuppressWarnings("all")
public interface Business<T, P> {

    void resolve(SqlPatternProvider.Session<T> session, SqlPatternProvider<T, P> provider) throws Throwable;

}
