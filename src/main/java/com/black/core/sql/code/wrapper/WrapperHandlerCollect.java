package com.black.core.sql.code.wrapper;

import java.util.Collection;
import java.util.HashSet;

public class WrapperHandlerCollect {

    private final ThreadLocal<Collection<StatementPlusWrapperHandler>> statementHandlerLocal= new ThreadLocal<>();

    private static WrapperHandlerCollect instance;

    private WrapperHandlerCollect(){}

    public static WrapperHandlerCollect getInstance() {
        if (instance == null) instance = new WrapperHandlerCollect();
        return instance;
    }

    public Collection<StatementPlusWrapperHandler> getStatementHandlers(){
        Collection<StatementPlusWrapperHandler> handlers = statementHandlerLocal.get();
        if (handlers == null){
            handlers = new HashSet<>();
            handlers.add(new QueryStatementHandler());
            handlers.add(new UpdateStatementHandler());
            handlers.add(new DeleteStatementHandler());
            statementHandlerLocal.set(handlers);
        }
        return handlers;
    }

}
