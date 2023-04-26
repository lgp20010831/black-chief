package com.black.datasource;

import com.black.config.AttributeUtils;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import lombok.NonNull;

import javax.sql.DataSource;
import java.util.concurrent.LinkedBlockingQueue;

public class ProduceElementDataSourceDiscriminateManager {

    private static final IoLog log = LogFactory.getArrayLog();

    private static final LinkedBlockingQueue<ProduceElementResolver> resolvers = new LinkedBlockingQueue<>();

    public static void registerResolver(ProduceElementResolver resolver){
        resolvers.add(resolver);
    }

    public static DataSource tryGetDataSource(@NonNull Object element){
        DataSource dataSource = null;
        for (ProduceElementResolver resolver : resolvers) {
            try {
                dataSource = resolver.tellApart(element);
            } catch (Throwable e) {
                continue;
            }
            if (dataSource != null){
                log.info("[PEDM] processor: {} ====> Identify Elements: {}",
                        AttributeUtils.getName(resolver), element);
                break;
            }
        }
        return dataSource;
    }

}
