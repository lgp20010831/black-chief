package com.black.ods;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public interface OdsChain {

    DataSource getDataSource();

    void setDataSource(DataSource dataSource);

    OdsUndertake head();

    void setHead(OdsUndertake undertake);

    boolean isOpenTransactional();

    void setTransactional(boolean open);

    OdsConnectionManager getConnectionManager();

    default void execute(){
        execute(null);
    }

    void execute(List<Map<String, Object>> initData);
}
