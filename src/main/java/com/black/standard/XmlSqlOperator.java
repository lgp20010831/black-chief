package com.black.standard;

import com.black.sql.QueryResultSetParser;

@SuppressWarnings("all")
public interface XmlSqlOperator {

    default XmlSqlOperator getSqlXmlDelegate(){
        return null;
    }

    default QueryResultSetParser selectXml(String id, Object... params){
        return getSqlXmlDelegate().selectXml(id, params);
    }

    default void updateXml(String id, Object... params){
        getSqlXmlDelegate().updateXml(id, params);
    }

}
