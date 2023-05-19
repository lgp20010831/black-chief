package com.black.token;

import com.black.sql_v2.period.AttributeHandler;
import com.black.user.User;

/**
 * @author 李桂鹏
 * @create 2023-05-19 18:09
 */
@SuppressWarnings("all")
public class TokenDataLocal {

    private static final ThreadLocal<AttributeHandler> local = new ThreadLocal<>();


    public static AttributeHandler get(){
        AttributeHandler handler = local.get();
        return handler == null ? new MapAttributeHandler(null) : handler;
    }

    public static boolean exist(){
        return local.get() != null;
    }

    public static void set(AttributeHandler user){
        local.set(user);
    }

    public static void remove(){
        local.remove();
    }
}
