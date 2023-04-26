package com.black.core.sql.code.mapping;

import com.black.core.sql.code.config.GlobalSQLConfiguration;
import com.black.core.sql.code.config.SyntaxConfigurer;
import com.black.table.TableMetadata;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Mapper {

    String getTableName();
    //=====================================================================================
    //                                  查询
    //=====================================================================================
    Map<String, Object> findById(Object id);

    Map<String, Object> find(Map<String, Object> condition);

    List<Map<String, Object>> findAll();

    List<Map<String, Object>> findAll(Map<String, Object> condition);

    List<Map<String, Object>> findAllById(Iterable<Object> ids);

    boolean existsById(Object id);

    int count();

    int count(Map<String, Object> map);
    //=====================================================================================
    //                                  更新
    //=====================================================================================
    Map<String, Object> save(Map<String, Object> e);

    List<Map<String, Object>> saveBatch(Collection<Map<String, Object>> collection);
    //=====================================================================================
    //                                  删除
    //=====================================================================================
    void deleteById(Object var1);

    void delete(Map<String, Object> condition);

    void deleteAll(Collection<Map<String, Object>> collection);

    void deleteAll();

    //=====================================================================================
    //                                  配置
    //=====================================================================================
    void configureSyntax(SyntaxConfigurer syntaxConfigurer, int validReferences);

    GlobalSQLConfiguration getConfiguration();

    Connection getConnection();

    GlobalParentMapping getParent();

    TableMetadata getMetadata();

    String getAlias();

    String showTableInfo();

    //=====================================================================================
    //                                  全局
    //=====================================================================================

    Map<String, Object> globalSelectSingle(Map<String, Object> map);

    Map<String, Object> globalSelectSingle(Map<String, Object> map, String blendString);

    List<Map<String, Object>> globalSelect(Map<String, Object> map);

    List<Map<String, Object>> globalSelect(Map<String, Object> map, String blendString);

    Map<String, Object> globalDictSelectSingle(Map<String, Object> map, String... dictionary);

    Map<String, Object> globalDictSelectSingle(Map<String, Object> map, String blendString, String... dictionary);

    List<Map<String, Object>> globalDictSelect(Map<String, Object> map, String... dictionary);

    List<Map<String, Object>> globalDictSelect(Map<String, Object> map, String blendString, String... dictionary);

    Map<String, Object> globalSyntaxSelectSingle(Map<String, Object> map, SyntaxConfigurer configurer);

    List<Map<String, Object>> globalSyntaxSelect(Map<String, Object> map, SyntaxConfigurer configurer);
}



