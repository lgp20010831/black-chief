package com.black.sql_v2.serialize;

import com.black.bin.InstanceBeanManager;
import com.black.bin.InstanceType;

public class SqlSerializerAdapterAdvocate extends AbstractSerializeAdvocate{


    @Override
    public boolean support(Class<?> type) {
        return SqlSerialize.class.isAssignableFrom(type);
    }

    @Override
    public String toSerialize(Object value) {
        SqlSerialize sqlSerialize = (SqlSerialize) value;
        return sqlSerialize.toSerialize();
    }

    @Override
    public Object deSerialize(String text, Class<?> type) {
        Object instance = InstanceBeanManager.instance(type, InstanceType.REFLEX_AND_BEAN_FACTORY);
        SqlSerialize sqlSerialize = (SqlSerialize) instance;
        return sqlSerialize.deserialize(text);
    }
}
