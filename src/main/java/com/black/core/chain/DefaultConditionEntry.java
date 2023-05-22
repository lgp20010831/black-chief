package com.black.core.chain;

import com.black.bin.InstanceType;
import com.black.core.builder.SortUtil;
import com.black.core.spring.factory.AgentLayer;
import com.black.utils.ProxyUtil;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.*;

public class DefaultConditionEntry implements ConditionEntry, ConditionResultBody{

    String alias;
    Collection<Object> source;
    Judge judge;
    boolean sort = true;
    boolean proxy = false;
    boolean instance = true;
    AgentLayer layer;
    InstanceType instanceType = InstanceType.BEAN_FACTORY_SINGLE;
    private final QueryConditionRegister register;

    public DefaultConditionEntry(QueryConditionRegister register) {
        this.register = register;
    }

    @Override
    public ConditionEntry setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public ConditionEntry condition(Judge judge) {
        this.judge = judge;
        return this;
    }

    @Override
    public Judge getJudge() {
        return judge;
    }

    @Override
    public ConditionEntry needOrder(boolean order) {
        sort = order;
        return this;
    }

    @Override
    public ConditionEntry proxy(AgentLayer layer) {
        proxy = true;
        this.layer = layer;
        return this;
    }

    @Override
    public void setInstanceType(InstanceType instanceType) {
        this.instanceType = instanceType;
    }

    public InstanceType getInstanceType() {
        return instanceType;
    }

    @Override
    public boolean isProxy() {
        return proxy && layer != null;
    }

    @Override
    public AgentLayer getLayer() {
        return layer;
    }

    @Override
    public void instance(boolean instance) {
        this.instance = instance;
    }

    @Override
    public boolean isInstance() {
        return instance;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void registerObject(Object target) {
        if (source == null){
            source = new HashSet<>();
        }
        source.add(target);
    }

    @Override
    public Collection<Object> getCollectSource() {
        if (source == null){
            source = new HashSet<>();
        }
        return sort();
    }

    //order 越小越在前面, 没有 order 的在最后面
    //1. 5. 7. 2 .3
    //      ↓
    //1. 2. 3. 5. 7
    protected Collection<Object> sort(){
        if (!sort){
            return source;
        }
        Map<Integer, Set<Object>> needSortSource = new HashMap<>();
        Set<Object> noNeedSortSource = new HashSet<>();
        for (Object o : source) {
            int order = getOrder(o);
            if (order == -1){
                noNeedSortSource.add(o);
            }else {
                Set<Object> set = needSortSource.computeIfAbsent(order, k -> new HashSet<>());
                set.add(o);
            }
        }
        ArrayList<Integer> integers = SortUtil.sort(new ArrayList<>(needSortSource.keySet()));
        ArrayList<Object> result = new ArrayList<>(integers.size());
        for (Integer integer : integers) {
            result.addAll(needSortSource.get(integer));
        }
        result.addAll(noNeedSortSource);
        return result;
    }


    protected int getOrder(Object o){
        if (o instanceof Order){
            Order order = (Order) o;
            return order.getOrder();
        }
        Class<Object> primordialClass = ProxyUtil.getPrimordialClass(o);
        ChainOrder order = AnnotationUtils.getAnnotation(primordialClass, ChainOrder.class);
        if (order == null){
            return -1;
        }
        return order.value();
    }
}
