package com.black.core.spring.factory;

public interface AgentLayer {

    Object proxy(AgentObject layer) throws Throwable;
}
