package com.black.sql_v2.javassist;

import com.black.core.annotation.ChiefServlet;
import com.black.core.util.Av0;
import org.springframework.web.bind.annotation.GetMapping;

@ChiefServlet("hhh")
public class TestController {

    @GetMapping("hello")
    Object loc(){
        return Av0.js("name", "lgp").toJSONString();
    }
}
