package com.black.core.swagger;

import com.black.api.Configuration;
import com.black.api.swagger.EnabledCastSwaggerToApi;
import com.black.api.swagger.HandlerSwaggerAdaptation;
import com.black.api.swagger.SwaggerJdbcPropertyApiResolver;
import com.black.core.spring.ChiefExpansivelyApplication;
import com.black.core.spring.OpenComponent;
import com.black.core.spring.annotation.LazyLoading;

@LazyLoading(EnabledCastSwaggerToApi.class)
public class SwaggerIntegrationApiComponent implements OpenComponent {

    @Override
    public void load(ChiefExpansivelyApplication expansivelyApplication) throws Throwable {
        HandlerSwaggerAdaptation adaptation = new HandlerSwaggerAdaptation();
        adaptation.addApiScanner();
        Configuration.resolverTypes.add(SwaggerJdbcPropertyApiResolver.class);
        adaptation.addApiRemarkFun();
    }
}
