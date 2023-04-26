package com.black.core.mybatis.source;

import org.apache.ibatis.session.SqlSession;

public interface TransactionIbtaisSessionHandler {

    void commit(boolean force);

    void rollback();

    SqlSession getSqlsession();

    String getAlias();

    void openTransaction();

}
