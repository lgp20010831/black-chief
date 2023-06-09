package com.black.xml.crud;

import com.black.xml.servlet.MvcGenerator;

/**
 * @author 李桂鹏
 * @create 2023-06-02 14:38
 */
@SuppressWarnings("all")
public class SimpleSqlMethodGenerator extends AbstractSqlMethodGenerator{


    protected SimpleSqlMethodGenerator(CrudGeneratorConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void generate0(MvcGenerator generator) {
        generatorList(generator);
        generatorQueryById(generator);
        generatorSave(generator);
        generatorDeleteById(generator);
    }
}
