package com.black.core.rpc;

import com.black.core.annotation.ChiefServlet;
import com.black.core.util.LazyAutoWried;
import org.springframework.web.bind.annotation.GetMapping;

@ChiefServlet
public class DemoAction {

    @LazyAutoWried
    A1Mapper mapper;


    @GetMapping("doRpc")
    Object doRpc(Integer num){
        switch (num){
            case 1:
                return mapper.invoke1();
            case 2:
                return mapper.invoke2();
            case 3:
                return mapper.invoke3();
        }
        return num;
    }
}
