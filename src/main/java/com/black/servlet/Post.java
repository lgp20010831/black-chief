package com.black.servlet;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("all")
public interface Post {

    default HttpServletRequest getRequest(){
        return null;
    }

}
