package com.black.core.sql.xml;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XmlEngine {


    private static final Map<String, XmlNodeHandler> handlerCache = new ConcurrentHashMap<>();

    public static Map<String, XmlNodeHandler> getHandlerCache() {
        return handlerCache;
    }

    public static void addHandler(String nodeName, XmlNodeHandler nodeHandler){
        handlerCache.put(nodeName, nodeHandler);
    }

    public static XmlNodeHandler getHandler(String nodeName){
        return getHandlerCache().get(nodeName);
    }

    public static String processorSql(ElementWrapper wrapper, Map<String, Object> argMap, PrepareSource prepareSource){
        XmlSqlSource xmlSqlSource = new XmlSqlSource();
        xmlSqlSource.setArgMap(argMap);
        XmlNodeHandler xmlNodeHandler = getHandler(wrapper.getName());
        Assert.notNull(xmlNodeHandler, "unknown xml node is [" + wrapper.getName() + "]");

        xmlNodeHandler.doHandler(xmlSqlSource, wrapper, prepareSource);
        String sql = xmlSqlSource.getSql();

        //handler ${}
        sql = GlobalMapping.parseAndObtain(sql);

        //handler #{}
        return MapArgHandler.parseSql(sql, argMap);
    }
}
