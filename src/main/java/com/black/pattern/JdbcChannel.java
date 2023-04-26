package com.black.pattern;

import com.black.core.sql.code.session.SqlSession;

import java.io.InputStream;

public class JdbcChannel extends AbstractChannel<SqlSession, String> {

    public JdbcChannel(SqlSession target) {
        super(target);
    }

    @Override
    public void write(String s) {
        getTarget().write(s);
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }
}
