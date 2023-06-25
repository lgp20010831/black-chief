package com.black.core.demo;

import com.black.core.annotation.ChiefServlet;
import com.black.core.annotation.ChiefTransaction;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.sql_v2.Sql;
import com.black.throwable.IOSException;
import com.black.utils.ServiceUtils;
import org.springframework.web.bind.annotation.GetMapping;

@ChiefServlet("test")
@ChiefTransaction
public class TestController {


    @GetMapping("ill")
    public void ill(int id, int ill){
        Sql.configDataSource("ill", new SpringDataSourceBuilder());
        Sql.opt("ill").delete("ayc", ServiceUtils.ofMap("id", id));
        if (ill == 1)
            throw new IOSException();
    }

    public static void main(String[] args) {
        String[] strings = {"1", "2"};
        Object[] array = ServiceUtils.castArray(strings, Object.class);
        System.out.println(array);
    }
}
