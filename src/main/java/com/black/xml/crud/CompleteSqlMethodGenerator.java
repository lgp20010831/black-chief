package com.black.xml.crud;

import com.black.xml.servlet.MvcGenerator;

/**
 * @author 李桂鹏
 * @create 2023-06-05 9:55
 */
@SuppressWarnings("all")
public class CompleteSqlMethodGenerator extends AbstractSqlMethodGenerator{


    protected CompleteSqlMethodGenerator(CrudGeneratorConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void generate0(MvcGenerator generator) {
        generatorList(generator);
        generatorQueryById(generator);
        generatorSingle(generator);
        generatorSave(generator);
        generatorSaveBatch(generator);
        generatorDelete(generator);
        generatorDeleteById(generator);
        generatorDeleteByIds(generator);
    }
}
