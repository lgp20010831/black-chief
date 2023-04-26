package com.black.utils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class AverageCalculator {

    public static void run(Runnable runnable, int size){
        long start = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            runnable.run();
        }
        long end = System.currentTimeMillis();
        log.info("程序运行次数:{}, 平均花费时间: {} ms", size, (end - start)/size);
    }

    public static void runn(Runnable runnable, int size){
        long start = System.nanoTime();
        for (int i = 0; i < size; i++) {
            runnable.run();
        }
        long end = System.nanoTime();
        log.info("程序运行次数:{}, 平均花费时间: {} ns", size, (end - start)/size);
    }

}
