package com.black.role;

import com.black.user.User;

public class UserLocal {

    private static final ThreadLocal<User> local = new ThreadLocal<>();


    public static User getUser(){
        return local.get();
    }

    public static void set(User user){
        local.set(user);
    }

    public static void remove(){
        local.remove();
    }

    public static String getId(){
        User user = getUser();
        return user == null ? null : user.getId();
    }

    public static String getAccount(){
        User user = getUser();
        return user == null ? null : user.getAccount();
    }

    public static String getPassword(){
        User user = getUser();
        return user == null ? null : user.getPassword();
    }

    public static String getName(){
        User user = getUser();
        return user == null ? null : user.getName();
    }
}
