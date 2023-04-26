package com.black.ods;

import com.black.core.query.Stack;

import javax.sql.DataSource;
import java.util.List;

public interface OdsUndertake {

    OdsUndertakeConfiguation getConfiguration();

    List<OdsUndertake> lefts();

    List<OdsUndertake> rights();

    void setChain(OdsChain chain);

    int invokeCount();

    Stack<OdsExecuteResult> resultStack();

    OdsExecuteResult newestResult();

    void bindRight(OdsUndertake undertake);

    void bindLeft(OdsUndertake undertake);

    OdsChain getChain();

    void createQueryActuator(String sql);

    void createUpdateActuator(String sql);

    JdbcActuator getActuator();

    DataSource getDataSource();

    void setDataSource(DataSource dataSource);

    boolean isOpenTransactional();

    void setTransactional(boolean open);

    void execute(OdsExecuteResult result);
}
