package com.black.datasource;

import com.black.config.annotation.Attribute;
import com.black.config.annotation.AttributePrefix;
import com.black.config.annotation.Unnecessary;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data @Unnecessary
@AttributePrefix("spring.datasource.dynamic")
public class MybatisDynamicDataSourceProperties{

    @Attribute("datasource")
    private Map<String, DataSourceProperties> datasourceMap = new LinkedHashMap<>();

    private String primary;
}
