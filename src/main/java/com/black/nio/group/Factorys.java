package com.black.nio.group;

public class Factorys {


    public static SessionFactory open(GioResolver resolver, NioType type){
        return open("0.0.0.0", 3333, resolver, type);
    }

    public static SessionFactory open(int port, GioResolver resolver, NioType type){
        return open("0.0.0.0", port, resolver, type);
    }

    public static SessionFactory open(String host, int port, GioResolver resolver, NioType type){
        Configuration configuration = new Configuration(type);
        configuration.setHost(host);
        configuration.setPort(port);
        configuration.addResolver(resolver);
        return new GioSessionFactory(configuration);
    }

}
