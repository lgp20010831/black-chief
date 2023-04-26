package com.black.core.api;

import com.black.api.ApiController;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.spring.ChiefApplicationHolder;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.sql.code.TransactionSQLManagement;
import com.black.core.sql.code.datasource.ConnectionManagement;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Assert;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Setter  @Getter
public class ApiConfiguration {

    static final IoLog log = LogFactory.getLog4j();

    //配置请求中过滤调的字段
    List<String> configRequestExcludes = new ArrayList<>();

    //获取要扫描的 class 集合
    Supplier<Collection<Class<?>>> scannerClasses = () -> {
        ChiefExpansivelyApplication expansivelyApplication = ChiefApplicationHolder.getExpansivelyApplication();
        if (expansivelyApplication != null){
            return expansivelyApplication.getProjectClasses();
        }
        return new HashSet<>();
    };

    boolean cacheStream = false;

    BeanFactory beanFactory;

    //提供数据库连接
    Supplier<Connection> connectionSupplier;

    //0: 从 beanfactory 取 1 :从 ConnectionManagement 取
    int datasourceType = 0;

    String alias = "master";

    Consumer<Connection> backConnection = connection -> {
        switch (datasourceType){
            case 0:
                Assert.notNull(beanFactory, "not find bean factory");
                DataSource dataSource = beanFactory.getBean(DataSource.class);
                DataSourceUtils.releaseConnection(connection, dataSource);
                break;
            case 1:
                ConnectionManagement.closeCurrentConnection(alias);
                break;
            default:
                SQLUtils.closeConnection(connection);
        }
    };

    Function<String, String> streamCallBack = stream -> stream;

    Set<Class<?>> excludesController = new HashSet<>();

    Set<Class<? extends Annotation>> selectedScanAnnotationTypes = new HashSet<>();

    private final Map<String, String> globalHeaders = new LinkedHashMap<>();


    public ApiConfiguration(){
        configRequestExcludes.addAll(Arrays.asList("is_deleted", "inserted_at",
                "updated_at", "deleted_at",
                "create_date", "create_time", "create_user", "creator", "update_date", "update_time",
                "updater", "delete_date", "delete_time"));
        selectedScanAnnotationTypes.add(ApiController.class);
    }
    public ApiConfiguration putHeader(String key, String value){
        globalHeaders.put(key, value);
        return this;
    }

    public ApiConfiguration putHeaders(Map<String, String> headers){
        if (headers != null){
            globalHeaders.putAll(headers);
        }
        return this;
    }

    public Supplier<Connection> getConnectionSupplier() {
        if (connectionSupplier != null){
            return connectionSupplier;
        }
        switch (datasourceType){
            case 0:
                log.info("current get database mode -- get from spring");
                return connectionSupplier = getFormBeanFactory();
            case 1:
                log.info("current get database mode -- get from map sql -- alias: {}", alias);
                backConnection = connection -> {
                    if (!TransactionSQLManagement.isActivity(alias)) {
                        ConnectionManagement.closeConnection(connection, alias);
                    }
                };
                return connectionSupplier = getFormConnectionManagement();

        }
        throw new IllegalStateException("not support datasource type -- should be 0 or 1");
    }

    private Supplier<Connection> getFormBeanFactory(){
        Assert.notNull(beanFactory, "要从工厂中获取连接, 请先注入工厂");
        DataSource dataSource = beanFactory.getBean(DataSource.class);
        return () -> DataSourceUtils.getConnection(dataSource);
    }

    private Supplier<Connection> getFormConnectionManagement(){
        return () -> ConnectionManagement.getConnection(alias);
    }



    public void setExcludesController(Class<?>... excludesController) {
        this.excludesController.addAll(Arrays.asList(excludesController));
    }
}
