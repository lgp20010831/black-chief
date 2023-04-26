package com.black.core.run.code;

import com.black.core.factory.beans.BeanFactory;
import com.black.core.sql.code.DataSourceBuilder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;

@Getter @Setter
public class Configuration {
    final BeanFactory factory;
    String[] position;
    Class<? extends SQLFileExecute> executeType;
    Class<? extends SQLFileScanner> scannerType;
    Class<? extends DataSourceBuilder> datasource;
    boolean stopOnFileError;
    boolean stopOnSentenceError;
    Connection connection;

    private SQLFileExecute execute;

    private SQLFileScanner scanner;

    private DataSourceBuilder dataSourceBuilder;


    public Configuration(BeanFactory factory) {
        this.factory = factory;
    }

    public void setDatasource(Class<? extends DataSourceBuilder> datasource) {
        this.datasource = datasource;
    }

    public void setExecuteType(Class<? extends SQLFileExecute> executeType) {
        this.executeType = executeType;
    }

    public void setScannerType(Class<? extends SQLFileScanner> scannerType) {
        this.scannerType = scannerType;
    }

    public void lazyLoad(){
        if (executeType != null)
            execute = factory.getSingleBean(executeType);
        if (scannerType != null)
            scanner = factory.getSingleBean(scannerType);
        if (datasource != null)
            dataSourceBuilder = factory.getSingleBean(datasource);
    }
}
