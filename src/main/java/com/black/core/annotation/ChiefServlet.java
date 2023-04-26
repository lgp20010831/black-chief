package com.black.core.annotation;

import com.black.core.aop.servlet.AnalyzedMethod;
import com.black.core.aop.servlet.GlobalEnhanceRestController;
import com.black.core.aop.servlet.RestResponse;
import com.black.core.mvc.response.Response;
import com.black.core.util.AliasWith;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@CrossOrigin
@AnalyzedMethod
@RestController
@RequestMapping
@GlobalEnhanceRestController
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChiefServlet {

    @AliasFor(annotation = GlobalEnhanceRestController.class, attribute = "value")
    @AliasWith(target = GlobalEnhanceRestController.class, name = "value")
    Class<? extends RestResponse> response() default Response.class;

    @AliasFor(annotation = RequestMapping.class)
    String[] value() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] headers() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] consumes() default {};

    @AliasFor(annotation = RequestMapping.class)
    String[] produces() default {};

    @AliasFor(annotation = RestController.class, attribute = "value")
    String componentName() default "";
}
