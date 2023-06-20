package com.black.core.yml;

import com.black.core.annotation.ChiefServlet;
import com.black.log.Lgwr;
import com.black.swagger.v2.V2Swagger;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;

@Api(tags = "测试模块")
@ChiefServlet @SuppressWarnings("all") @Lgwr
public class Usios {

    @V2Swagger("user{}")
    @GetMapping("/ui/**")
    Object aaa(){
        return "hello";
    }

}
