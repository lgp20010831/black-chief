package com.black.fun_net;

import org.springframework.web.bind.annotation.RequestMapping;

@SuppressWarnings("all")
@RequestMapping("/xxxx")
public class MyFunNetServlet extends Net{


    @Desc("!id | 根据 id 查询")
    Post list = () -> {
        System.out.println(getString("id"));
        write("hello wrold");
    };

    @Desc("!?body | 根据条件 map 查询列表 | @OpenSqlPage")
    Post userList = () -> write(query("user", body()));

}
