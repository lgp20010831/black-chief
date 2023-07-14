package com.black.core.api;

import com.black.api.*;
import com.black.api.ApiContext;
import com.black.role.SkipVerification;
import com.black.core.servlet.annotation.UnwantedVerify;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.core.util.StreamUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;


@Log4j2
@UnwantedVerify @SkipVerification
@RestController @CrossOrigin
public class ApiService{

    BeanFactory beanFactory;

    private Collection<Class<?>> controllerClasses;

    private String cache = null;

    public ApiService(BeanFactory beanFactory) {
        log.info("api service initialization completed");
        this.beanFactory = beanFactory;
    }

    @GetMapping("api")
    public String api(HttpServletResponse response){
        log.info("api 文档访问");
        configResponse(response);
        ApiConfiguration configuration = prepare();
        String stream = doFlush(configuration);
        log.info("api 正文大小: {}", stream.length());
        return stream;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public ApiConfiguration prepare(){
        ApiConfiguration configuration = ApiConfigurationHolder.getConfiguration();
        Set<Class<? extends Annotation>> annotationTypes = configuration.getSelectedScanAnnotationTypes();
        configuration.setBeanFactory(beanFactory);
        if (controllerClasses == null){
            Collection<Class<?>> projectClasses = configuration.getScannerClasses().get();
            controllerClasses = StreamUtils.filterSet(projectClasses, cly -> {
                if (!BeanUtil.isSolidClass(cly)){
                    return false;
                }
                for (Class<? extends Annotation> annotationType : annotationTypes) {
                    if (AnnotationUtils.getAnnotation(cly, annotationType) != null) {
                        return true;
                    }
                }
                return false;
            });
            controllerClasses.removeIf(cc -> configuration.getExcludesController().contains(cc));
        }
        return configuration;
    }

    @GetMapping("api-data")
    public Object data(){
        log.info("api data 数据访问");
        ApiConfiguration configuration = prepare();
        return getData(configuration);
    }

    @GetMapping("clearCache")
    public void clearCache(){
        log.info("clear api stream cache");
        cache = null;
    }

    private void configResponse(HttpServletResponse response){
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
    }

    private Object getData(ApiConfiguration apiConfiguration){
        ApiV2Utils.configRequestExclude(apiConfiguration.getConfigRequestExcludes().toArray(new String[0]));
        Supplier<Connection> connectionSupplier = apiConfiguration.getConnectionSupplier();
        Assert.notNull(connectionSupplier, "connectionSupplier is null");
        Connection connection = connectionSupplier.get();
        Configuration configuration = new Configuration();
        configuration.setType(ApiType.HTML);
        configuration.setConnection(connection);
        Map<String, String> globalHeaders = apiConfiguration.getGlobalHeaders();
        configuration.putHeaders(globalHeaders);
        try {
            for (Class<?> type : controllerClasses) {
                configuration.addController(type);
            }
            ApiContext context = new ApiContext(configuration);
            return context.getModular();
        }finally {
            Consumer<Connection> backConnection = apiConfiguration.getBackConnection();
            if (backConnection != null){
                backConnection.accept(connection);
            }
        }
    }

    private String doFlush(ApiConfiguration apiConfiguration){
        if (apiConfiguration.isCacheStream() && cache != null){
            return cache;
        }
        ApiV2Utils.configRequestExclude(apiConfiguration.getConfigRequestExcludes().toArray(new String[0]));
        Supplier<Connection> connectionSupplier = apiConfiguration.getConnectionSupplier();
        Assert.notNull(connectionSupplier, "connectionSupplier is null");
        Connection connection = connectionSupplier.get();
        Configuration configuration = new Configuration();
        configuration.setType(ApiType.HTML);
        configuration.setConnection(connection);
        try {
            for (Class<?> type : controllerClasses) {
                configuration.addController(type);
            }
            ApiContext context = new ApiContext(configuration);
            String stream = context.getStream();
            if (apiConfiguration.isCacheStream()){
                cache = stream;
            }
            return stream;
        }finally {
            Consumer<Connection> backConnection = apiConfiguration.getBackConnection();
            if (backConnection != null){
                backConnection.accept(connection);
            }
        }
    }
}
