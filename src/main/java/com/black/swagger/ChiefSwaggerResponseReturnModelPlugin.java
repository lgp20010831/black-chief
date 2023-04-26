package com.black.swagger;

import com.black.api.ResponseData;
import com.black.function.Consumer;
import com.black.javassist.JavassistCtClassManager;
import com.black.javassist.Utils;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.query.ClassWrapper;
import com.black.core.query.FieldWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.tools.BeanUtil;
import com.black.core.util.*;
import com.black.utils.ServiceUtils;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import io.swagger.annotations.ApiModelProperty;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.method.HandlerMethod;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.*;

import static com.black.swagger.JDBCTableCtClassManager.FICTITIOUS_PATH;

public class ChiefSwaggerResponseReturnModelPlugin implements OperationBuilderPlugin, BeanFactoryAware {

    @LazyAutoWried
    TypeResolver typeResolver;

    BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void apply(OperationContext operationContext) {
        if (!operationContext.getReturnType().isInstanceOf(Object.class)) {
            return;
        }
        ChiefSwaggerResponseAdaptive annotation = operationContext.findAnnotation(ChiefSwaggerResponseAdaptive.class).orNull();
        if (annotation == null){
            return;
        }
        Class<?> returnClass;
        Class<?> responseClass = findResponseClass(operationContext);
        Class<?> entityClass = findEntityClass(operationContext, annotation);
        Assert.notNull(entityClass, "unknown entity class");
        if (responseClass == null){
            returnClass = entityClass;
        }else {
            returnClass = findCtResponseAndEntityClass(responseClass, entityClass, annotation);
        }
        if (returnClass == null){
            return;
        }
        //ClassWrapper<?> rcw = ClassWrapper.get(returnClass);
        ResolvedType rt = typeResolver.resolve(returnClass);
        operationContext.getDocumentationContext().getAdditionalModels().add(rt);
        Set<ResponseMessage> messages = Av0.set(new ResponseMessageBuilder().code(200).message("OK").responseModel(new ModelRef(ChiefSwaggerUtils.findApiModel(returnClass))).build());
        operationContext.operationBuilder().responseMessages(messages);
    }

    private Class<?> findCtResponseAndEntityClass(Class<?> responseClass, Class<?> entityClass, ChiefSwaggerResponseAdaptive annotation){
        ClassWrapper<?> responseCw = ClassWrapper.get(responseClass);
        boolean list = annotation.list();
        String desc = responseClass.getSimpleName() + "_" + (list ? "list" : "single") + "_" + entityClass.getSimpleName();
        return JavassistCtClassManager.getCtClass(desc, () -> {
            String className = desc;
            String fullName = FICTITIOUS_PATH + "." + className;
            CtClass ctClass = Utils.createClass(fullName);
            ConstPool constPool = ctClass.getClassFile().getConstPool();
            //获取响应类的所有字段
            Collection<FieldWrapper> responseCwFields = responseCw.getFields();
            ArrayList<CtField> fields = new ArrayList<>();
            for (FieldWrapper responseCwField : responseCwFields) {
                ApiModelProperty modelProperty = responseCwField.getAnnotation(ApiModelProperty.class);
                String remark = modelProperty == null ? "" : modelProperty.value();
                Map<Class<? extends java.lang.annotation.Annotation>, Consumer<Annotation>> annMap = ServiceUtils.ofMap(ApiModelProperty.class, (Consumer<Annotation>) annotationSwagger -> {
                    annotationSwagger.addMemberValue("value", new StringMemberValue(remark, constPool));
                });
                CtField field;
                if (responseCwField.hasAnnotation(ResponseData.class)){
                    if (list){
                        field = Utils.createField(responseCwField.getName(), List.class, annMap, ctClass);
                        Utils.getGenericSignature(field, entityClass, "List");
                    }else {
                        field = Utils.createField(responseCwField.getName(), entityClass, annMap, ctClass);
                    }

                }else {
                    field = Utils.createField(responseCwField.getName(), responseCwField.getType(), annMap, ctClass);
                }
                fields.add(field);
            }
            Class<?> javaClass = Utils.createJavaClass(fields, ctClass);
            return javaClass;
        });
    }

    private Class<?> findEntityClass(OperationContext operationContext, ChiefSwaggerResponseAdaptive annotation){
        String value = annotation.value();
        Class<?> target = annotation.target();
        if (StringUtils.hasText(value)){
            HandlerMethod handlerMethod = ChiefSwaggerUtils.findControllerType(operationContext);
            String beanName = handlerMethod.getBean().toString();
            Object bean = beanFactory.getBean(beanName);
            ClassWrapper<?> classWrapper = ClassWrapper.get(BeanUtil.getPrimordialClass(bean));
            MethodWrapper methodWrapper = classWrapper.getSingleMethod(value);
            return (Class<?>) methodWrapper.invoke(bean);
        }else if (!target.equals(void.class)){
            return target;
        }
        throw new IllegalStateException("ill format of annotation");
    }

    private Class<?> findResponseClass(OperationContext operationContext){
        HandlerMethod handlerMethod = ChiefSwaggerUtils.findControllerType(operationContext);
        String beanName = handlerMethod.getBean().toString();
        Object bean = beanFactory.getBean(beanName);
        ClassWrapper<Object> cw = ClassWrapper.get(BeanUtil.getPrimordialClass(bean));
        GlobalEnhanceRestController annotation = AnnotationUtils.findAnnotation(cw, GlobalEnhanceRestController.class);
        if (annotation != null){
            return annotation.value();
        }
        return null;
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }
}
