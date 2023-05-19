package com.black.core.log.record;

/**
 * 日志记录配置
 * @author 李桂鹏
 * @create 2023-05-19 11:13
 */
@SuppressWarnings("all")
public class Configuration {

    private static Configuration configuration;

    public synchronized static Configuration getInstance() {
        if (configuration == null){
            configuration = new Configuration();
        }
        return configuration;
    }

    private Configuration(){}

    private Class<? extends LogInfo> entity = LogInfo.class;

}
