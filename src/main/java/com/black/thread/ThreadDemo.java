package com.black.thread;

import com.black.core.util.Utils;
import lombok.extern.log4j.Log4j2;

/**
 * @author 李桂鹏
 * @create 2023-06-13 9:40
 */
@SuppressWarnings("all") @Log4j2
public class ThreadDemo {

    public static void main(String[] args) {
        ThreadPool pool = new ThreadPool(8);
        pool.setAdequateResources(true);
        for (int i = 0; i < 120; i++) {
            pool.execute(() -> {
                log.info("执行业务");
                Utils.sleep(2000);
            });
        }

    }
}
