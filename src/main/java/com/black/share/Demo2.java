package com.black.share;

import com.black.core.util.Utils;
import com.black.utils.IdUtils;

/**
 * @author 李桂鹏
 * @create 2023-05-04 11:55
 */
@SuppressWarnings("all")
public class Demo2 {

    public static void main(String[] args) {
        ShareMap map = new ShareMap();
        for (;;){
            Utils.sleep(2000);
            System.out.println(map.keySet());
        }
    }
}
