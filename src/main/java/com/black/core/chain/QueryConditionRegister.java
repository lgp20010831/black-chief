package com.black.core.chain;

import com.black.core.spring.factory.AgentLayer;

import java.util.Collection;
import java.util.HashSet;

public class QueryConditionRegister {

    private final CollectedCilent client;

    Collection<ConditionEntry> entries = new HashSet<>();

    public QueryConditionRegister(CollectedCilent client) {
        this.client = client;
    }

    public ConditionEntry begin(){
        DefaultConditionEntry entry = new DefaultConditionEntry(this);
        entries.add(entry);
        return entry;
    }

    public ConditionEntry begin(String alias, Judge judge){
        return begin(alias, false, judge);
    }

    public ConditionEntry begin(String alias, boolean order, Judge judge){
        return begin(alias, order, null, judge);
    }

    public ConditionEntry begin(String alias, boolean order, AgentLayer layer, Judge judge){
        DefaultConditionEntry entry = new DefaultConditionEntry(this);
        entry.setAlias(alias).needOrder(order).condition(judge).proxy(layer);
        entries.add(entry);
        return entry;
    }

    public CollectedCilent getClient() {
        return client;
    }

    public Collection<ConditionEntry> getEntries() {
        return entries;
    }
}
