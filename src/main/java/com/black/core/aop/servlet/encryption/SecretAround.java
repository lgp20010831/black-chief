package com.black.core.aop.servlet.encryption;

import com.black.core.aop.servlet.*;
import com.black.core.aop.servlet.encryption.annotation.PrimarySecretParam;
import com.black.core.aop.servlet.encryption.annotation.SecurityMethod;
import com.black.core.cache.TypeConvertCache;
import com.black.core.convert.TypeHandler;
import com.black.core.query.MethodWrapper;
import com.black.core.spring.instance.InstanceFactory;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.Charset;


@Log4j2
@GlobalAround
public class SecretAround implements GlobalAroundResolver {

    final InstanceFactory factory;

    public SecretAround(@NonNull InstanceFactory factory) {
        this.factory = factory;
    }

    @Override
    public int getOrder() {
        return -214;
    }

    @Override
    public Object[] handlerArgs(Object[] args, HttpMethodWrapper httpMethodWrapper) {
        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        if (!wrapper.hasAnnotation(SecurityMethod.class)) {
            return args;
        }
        //获取注解
        SecurityMethod annotation = wrapper.getAnnotation(SecurityMethod.class);
        if (!annotation.decryptParam()){
            return args;
        }

        if (!wrapper.parameterHasAnnotation(PrimarySecretParam.class)) {
            if (log.isWarnEnabled()) {
                log.warn("若要解密方法参数, 需要用 PrimarySecretParam 注解标注在参数上");
            }
            return args;
        }
        ParameterWrapper pw = wrapper.getSingleParameterByAnnotation(PrimarySecretParam.class);
        Decryption handler = instanceHandler(annotation.decryptHandler());
        //要解密的参数
        Object param = args[pw.getIndex()];
        if (param == null){
            return args;
        }
        args[pw.getIndex()] = cast(pw, handler.decode(param.toString()));
        return args;
    }


    Object cast(ParameterWrapper pw, byte[] decodeBytes){
        PrimarySecretParam annotation = pw.getAnnotation(PrimarySecretParam.class);
        Object val = null;
        if (annotation != null){
            Charset charset = Charset.forName(annotation.codingFormat());
            val = new String(decodeBytes, charset);
            Class<?> type = pw.getType();
            if (!String.class.isAssignableFrom(type)){
                TypeHandler handler = TypeConvertCache.getTypeHandler();
                if (handler == null){
                    throw new CiphertextOperationException("cannot convert type to " + type.getSimpleName());
                }
                val = handler.convert(type, val);
            }
        }
        return val;
    }

    @Override
    public Object handlerAfterInvoker(Object result, HttpMethodWrapper httpMethodWrapper, Class<? extends RestResponse> responseClass) {
        MethodWrapper wrapper = httpMethodWrapper.getMethodWrapper();
        if (!wrapper.hasAnnotation(SecurityMethod.class)) {
            return result;
        }

        //获取注解
        SecurityMethod annotation = wrapper.getAnnotation(SecurityMethod.class);
        if (!annotation.encryptResult()){
            return result;
        }
        Encryption handler = instanceHandler(annotation.encryptHandler());
        Object finallyResult = result;
        if (result instanceof RestResponse){
            finallyResult = ((RestResponse)result).obtainResult();
        }

        byte[] byteArray = null;
        TypeHandler typeHandler = TypeConvertCache.initAndGet();
        if (typeHandler != null){
           byteArray  = typeHandler.genericConvert(byte[].class, finallyResult);
        }

        if (byteArray == null){
            return result;
        }
        finallyResult = handler.encode(byteArray);
        if (result instanceof RestResponse){
            ((RestResponse)result).setResult(finallyResult);
            return result;
        }
        return finallyResult;
    }

    protected <S> S instanceHandler(@NonNull Class<S> handlerClass){
        try {
            return factory.getInstance(handlerClass);
        }catch (Throwable e){
            throw new CiphertextOperationException("无法实例化处理器: " + handlerClass.getSimpleName());
        }
    }
}
