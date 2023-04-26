package com.black.api.handler;

import com.black.core.bean.TrustBeanCollector;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;

import java.sql.Connection;

public class TrustBeanMetadataBuilder implements MetadataBuilder {

    private final AliasColumnConvertHandler columnConvertHandler;

    public TrustBeanMetadataBuilder() {
        columnConvertHandler = new HumpColumnConvertHandler();
    }

    @Override
    public Object buildMatedata(String plane, Connection connection) {
        String alias = columnConvertHandler.convertAlias(plane);
        if (TrustBeanCollector.existTrustBean(alias)) {
            return TrustBeanCollector.getTrustBean(alias);
        }
        return null;
    }
}
