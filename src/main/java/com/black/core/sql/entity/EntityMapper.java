package com.black.core.sql.entity;

import com.black.core.annotation.Parent;
import com.black.core.sql.code.config.SyntaxConfigurer;

import java.util.Collection;
import java.util.List;

@Parent @SuppressWarnings("all")
public interface EntityMapper<E> {

    //=====================================================================================
    //                                  查询
    //=====================================================================================
    E findById(Object id);

    E find(E condition);

    List<E> findAll();

    List<E> findAll(E condition);

    List<E> findAllById(Iterable<Object> ids);

    boolean existsById(Object id);

    int count();
    //=====================================================================================
    //                                  更新
    //=====================================================================================
    E save(E e);

    List<E> saveBatch(Collection<E> collection);
    //=====================================================================================
    //                                  删除
    //=====================================================================================
    void deleteById(Object var1);

    void delete(E condition);

    void deleteAll(Collection<E> collection);

    void deleteAll();

    //=====================================================================================
    //                                  配置
    //=====================================================================================
    void configureSyntax(SyntaxConfigurer syntaxConfigurer, int validReferences);
}
