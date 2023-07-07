package com.black.api;

import com.alibaba.fastjson.JSONObject;
import com.black.blent.Blent;
import com.black.blent.BlentJavassistManager;
import com.black.javassist.*;
import com.black.core.bean.TrustBeanCollector;
import com.black.core.util.StringUtils;
import com.black.swagger.RequestResolver;
import io.swagger.annotations.ApiModelProperty;
import javassist.CtField;

import java.sql.Connection;
import java.util.List;

import static com.black.api.ApiV2Utils.DEF_ANN_COLUMN_GENERATOR;

public class ApiRequestResolver extends RequestResolver {

    protected Connection connection;

    protected final JdbcEntityCreator entityCreator;

    public ApiRequestResolver(){
        this(null);
    }

    public ApiRequestResolver(Connection connection) {
        this.connection = connection;
        entityCreator = new JdbcEntityCreator(connection);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        entityCreator.setConnection(connection);
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    protected List<CtField> mutateJsonToFields(JSONObject json, PartiallyCtClass partiallyCtClass) {
        return Utils.mutateJsonToFields(json, partiallyCtClass.getCtClass(), (field, ctClass, jsonValue) -> {
            String remark = jsonValue == null ? "" : jsonValue.toString();
            remark = Utils.getRemarkByRV(remark);
            CtAnnotation annotation = new CtAnnotation(ApiModelProperty.class);
            annotation.addField("value", remark, String.class);
            CtAnnotation ctAnnotation = new CtAnnotation(ApiRemark.class);
            ctAnnotation.addField("value", remark, String.class);
            CtAnnotations annotations = CtAnnotations.group(annotation, ctAnnotation);
            Utils.addFieldAnnotation(field, annotations);
        });
    }

    @Override
    protected PartiallyCtClass parseBlentToPartiallyClass(Blent blent, Class<?> assist) {
        return BlentJavassistManager.parseBlentToPartiallyClass(blent, alias -> {
            return toFun(alias, assist);
        });
    }
    protected Class<?> toFun(String alias, Class<?> assist){
        if (TrustBeanCollector.existTrustBean(alias)) {
            return TrustBeanCollector.getTrustBean(alias);
        }
        if (connection == null){
            throw new IllegalStateException("not find trust bean: " + alias + " - and no connection");
        }
        String tableName = StringUtils.unruacnl(alias);
        if (entityCreator.getColumnAnnotationGenerator() == null) {
            entityCreator.setColumnAnnotationGenerator(DEF_ANN_COLUMN_GENERATOR);
        }
        Class<?> javaClass = entityCreator.createJavaClass(tableName);
        TrustBeanCollector.registerTrustBean(javaClass);
        return javaClass;
    }
}
