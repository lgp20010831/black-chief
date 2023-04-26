package com.black.model;

import java.io.IOException;

public class MODELDOME {


    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            System.out.println("程序退出");
        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            System.out.println("程序二次结束");
        }));
        Thread.sleep(1000);
        System.out.println("程序结束");
    }
}
