package com.black.ods;

import javax.sql.DataSource;

public class Ods {

    public static OdsChain createChain(DataSource dataSource){
        DefaultOdsChain chain = new DefaultOdsChain();
        chain.setDataSource(dataSource);
        return chain;
    }

    public static OdsUndertake queryUndertake(String sql){
        DefaultOdsUndertake undertake = new DefaultOdsUndertake();
        undertake.createQueryActuator(sql);
        return undertake;
    }

    public static OdsUndertake updateUndertake(String sql){
        DefaultOdsUndertake undertake = new DefaultOdsUndertake();
        undertake.createUpdateActuator(sql);
        return undertake;
    }

}
