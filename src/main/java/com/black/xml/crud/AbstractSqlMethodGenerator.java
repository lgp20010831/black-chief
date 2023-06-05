package com.black.xml.crud;

import com.black.core.sql.unc.OperationType;
import com.black.javassist.PartiallyCtClass;
import com.black.sql_v2.action.AbstractSqlOptServlet;
import com.black.xml.servlet.MappingMethodInfo;
import com.black.xml.servlet.MvcGenerator;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author 李桂鹏
 * @create 2023-06-05 9:36
 */
@SuppressWarnings("all")
public abstract class AbstractSqlMethodGenerator implements RequestMethodGenerator{

    private final CrudGeneratorConfiguration configuration;

    protected String swaggerV2Value;

    protected String bodyParam;

    protected String arrayParam;

    protected AbstractSqlMethodGenerator(CrudGeneratorConfiguration configuration) {
        this.configuration = configuration;
        swaggerV2Value = configuration.getTableName() + "{}";
        bodyParam = "!?body@v2Swagger(value:" + swaggerV2Value + ")";
        arrayParam = "!?body::array@v2Swagger(value:" + swaggerV2Value + ")";
    }

    @Override
    public void generate(MvcGenerator generator) {
        generator.setSuperClass(AbstractSqlOptServlet.class);
        generateGetTableName(generator);
        generate0(generator);
    }

    protected void generateGetTableName(MvcGenerator generator){
        String tableName = configuration.getTableName();
        PartiallyCtClass partiallyCtClass = generator.getPartiallyCtClass();
        partiallyCtClass.addMethod("getTableName", String.class, "{return \"" + tableName + "\";}");
    }

    protected abstract void generate0(MvcGenerator generator);

    public void generatorList(MvcGenerator generator){
        if (!configuration.getAllowOperationType().contains(OperationType.SELECT) ||
                configuration.getHiddleMethods().contains("list")){
            return;
        }
        MappingMethodInfo info = createInfo("list", "list", RequestMethod.POST, "查询列表",
                "{\n  return list0($1);\n}",
                true, false, true, swaggerV2Value, bodyParam);
        generator.addRequestMethod(info);
    }

    public void generatorQueryById(MvcGenerator generator){
        if (!configuration.getAllowOperationType().contains(OperationType.SELECT) ||
                configuration.getHiddleMethods().contains("queryById")){
            return;
        }
        MappingMethodInfo info = createInfo("queryById", "queryById", RequestMethod.GET, "根据id查询",
                "{\n  return queryById0($1);\n}",
                false, false, true, swaggerV2Value, "!id");
        generator.addRequestMethod(info);
    }

    public void generatorSingle(MvcGenerator generator){
        if (!configuration.getAllowOperationType().contains(OperationType.SELECT) ||
                configuration.getHiddleMethods().contains("single")){
            return;
        }
        MappingMethodInfo info = createInfo("single", "single", RequestMethod.POST, "查询一条",
                "{\n  return single0($1);\n}",
                false, false, true, swaggerV2Value, bodyParam);
        generator.addRequestMethod(info);
    }


    public void generatorSave(MvcGenerator generator){
        if ((!configuration.getAllowOperationType().contains(OperationType.UPDATE) &&
                !configuration.getAllowOperationType().contains(OperationType.INSERT)) ||
                configuration.getHiddleMethods().contains("save")){
            return;
        }
        MappingMethodInfo info = createInfo("save", "save", RequestMethod.POST, "更新/添加",
                "{\n  save0($1);\n}",
                false, true, false, "", bodyParam);
        generator.addRequestMethod(info);
    }

    public void generatorSaveBatch(MvcGenerator generator){
        if ((!configuration.getAllowOperationType().contains(OperationType.UPDATE) &&
                !configuration.getAllowOperationType().contains(OperationType.INSERT)) ||
                configuration.getHiddleMethods().contains("saveBatch")){
            return;
        }
        MappingMethodInfo info = createInfo("saveBatch", "saveBatch", RequestMethod.POST, "批次-更新/添加",
                "{\n  saveBatch0($1);\n}",
                false, true, false, "", arrayParam);
        generator.addRequestMethod(info);
    }

    public void generatorDelete(MvcGenerator generator){
        if (!configuration.getAllowOperationType().contains(OperationType.DELETE) ||
                configuration.getHiddleMethods().contains("delete")){
            return;
        }
        MappingMethodInfo info = createInfo("delete", "delete", RequestMethod.POST, "删除",
                "{\n  delete0($1);\n}",
                false, true, false, "", bodyParam);
        generator.addRequestMethod(info);
    }

    public void generatorDeleteById(MvcGenerator generator){
        if (!configuration.getAllowOperationType().contains(OperationType.DELETE) ||
                configuration.getHiddleMethods().contains("deleteById")){
            return;
        }
        MappingMethodInfo info = createInfo("deleteById", "deleteById", RequestMethod.GET, "根据id删除",
                "{\n  deleteById0($1);\n}",
                false, true, false, "", "!id");
        generator.addRequestMethod(info);
    }

    public void generatorDeleteByIds(MvcGenerator generator){
        if (!configuration.getAllowOperationType().contains(OperationType.DELETE) ||
                configuration.getHiddleMethods().contains("deleteByIds")){
            return;
        }
        MappingMethodInfo info = createInfo("deleteByIds", "deleteByIds", RequestMethod.POST, "根据id数组删除",
                "{\n  deleteByIds0($1);\n}",
                false, true, false, "", "!?body::array");
        generator.addRequestMethod(info);
    }
}
