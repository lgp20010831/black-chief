package com.black.core.sql.code.datasource;


import com.black.core.factory.beans.InitMethod;
import com.black.core.factory.beans.Properties;
import com.black.core.factory.beans.PrototypeBean;
import com.zaxxer.hikari.HikariDataSource;

public class MyDynamicDataSource extends ThreadLocalDynamicDataSource{

    @InitMethod
    void load(@PrototypeBean @Properties("datasource.master") HikariDataSource master,
              @PrototypeBean @Properties("datasource.second") HikariDataSource second){
        setDefaultDataSource(master);
        registerDataSource("master", master);
        registerDataSource("second", second);
    }

}
