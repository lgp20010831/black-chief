package com.black.core.config;


import com.black.core.aop.config.GlobalAopAutoConfiguration;
import com.black.core.aop.servlet.flow.FlowController;
import com.black.core.aop.servlet.result.ResponseVoidWritor;
import com.black.core.api.ApiService;
import com.black.core.permission.*;
import com.black.core.servlet.ServletArgumentParser;
import com.black.core.servlet.intercept.ServletInterceptor;
import com.black.core.spring.ChiefApplicationConfigurer;
import com.black.core.spring.ChiefApplicationRunner;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.PettyApplicationHelper;
import com.black.core.util.IntegratorScanner;
import com.black.core.util.SimplePattern;
import com.black.core.work.w2.action.DefaultWorkflowV2Controller;
import com.black.core.work.w2.connect.annotation.EnableWorkflowRefinedModule;
import com.black.database.VisitDatabaseController;
import com.black.role.NewestTokenIntercept;
import com.black.swagger.ChiefIbatisAdaptivePlugin;
import com.black.swagger.ChiefSqlMapResponsePlugin;
import com.black.swagger.ChiefSwaggerResponseReturnModelPlugin;
import com.black.swagger.SwaggerAnalyticResolverPlugin;
import com.black.swagger.v2.ChiefOpenResponseCommonTypePlugin;
import com.black.swagger.v2.V2SWaggerPlugin;
import com.black.visit.VisitCompileController;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Log4j2 @SuppressWarnings("all")
@Import({SqlAutoAssembleConfiguration.class, FtlAutoConfiguration.class})
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@Conditional({ChiefConfigurationConditional.class})
@AutoConfigureAfter({GlobalAopAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({OpenChiefSpringProperties.class})
public class SpringAutoConfiguration extends AbstractConfiguration implements ChiefApplicationConfigurer, WebMvcConfigurer {

    public SpringAutoConfiguration(){
        handlerInterceptor = new ServletInterceptor();
        newestTokenIntercept = new NewestTokenIntercept();
    }

    /** 全局 servlet 拦截器 */
    private HandlerInterceptor handlerInterceptor;

    private NewestTokenIntercept newestTokenIntercept;

    @Override
    public String[] scanPackages() {
        return new String[]{"com.black.core"};
    }


    @Bean SimplePattern simplePattern()                 {return new IntegratorScanner();}

    @ConditionalOnProperty(
            prefix = "chief",
            name = {"enabled-visit-compile"},
            havingValue = "true",
            matchIfMissing = false
    )
    @ConditionalOnMissingBean @Bean
    VisitCompileController visitCompileController(){
        return new VisitCompileController();
    }

    @ConditionalOnMissingBean
    @Bean ApiService apiServiceController(){
        return new ApiService(beanFactory);
    }

    @ConditionalOnMissingBean @Bean
    FlowController flowController$4s24(){
        return new FlowController();
    }

    @ConditionalOnMissingBean @Bean
    ResponseVoidWritor responseVoidWritor(){
        return new ResponseVoidWritor();
    }

    @ConditionalOnMissingBean @Bean
    @Conditional(WorkflowCondition.class)
    DefaultWorkflowV2Controller workflowV2Controller(){
        return new DefaultWorkflowV2Controller();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "chief",
            name = {"enabled-visit-database"},
            havingValue = "true",
            matchIfMissing = false
    )
    VisitDatabaseController visitDatabaseController(){
        return new VisitDatabaseController();
    }

    public static class SwaggerCondition implements Condition{

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return ChiefApplicationRunner.isOpen();
        }
    }

    @Conditional({SwaggerCondition.class})
    @AutoConfigureAfter(SpringAutoConfiguration.class)
    @Configuration(proxyBeanMethods = false)
    public static class SwaggerChiefConfiguration{
        @ConditionalOnMissingBean @Bean
        ChiefIbatisAdaptivePlugin chiefIbatisAdaptivePlugin(){
            return new ChiefIbatisAdaptivePlugin();
        }

        @ConditionalOnMissingBean @Bean
        ChiefSqlMapResponsePlugin chiefSqlMapResponsePlugin(){
            return new ChiefSqlMapResponsePlugin();
        }

        @ConditionalOnMissingBean @Bean
        SwaggerAnalyticResolverPlugin swaggerAnalyticResolverPlugin(){return new SwaggerAnalyticResolverPlugin();}
//    @ConditionalOnMissingBean @Bean
//    ChiefResponseModelPlugin chiefResponseModelPlugin(){
//        return new ChiefResponseModelPlugin();
//    }

        @ConditionalOnMissingBean @Bean
        ChiefSwaggerResponseReturnModelPlugin chiefSwaggerResponseReturnModelPlugin(){
            return new ChiefSwaggerResponseReturnModelPlugin();
        }

        @ConditionalOnMissingBean @Bean
        V2SWaggerPlugin v2SWaggerPlugin(){
            return new V2SWaggerPlugin();
        }

        @ConditionalOnMissingBean @Bean
        ChiefOpenResponseCommonTypePlugin chiefOpenResponseCommonTypePlugin(){
            return new ChiefOpenResponseCommonTypePlugin();
        }
    }

    public static class WorkflowCondition implements Condition{

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return ChiefApplicationRunner.isPertain(EnableWorkflowRefinedModule.class);
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (ChiefApplicationRunner.isOpen()){
            long startTime = System.currentTimeMillis();
            PettyApplicationHelper pettyApplicationHelper = new PettyApplicationHelper(applicationContext, beanFactory);
            ChiefExpansivelyApplication application = pettyApplicationHelper.createApplication(this);
            if (log.isInfoEnabled()) {
                log.info("ChiefExpansivelyApplication: initialization completed in {} ms", System.currentTimeMillis()-startTime);
            }
            if (beanFactory instanceof DefaultListableBeanFactory){
                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
                defaultListableBeanFactory.registerSingleton("chiefApplication", application);
                if (application instanceof BeanPostProcessor){
                    defaultListableBeanFactory.addBeanPostProcessor((BeanPostProcessor) application);
                }
                if (application instanceof BeanFactoryPostProcessor){
                    BeanFactoryPostProcessor beanFactoryPostProcessor = (BeanFactoryPostProcessor) application;
                    beanFactoryPostProcessor.postProcessBeanFactory(defaultListableBeanFactory);
                }
            }
        }
    }

    /***
     * spring mvc 针对每个控制器方法准备了一个缓存
     * HandlerMethodArgumentResolver 集合存放所有处理器
     * 如果其中一个方法可以被集合中 index 优先的处理器进行处理
     * 那么以后这个方法都会由该处理器处理, 所有要添加的处理器一定要
     * 与其他处理器区别开, 让前面的处理器无法插手
     * @param resolvers mvc 处理参数队列
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ServletArgumentParser());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(handlerInterceptor);
        registry.addInterceptor(newestTokenIntercept);
    }

    public NewestTokenIntercept getNewestTokenIntercept() {
        return newestTokenIntercept;
    }

    public HandlerInterceptor getHandlerInterceptor() {
        return handlerInterceptor;
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional({RUPConfigurationConditional.class})
    @AutoConfigureAfter(SpringAutoConfiguration.class)
    public static class RUPAutoConfiguration{

        @ConditionalOnMissingBean @Bean
        RoleController roleController$so27(){
            return new RoleController();
        }

        @ConditionalOnMissingBean @Bean
        RolePermissionController rolePermissionController54$osp(){
            return new RolePermissionController();
        }

        @ConditionalOnMissingBean @Bean
        RoleUserController roleUserController1000(){
            return new RoleUserController();
        }

        @ConditionalOnMissingBean @Bean
        UserController userController$lqq(){
            return new UserController();
        }

        @ConditionalOnMissingBean @Bean
        PermissionController permissionController10004(){
            return new PermissionController();
        }

        @ConditionalOnMissingBean @Bean
        RUPResponseAop rupResponseAop$445(){
            return new RUPResponseAop();
        }
    }

    public static class RUPConfigurationConditional implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return ChiefApplicationRunner.isPertain(EnabledRUPComponent.class);
        }
    }
}
