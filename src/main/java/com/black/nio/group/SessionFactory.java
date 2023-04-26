package com.black.nio.group;

import java.util.function.Consumer;

public interface SessionFactory {

    default SessionFactory apply(Consumer<Configuration> consumer){
        if (consumer != null){
            consumer.accept(getConfiguration());
        }
        return this;
    }

    /**
     * 返回配置类
     * @return 配置类
     */
    Configuration getConfiguration();

    /***
     * 开启一个会话
     * @return 会话
     */
    Session openSession();

}
