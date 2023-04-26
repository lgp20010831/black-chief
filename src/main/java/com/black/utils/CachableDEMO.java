package com.black.utils;

public class CachableDEMO {


    public static void main(String[] args) throws InterruptedException {
        DynamicCachable cachable = new DynamicCachable(5000, false);
        get(cachable);

    }

    static void get(DynamicCachable cachable) throws InterruptedException {
        int i = 0;
        for (int j = 0; j < 500; j++) {
            int finalI = j;
            cachable.get(j, u -> {
                return "rt" + finalI;
            });
        }
        for (;;){
            Thread.sleep(500);
            cachable.get("val", v -> {
                System.out.println("解析");
                return "hello";
            });
            System.out.println("获取缓存");
            if (i++ == 10)  {
                i = 0;
                Thread.sleep(10000);
            }
        }

    }

}
