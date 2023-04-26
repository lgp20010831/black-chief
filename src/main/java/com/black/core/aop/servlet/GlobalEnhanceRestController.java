package com.black.core.aop.servlet;

import com.black.core.mvc.response.Response;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 标注此注解的类会成为一个被 aop 增强的
 * 控制器, aop 会增强 带有 @RequestMapping 注解的方法
 * 且方法的返回值必须是模糊的, 例如 Object.class, 或者响应类类型 RestResponse
 * 对于增强的方法, 会打印调用此方法的请求信息, 并且对返回值进行封装, 封转成 RestResponse 类型
 * 所有被增强的方法只需要返回具体的数据就可以
 *
 * 如果需要手动干预方法的执行可以去实现{@link GlobalAroundResolver}接口
 * 并且标注注解{@link GlobalAround} 则会在被增强方法执行前后被调用
 *
 * 根据这个特性, 有提供了许多的功能, 比如依赖于 mybatisplus 来实现自动解析参数
 * 注入 wrapper 只需要在方法参数上加上 {@link com.black.core.aop.servlet.plus.WriedQueryWrapper}
 * 注解
 *
 * 查询主表并查询从表, 往往可能需要一对多查询
 * 在 mybatis 中需要在ResultMap 中通过<Collection>标签在实现
 * 现在通过 mybatisplus 可以在通过配置来实现
 * 在方法上标注注解{@link com.black.core.aop.ibatis.servlet.ConfigurationsFactory}
 * 填充其 query 属性, 每一个 query注解相当于一个从表, 配置完属性后,
 * 需要在参数中加一个{@link com.black.core.mybatis.plus.QueryFactory} 参数
 * 并且标注{@link com.black.core.aop.ibatis.servlet.OpenFactory} 注解
 * 然后调用该类方法来实现自动查询, 且会把主表源数据整合到一起
 */
@RestController
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalEnhanceRestController {

    //设置响应类类型
    Class<? extends RestResponse> value() default Response.class;

    //打印接口的调用日志
    boolean printLog() default true;
}
