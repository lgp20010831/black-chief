package com.black.core.sql.code.mapping;

import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.table.jdbc.CatalogConnection;

import java.util.List;
import java.util.Map;

public class GlobalParentImpl implements GlobalParentMapping{

    private final GlobalParentMapping delegate;

    public GlobalParentImpl(GlobalParentMapping delegate) {
        this.delegate = delegate;
    }

    @Override
    public int count(String name, Map<String, Object> map) {
        return delegate.count(name, map);
    }

    @Override
    public List<Map<String, Object>> globalSelect(String name, Map<String, Object> map, String blendString) {
        return delegate.globalSelect(name, map, blendString);
    }

    @Override
    public List<Map<String, Object>> globalDictSelect(String name, Map<String, Object> map, String blendString, String... dictionary) {
        return delegate.globalDictSelect(name, map, blendString, dictionary);
    }

    @Override
    public List<Map<String, Object>> globalSyntaxSelect(String name, Map<String, Object> map, SyntaxConfigurer configurer) {
        return delegate.globalSyntaxSelect(name, map, configurer);
    }

    @Override
    public String globalInsertSingle(String name, Map<String, Object> map) {
        return delegate.globalInsertSingle(name, map);
    }

    @Override
    public List<String> globalInsertBatch(String name, List<Map<String, Object>> list) {
        return delegate.globalInsertBatch(name, list);
    }

    @Override
    public List<String> UnsupportPlatformInsertBatch(String name, List<Map<String, Object>> list) {
        return delegate.UnsupportPlatformInsertBatch(name, list);
    }

    @Override
    public Boolean fastJoin(String name, List<Map<String, Object>> list) {
        return delegate.fastJoin(name, list);
    }

    @Override
    public List<String> saveOrUpdate(String name, List<Map<String, Object>> list) {
        return delegate.saveOrUpdate(name, list);
    }

    @Override
    public String globalUpdate(String name, Map<String, Object> setMap, Map<String, Object> condition, String blend) {
        return delegate.globalUpdate(name, setMap, condition, blend);
    }

    @Override
    public String updateOrSave(String name, Map<String, Object> setMap, Map<String, Object> condition) {
        return delegate.updateOrSave(name, setMap, condition);
    }

    @Override
    public boolean globalDelete(String name, Map<String, Object> map, String blend) {
        return delegate.globalDelete(name, map, blend);
    }

    @Override
    public void refresh() {
        delegate.refresh();
    }

    @Override
    public CatalogConnection getConnection() {
        return delegate.getConnection();
    }

    @Override
    public Object doNothing() {
        return delegate.doNothing();
    }

    @Override
    public String getAlias() {
        return delegate.getAlias();
    }

    @Override
    public String showTableInfo(String name) {
        return delegate.showTableInfo(name);
    }

    @Override
    public GlobalSQLConfiguration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public List<Map<String, Object>> executeSelectSql(String sql, Map<String, Object> condition) {
        return delegate.executeSelectSql(sql, condition);
    }
}
