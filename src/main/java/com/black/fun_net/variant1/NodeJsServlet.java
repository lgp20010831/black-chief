package com.black.fun_net.variant1;

import com.black.core.annotation.Export;
import com.black.fun_net.Net;
import com.black.fun_net.Post;
import com.black.fun_net.Signature;
import com.black.user.User;

import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-06-20 16:13
 */
@SuppressWarnings("all")
public class NodeJsServlet extends Net {

    @Signature("!?body | 根据条件 map 查询列表 | @OpenSqlPage")
    Post<List<User>> userList = () -> write(query("user", body()).list());

    @Export("system")
    void export(){

//        get("/userList", () -> {
//            return write(query("user", body()).list();
//        }, "!?body | 根据条件 map 查询列表 | @OpenSqlPage");
//

    }


}
