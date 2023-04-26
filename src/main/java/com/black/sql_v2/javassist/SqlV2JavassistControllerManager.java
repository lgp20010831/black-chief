package com.black.sql_v2.javassist;

import com.black.api.ApiRemark;
import com.black.core.annotation.ChiefServlet;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.annotation.OpenTransactional;
import com.black.core.util.StringUtils;
import com.black.javassist.CtAnnotation;
import com.black.javassist.CtAnnotations;
import com.black.javassist.PartiallyCtClass;
import com.black.javassist.Utils;
import com.black.sql_v2.action.AbstractProvideServlet;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class SqlV2JavassistControllerManager {

    private Class<?> superClass = AbstractProvideServlet.class;

    private String createPath = Utils.FICTITIOUS_PATH;

    private IoLog log = LogFactory.getArrayLog();

    public SqlV2JavassistControllerManager(){

    }

    public SqlV2JavassistControllerManager(Class<?> superClass) {
        this.superClass = superClass;
    }

    public void setSuperClass(Class<?> superClass) {
        this.superClass = superClass;
    }

    public Class<?> createController(String alias, String tableName){
        String className = createClassName(alias, tableName);
        PartiallyCtClass partiallyCtClass = PartiallyCtClass.make(className, createPath);

        //设置父类
        partiallyCtClass.setSuperClass(superClass);


        //添加 getTableName 和 getAlias 方法
        partiallyCtClass.addMethod("getTableName", String.class, getGetTableNameBody(tableName));
        partiallyCtClass.addMethod("getAlias", String.class, getGetAliasBody(alias));

        //添加 @ChiefServlet 注解
        addControllerAnnotation(partiallyCtClass, tableName, alias);
        Class<?> javaClass = partiallyCtClass.getJavaClass();
        log.info("[SqlV2JavassistControllerManager] create sql provide controller: {}", javaClass.getSimpleName());
        return javaClass;
    }

    protected void addControllerAnnotation(PartiallyCtClass partiallyCtClass, String tableName, String alias){
        CtAnnotation servletAnn = new CtAnnotation(ChiefServlet.class);
        servletAnn.addField("value", alias + "/" + tableName, String[].class);
        CtAnnotation transactionalAnn = new CtAnnotation(OpenTransactional.class);
        CtAnnotation apiAnn = new CtAnnotation(ApiRemark.class);
        SqlV2ApiRemarkRegister instance = SqlV2ApiRemarkRegister.getInstance();
        String remark = instance.get(tableName);
        apiAnn.addField("value", StringUtils.hasText(remark) ? remark : tableName + "管理", String.class);
        partiallyCtClass.addClassAnnotations(CtAnnotations.group(servletAnn, transactionalAnn, apiAnn));
    }

    protected String getGetTableNameBody(String tableName){
        return "{return \"" + tableName +"\";}";
    }

    protected String getGetAliasBody(String alias){
        return "{return \"" + alias +"\";}";
    }

    protected String createClassName(String alias, String tableName){
        return StringUtils.linkStr(StringUtils.titleCase(alias), StringUtils.titleCase(StringUtils.ruacnl(tableName)), "Controller");
    }

}
