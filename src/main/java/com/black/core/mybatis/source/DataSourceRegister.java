package com.black.core.mybatis.source;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class DataSourceRegister {

    private final List<SourceBuilder> dataSourceConnectWrappers = new ArrayList<>();

    protected void register(SourceBuilder sourceBuilder){
        dataSourceConnectWrappers.remove(sourceBuilder);
        dataSourceConnectWrappers.add(sourceBuilder);
    }

    public SourceBuilder begin(String alias){
        return new SourceBuilder(this, alias);
    }

    public List<SourceBuilder> getDataSourceConnectWrappers() {
        return dataSourceConnectWrappers;
    }

    @Getter
    public static class SourceBuilder{
        String alias;
        DataSourceRegister register;
        String driver;
        String url;
        String username;
        String password;

        public SourceBuilder(DataSourceRegister register, String alias) {
            this.alias = alias;
            this.register = register;
        }

        public SourceBuilder driver(String driver) {
            this.driver = driver;
            return this;
        }

        public SourceBuilder password(String password) {
            this.password = password;
            return this;
        }

        public SourceBuilder url(String url) {
            this.url = url;
            return this;
        }
        public SourceBuilder username(String username) {
            this.username = username;
            return this;
        }
        public void end(){
            register.register(this);
        }
    }
}
