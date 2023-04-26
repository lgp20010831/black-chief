package com.black.jdbc;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.YmlDataSourceBuilder;
import lombok.Data;

@Data
public class Configuration {

    private IoLog log;

    /**
     * Do you use jpa parsing techniques
     * Default to No for higher efficiency
     * Jpa parsing method:  ?1, ?2
     * Ability to reuse duplicate parameters
     */
    private boolean useJpaTechnique = false;

    /**
     * Data Source Constructor
     * There are multiple implementation options available
     * for maintaining a unique data source
     * Serving the factory
     */
    private DataSourceBuilder dataSourceBuilder;

    /**
     * Automate the activation of certain functions,
     * including automatic transaction initiation
     */
    private boolean automation = false;

    /**
     * Lazy loading
     * Including delayed creation of data sources
     * and delayed acquisition of database connections
     */
    private boolean lazy = true;

    public Configuration(){
        log = LogFactory.getArrayLog();
        setLogPrefix("[JDBC] ==> ");
        initDataSourceBuilder();
    }

    protected void initDataSourceBuilder(){
        dataSourceBuilder = new YmlDataSourceBuilder();
    }

    public void setLogPrefix(String prefix){
        IoLog ioLog = getLog();
        ioLog.setPrefix(prefix);
    }


}
