package com.black.ods;

import com.black.core.log.CommonLog4jLog;
import com.black.core.log.IoLog;
import com.black.core.query.Stack;
import com.black.core.query.TendsArrayStack;
import com.black.core.sql.SQLSException;
import com.black.core.util.Assert;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractOdsUndertake implements OdsUndertake{

    private static final IoLog log = new CommonLog4jLog();

    protected OdsChain chain;

    protected List<OdsUndertake> lefts;

    protected List<OdsUndertake> rights;

    protected Stack<OdsExecuteResult> resultStack = new TendsArrayStack<>();

    protected volatile OdsExecuteResult newestResult = null;

    protected AtomicInteger invokeCount = new AtomicInteger(0);

    protected DataSource dataSource;

    protected OdsUndertakeConfiguation configuation;

    protected JdbcActuator jdbcActuator;

    protected boolean openTransaction = false;

    public AbstractOdsUndertake() {
        lefts = createList();
        rights = createList();
        configuation = new OdsUndertakeConfiguation();
    }

    @Override
    public void setChain(OdsChain chain) {
        this.chain = chain;
        List<OdsUndertake> rights = rights();
        for (OdsUndertake right : rights) {
            right.setChain(chain);
        }
    }

    @Override
    public JdbcActuator getActuator() {
        return jdbcActuator;
    }

    @Override
    public void createQueryActuator(String sql) {
        jdbcActuator = new QueryJdbcActuator(this, sql);
    }

    @Override
    public void createUpdateActuator(String sql) {
        jdbcActuator = new UpdateJdbcActuator(this, sql);
    }

    @Override
    public OdsUndertakeConfiguation getConfiguration() {
        return configuation;
    }

    protected List<OdsUndertake> createList(){
        return new ArrayList<>();
    }

    @Override
    public List<OdsUndertake> lefts() {
        return lefts;
    }

    @Override
    public List<OdsUndertake> rights() {
        return rights;
    }

    @Override
    public int invokeCount() {
        return invokeCount.get();
    }

    @Override
    public Stack<OdsExecuteResult> resultStack() {
        return resultStack;
    }

    @Override
    public OdsExecuteResult newestResult() {
        return newestResult;
    }

    @Override
    public void bindRight(OdsUndertake undertake) {
        rights.add(undertake);
    }

    @Override
    public void bindLeft(OdsUndertake undertake) {
        lefts.add(undertake);
    }

    @Override
    public OdsChain getChain() {
        return chain;
    }

    @Override
    public DataSource getDataSource() {
        if (dataSource == null){
            return getChain().getDataSource();
        }
        return dataSource;
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean isOpenTransactional() {
        return openTransaction;
    }

    @Override
    public void setTransactional(boolean open) {
        openTransaction = open;
        List<OdsUndertake> lefts = lefts();
        for (OdsUndertake left : lefts) {
            left.setTransactional(open);
        }
    }

    @Override
    public void execute(OdsExecuteResult result) {
        log.info("====> execute undertake: {}", this);
        JdbcActuator actuator = getActuator();
        OdsChain chain = getChain();
        Assert.notNull(actuator, "actuator can not is null");
        OdsExecuteResult execute;
        try {

            execute = actuator.execute(result, chain);
            invokeCount.incrementAndGet();
            resultStack.push(execute);
            newestResult = execute;
        } catch (SQLException e) {
            throw new SQLSException(e);
        }
        List<OdsUndertake> rights = rights();
        for (OdsUndertake right : rights) {
            right.execute(execute);
        }
    }
}
