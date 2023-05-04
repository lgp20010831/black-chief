package com.black.share;

import com.black.core.util.Utils;
import com.black.io.Handler;
import com.black.utils.IdUtils;

/**
 * @author 李桂鹏
 * @create 2023-05-04 11:54
 */
@SuppressWarnings("all")
public class Demo1 {

    static void l1(){
        ShareMap map = new ShareMap();
        int i = 0;
        while (i++ < 10){
            System.out.println(i);
            Utils.sleep(3000);
            map.put(IdUtils.createShort8Id(), "1");
        }
        map.shutdown();
    }

    public static void main(String[] args) {
        ShareMap shareMap = new ShareMap();
        for (int i = 0; i < 5; i++) {
            shareMap.put(IdUtils.createShort8Id(), "1");
        }

    }
}
