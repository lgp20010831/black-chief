package com.black.core.sql.action;

import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.code.SyntaxFactory;
import com.black.core.sql.code.cascade.CascadeGroup;
import com.black.core.sql.code.cascade.Model;
import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.core.sql.code.mapping.GlobalParentImpl;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.Assert;
import com.black.table.jdbc.CatalogConnection;

import java.util.List;
import java.util.Map;

public abstract class AbstractCRUDBaseController<M extends GlobalParentMapping> extends DictDynamicController<M>
        implements GlobalParentMapping{

    private GlobalParentImpl impl;

    public AbstractCRUDBaseController(){

    }

    public SyntaxFactory.SyntaxExecutor build(String name){
        return SyntaxFactory.create(getMapper()).name(name);
    }

    public GlobalParentImpl getImpl() {
        if (impl == null){
            impl = new GlobalParentImpl(getMapper());
        }
        return impl;
    }

    protected MethodWrapper getImplReflexMethod(String methodName, int paramCount){
        ClassWrapper<GlobalParentMapping> classWrapper = ClassWrapper.get(GlobalParentMapping.class);
        MethodWrapper methodWrapper = classWrapper.getMethod(methodName, paramCount);
        Assert.notNull(methodWrapper, "unknown method: " + methodName + ", with param count: " + paramCount);
        return methodWrapper;
    }

    private Object reflexInvokeImplMethod(MethodWrapper methodWrapper, Object... args){
        GlobalParentImpl impl = getImpl();
        return methodWrapper.invoke(impl, args);
    }

    public SyntaxFactory.SyntaxExecutor factory(){
        return factory(null);
    }

    public SyntaxFactory.SyntaxExecutor factory(Map<String, Object> condition){
        return factory(condition, getTableName());
    }

    public SyntaxFactory.SyntaxExecutor factory(Map<String, Object> condition, String name){
        return SyntaxFactory.create(getMapper()).condition(condition).name(name);
    }

    public CascadeGroup group(Model model){
        return group(null, model);
    }

    public CascadeGroup group(Map<String, Object> condition, Model model){
        return group(condition, getTableName(), model);
    }

    public CascadeGroup group(Map<String, Object> condition, String name, Model model){
        SyntaxFactory.SyntaxExecutor executor = factory(condition, name);
        CascadeGroup group = new CascadeGroup(model);
        return group.pointParent(executor);
    }

    @Override
    public int count(String name, Map<String, Object> map) {
        return (int) reflexInvokeImplMethod(getImplReflexMethod("count", 2), name, map);
    }

    @Override
    public List<Map<String, Object>> globalSelect(String name, Map<String, Object> map, String blendString) {
        return (List<Map<String, Object>>) reflexInvokeImplMethod(getImplReflexMethod("globalSelect", 3), name, map, blendString);
    }

    @Override
    public List<Map<String, Object>> globalDictSelect(String name, Map<String, Object> map, String blendString, String... dictionary) {
        return (List<Map<String, Object>>) reflexInvokeImplMethod(getImplReflexMethod("globalDictSelect", 4), name, map, blendString, dictionary);
    }

    @Override
    public List<Map<String, Object>> globalSyntaxSelect(String name, Map<String, Object> map, SyntaxConfigurer configurer) {
        return (List<Map<String, Object>>) reflexInvokeImplMethod(getImplReflexMethod("globalSyntaxSelect", 3), name, map, configurer);
    }

    @Override
    public String globalInsertSingle(String name, Map<String, Object> map) {
        return (String) reflexInvokeImplMethod(getImplReflexMethod("globalInsertSingle", 2), name, map);
    }

    @Override
    public List<String> globalInsertBatch(String name, List<Map<String, Object>> list) {
        return (List<String>) reflexInvokeImplMethod(getImplReflexMethod("globalInsertBatch", 2), name, list);
    }

    @Override
    public List<String> UnsupportPlatformInsertBatch(String name, List<Map<String, Object>> list) {
        return (List<String>) reflexInvokeImplMethod(getImplReflexMethod("UnsupportPlatformInsertBatch", 2), name, list);
    }

    @Override
    public Boolean fastJoin(String name, List<Map<String, Object>> list) {
        return (Boolean) reflexInvokeImplMethod(getImplReflexMethod("fastJoin", 2), name, list);
    }

    @Override
    public List<String> saveOrUpdate(String name, List<Map<String, Object>> list) {
        return (List<String>) reflexInvokeImplMethod(getImplReflexMethod("saveOrUpdate", 2), name, list);
    }

    @Override
    public String globalUpdate(String name, Map<String, Object> setMap, Map<String, Object> condition, String blend) {
        return (String) reflexInvokeImplMethod(getImplReflexMethod("globalUpdate", 4), name, setMap, condition, blend);
    }

    @Override
    public String updateOrSave(String name, Map<String, Object> setMap, Map<String, Object> condition) {
        return (String) reflexInvokeImplMethod(getImplReflexMethod("updateOrSave", 3), name, setMap, condition);
    }

    @Override
    public boolean globalDelete(String name, Map<String, Object> map, String blend) {
        return (boolean) reflexInvokeImplMethod(getImplReflexMethod("globalDelete", 3), name, map, blend);
    }

    @Override
    public void refresh() {
        reflexInvokeImplMethod(getImplReflexMethod("refresh", 0));
    }

    @Override
    public CatalogConnection getConnection() {
        return (CatalogConnection) reflexInvokeImplMethod(getImplReflexMethod("getConnection", 0));
    }

    @Override
    public Object doNothing() {
        return reflexInvokeImplMethod(getImplReflexMethod("doNothing", 0));
    }

    @Override
    public String getAlias() {
        return (String) reflexInvokeImplMethod(getImplReflexMethod("getAlias", 0));
    }

    @Override
    public String showTableInfo(String name) {
        return (String) reflexInvokeImplMethod(getImplReflexMethod("showTableInfo", 3), name);
    }

    @Override
    public GlobalSQLConfiguration getConfiguration() {
        return (GlobalSQLConfiguration) reflexInvokeImplMethod(getImplReflexMethod("getConfiguration", 0));
    }

    @Override
    public List<Map<String, Object>> executeSelectSql(String sql, Map<String, Object> condition) {
        return (List<Map<String, Object>>) reflexInvokeImplMethod(getImplReflexMethod("executeSelectSql", 2), sql, condition);
    }
}
