package com.black.core.yml;

import org.springframework.web.bind.annotation.GetMapping;

//@ChiefServlet
@SuppressWarnings("all")
public class Usios {

    @GetMapping("/ui/**")
    Object aaa(){
        return "hello";
    }

}
