package com.black.log;

import com.black.core.annotation.ChiefServlet;

/**
 * @author 李桂鹏
 * @create 2023-06-06 11:39
 */
@SuppressWarnings("all") @ChiefServlet
public class LogDemo {


    @Lgwr
    public void run(){

    }

    public static void main(String[] args) {
        new LogDemo().run();
    }
}
