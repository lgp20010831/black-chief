package com.black.thread;

import com.black.core.sql.code.TransactionHandler;
import com.black.core.sql.code.log.Log;
import com.black.core.sql.code.log.Log4jSqlLog;
import com.black.core.util.Assert;
import com.black.function.Supplier;
import lombok.NonNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;


/**
 * @author 李桂鹏
 * @create 2023-05-24 16:12
 */
@SuppressWarnings("all")
public class MultithreadedTransactions extends AsyncThreadHandler{

    //每个线程维护的连接供应商
    private final LinkedBlockingQueue<TransactionHandler> mulitConnectionQueue = new LinkedBlockingQueue<>();

    private final Supplier<Connection>[] suppliers;

    private Log log;

    private volatile boolean successful = true;

    private Function<Connection, TransactionHandler> createHandlerFunction;

    public MultithreadedTransactions(@NonNull Supplier<Connection>... suppliers) {
        this.suppliers = suppliers;
        log = new Log4jSqlLog();
    }

    public MultithreadedTransactions customCreateHandler(Function<Connection, TransactionHandler> createHandlerFunction){
        this.createHandlerFunction = createHandlerFunction;
        return this;
    }

    public MultithreadedTransactions log(Log log){
        this.log = log;
        return this;
    }

    @Override
    protected boolean beforeRun() {

        if (!successful){
            return false;
        }

        for (Supplier<Connection> supplier : suppliers) {
            try {
                openAndRegisterConnection(supplier);
            }catch (RuntimeException e){
                successful = false;
                throw e;
            }
        }
        return true;
    }

    protected void openAndRegisterConnection(Supplier<Connection> supplier){
        Connection connection;
        try {
            connection = supplier.get();
        } catch (Throwable e) {
            throw new IllegalStateException("can not open connection", e);
        }
        if (connection == null){
            throw new IllegalStateException("can not obtain connection");
        }
        TransactionHandler transactionHandler;
        if (createHandlerFunction != null){
            transactionHandler = createHandlerFunction.apply(connection);
            Assert.notNull(transactionHandler, "create transactionHandler is null");
        }else {
            transactionHandler = new BaseTransactionHandler(connection, log);
        }

        if (transactionHandler.isOpen()){
            //Currently in the included transaction
            return;
        }else {
            try {
                mulitConnectionQueue.put(transactionHandler);
            } catch (InterruptedException e) {
                transactionHandler.close();
                throw new IllegalStateException("Unable to list the current " +
                        "connection transaction processor");
            }
            try {
                transactionHandler.open();
            } catch (SQLException e) {
                transactionHandler.close();
                throw new IllegalStateException("Unable to open transaction", e);
            }
        }
    }

    @Override
    protected void postTaskFail(Throwable ex) {
        successful = false;
    }

    protected void finish(){
        if (!successful){
            rollback();
        }
        commit();
        close();
    }

    @Override
    protected void postLatchFinish() {
        finish();
    }

    @Override
    protected void postLatchIntercept() {
        successful = false;
        finish();
    }

    @Override
    protected void postCyclicThrowable(Throwable e) {
        successful = false;
        finish();
    }

    @Override
    protected void postCyclicFinish() {
        finish();
    }

    protected void rollback(){
        for (TransactionHandler transactionHandler : mulitConnectionQueue) {
            transactionHandler.rollback();
        }
    }

    protected void commit(){
        for (TransactionHandler transactionHandler : mulitConnectionQueue) {
            transactionHandler.commit();
        }
    }

    protected void close(){
        for (TransactionHandler transactionHandler : mulitConnectionQueue) {
            transactionHandler.close();
        }
        mulitConnectionQueue.clear();
    }
}
