package com.black.api.swagger;

import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.AliasColumnConvertHandler;
import lombok.Data;

@Data
public class SwaggerCastToApiConfiguration {

    private static SwaggerCastToApiConfiguration configuration;


    public synchronized static SwaggerCastToApiConfiguration getInstance() {
        if (configuration == null){
            configuration = new SwaggerCastToApiConfiguration();
        }
        return configuration;
    }

    private AliasColumnConvertHandler columnConvertHandler = new HumpColumnConvertHandler();
}
