package com.black.core.aop.servlet.encryption.annotation;

import com.black.core.aop.servlet.encryption.Base64Secret;
import com.black.core.aop.servlet.encryption.Decryption;
import com.black.core.aop.servlet.encryption.Encryption;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityMethod {

    //是否解密参数
    boolean decryptParam() default false;

    //是否加密结果
    boolean encryptResult() default true;

    //如果该值为 true, 加密或解密失败, 不会抛出异常, 直接返回明文或密文
    boolean decryptAndEncryptThrowPlaintext() default false;

    //解密处理器
    Class<? extends Decryption> decryptHandler() default Base64Secret.class;

    //加密处理器
    Class<? extends Encryption> encryptHandler() default Base64Secret.class;
}
