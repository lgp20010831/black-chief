package com.black.core.sql.code;

import com.black.core.sql.code.config.GlobalSQLConfiguration;

public interface SqlConfigurationAware {


    void setConfiguration(GlobalSQLConfiguration configuration);

}
