package com.black.xml;

import com.black.core.factory.beans.xml.ElementWrapper;
import com.black.core.factory.beans.xml.XmlWrapper;
import com.black.core.factory.manager.FactoryManager;
import com.black.core.log.IoLog;
import com.black.core.spring.factory.ReusingProxyFactory;
import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.sql.run.RunSqlParser;
import com.black.core.sql.xml.PrepareSource;
import com.black.core.sql.xml.XmlEngine;
import com.black.core.sql.xml.XmlManager;
import com.black.core.util.Assert;
import com.black.sql.Query;
import com.black.sql.QueryResultSetParser;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;
import com.black.standard.XmlSqlOperator;
import lombok.Getter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 李桂鹏
 * @create 2023-05-08 11:11
 */
@SuppressWarnings("all") @Getter
public class XmlExecutor implements XmlSqlOperator {

    private final Map<String, ElementWrapper> bindCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Object> proxyCache = new ConcurrentHashMap<>();

    private final String name;

    private boolean supportFtl = true;

    private final SqlExecutor executor;

    private Set<String> handledXmlPaths = new HashSet<>();

    private final IoLog log;

    public XmlExecutor(String name) {
        this.name = name;
        executor = Sql.opt(name);
        log = executor.getEnvironment().getLog();
        XmlManager.init();
    }

    public Connection getConnection(){
        return executor.getConnection();
    }

    public void closeConnection(){
        executor.closeConnection();
    }

    public void bind(String id, ElementWrapper elementWrapper){
        bindCache.put(id, elementWrapper);
    }

    public boolean exist(String id){
        return bindCache.containsKey(id);
    }

    @Override
    public QueryResultSetParser selectXml(String id, Object... params) {
        return select(id, params);
    }

    @Override
    public void updateXml(String id, Object... params) {
        update(id, params);
    }

    public void scanAndParse(String... classPaths){
        List<XmlWrapper> wrappers = new ArrayList<>();
        for (String classPath : classPaths) {
            if (handledXmlPaths.contains(classPath)){
                continue;
            }
            try {
                List<XmlWrapper> xmlResources = XmlUtils.getXmlResources(classPath);
                if (isSupportFtl()){
                    List<XmlWrapper> ftlResources = XmlUtils.getFtlResources(classPath);
                    wrappers.addAll(ftlResources);
                }
                wrappers.addAll(xmlResources);
            }finally {
                handledXmlPaths.add(classPath);
            }
        }
        wrappers.forEach(this::parseXmlWrapper);
    }

    private void parseXmlWrapper(XmlWrapper wrapper){
        ElementWrapper rootElement = wrapper.getRootElement();
        List<ElementWrapper> selects = rootElement.getsByName("select");
        selects.forEach(this::parseElementWrapper);
        List<ElementWrapper> updates = rootElement.getsByName("update");
        updates.forEach(this::parseElementWrapper);
    }

    private void parseElementWrapper(ElementWrapper elementWrapper){
        String id = elementWrapper.getAttrVal("id");
        if (exist(id)){
            throw new IllegalStateException("xml id is it already exists: " + id);
        }
        bind(id, elementWrapper);
    }

    protected PrepareSource prepare(){
        return new PrepareSource(getConnection(), executor.getEnvironment().getConvertHandler());
    }

    public ElementWrapper findBind(String id){
        ElementWrapper wrapper = bindCache.get(id);
        Assert.notNull(wrapper, "can not find bind xml message: " + id);
        return wrapper;
    }

    public QueryResultSetParser select(String id, Object... params){
        Map<Object, Object> indexMap = XmlUtils.castIndexMap(params);
        Map<String, Object> env = XmlUtils.makeEnv(params);
        String sql = invoke(id, env, indexMap);
        log.info("[XML] invoke query sql ===> {}", sql);
        ResultSet resultSet = SQLUtils.runQuery(sql, getConnection());
        QueryResultSetParser parser = new QueryResultSetParser(resultSet);
        parser.setFinish(() -> closeConnection());
        return parser;
    }

    public void update(String id, Object... params){
        try {
            Map<Object, Object> indexMap = XmlUtils.castIndexMap(params);
            Map<String, Object> env = XmlUtils.makeEnv(params);
            String sql = invoke(id, env, indexMap);
            log.info("[XML] invoke update sql ===> {}", sql);
            SQLUtils.executeSql(sql, getConnection());
        }finally {
            closeConnection();
        }
    }

    protected String invoke(String id, Map<String, Object> env, Map<Object, Object> indexMap){
        ElementWrapper wrapper = findBind(id);
        long start = System.currentTimeMillis();
        //获取预处理 sql
        String sql = XmlEngine.prepareSql(wrapper, env, prepare());

        //处理 ${}
        sql = GlobalMapping.parseAndObtain(sql);

        //处理 ?1, ?2
        sql = Query.doParseSql(sql, indexMap);

        //处理 #{}  ^{} ?[]
        sql = RunSqlParser.parseSql(sql, env);

        //压缩 sql
        sql = XmlUtils.compressSql(sql);
        log.trace("[XML] --> xml resolve sql take: {} ms", System.currentTimeMillis() - start);
        return sql;
    }

    protected boolean isSelect(String id){
        ElementWrapper wrapper = findBind(id);
        return "select".equalsIgnoreCase(wrapper.getName());
    }

    public <T> T getMapper(Class<T> type){
        return (T) proxyCache.computeIfAbsent(type, t -> createProxy(t));
    }

    private <T> T createProxy(Class<T> type){
        ReusingProxyFactory reusingProxyFactory = FactoryManager.initAndGetProxyFactory();
        return reusingProxyFactory.proxy(type, new XmlProxyInvokeHandler(this));
    }
}
