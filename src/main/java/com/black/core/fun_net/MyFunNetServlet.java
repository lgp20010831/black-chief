package com.black.core.fun_net;

import com.black.compile.JavaDelegateCompiler;
import com.black.core.minio.Minios;
import com.black.fun_net.*;
import com.black.user.User;
import com.black.xml.crud.TypeRegister;

import java.util.List;


@SuppressWarnings("all")
@Signature("xxx | 测试函数式接口")
public class MyFunNetServlet extends Net {

    static {
        TypeRegister register = TypeRegister.getInstance();
        register.registerType(User.class);
    }

    @Signature("!id | 根据 id 查询")
    Get list = () -> {
        write(one() + "hello wrold");
    };

    @Signature("!?body | 根据条件 map 查询列表 | @OpenSqlPage")
    Post<List<User>> userList = () -> write(query("user", body()).list());

    @Signature("!%file::multi | 上传文件接口")
    Post uploadFile = () -> Minios.def().upload(file());

    Delete nill;

    @Signature("!?body::list<user> | 测试泛型擦除")
    Put testGeneric;

    @Signature("!?body::string | 运行代码") @PronMapping("consumes:text/plain")
    Post runCode = () -> new JavaDelegateCompiler().compileAndRun($1());

}
