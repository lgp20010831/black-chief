package com.black.log;

/**
 * @author 李桂鹏
 * @create 2023-06-06 11:39
 */
@SuppressWarnings("all")
public class LogDemo {


    public void run(){
        Logs.info("hello wrold");
    }

    public static void main(String[] args) {
        new LogDemo().run();
    }
}
