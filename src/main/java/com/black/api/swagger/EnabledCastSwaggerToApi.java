package com.black.api.swagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//将swagger 注解的信息转换成 api 信息
//例如将 @Api --> @ApiRemark
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnabledCastSwaggerToApi {

}
