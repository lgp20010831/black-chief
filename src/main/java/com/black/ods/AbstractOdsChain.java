package com.black.ods;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public abstract class AbstractOdsChain implements OdsChain{

    protected DataSource dataSource;

    protected OdsUndertake head;

    protected boolean openTransaction = false;

    protected final OdsConnectionManager connectionManager;

    public AbstractOdsChain() {
        connectionManager = new OdsConnectionManager(this);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public OdsUndertake head() {
        return head;
    }

    @Override
    public void setHead(OdsUndertake undertake) {
        head = undertake;
        head.setChain(this);
    }

    @Override
    public boolean isOpenTransactional() {
        return openTransaction;
    }

    @Override
    public void setTransactional(boolean open) {
        openTransaction = open;
        if (head != null){
            head.setTransactional(open);
        }
    }

    @Override
    public OdsConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public void execute(List<Map<String, Object>> initData) {
        if (head != null){
            try {
                OdsExecuteResult executeResult = new OdsExecuteResult(initData);
                head.execute(executeResult);
            }catch (Throwable ex){
                connectionManager.rollback();
            }finally {
                connectionManager.end();
            }
        }
    }
}
