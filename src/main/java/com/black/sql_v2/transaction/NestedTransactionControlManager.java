package com.black.sql_v2.transaction;

import com.black.utils.LocalSet;

public class NestedTransactionControlManager {


    private static final LocalSet<String> executorActiveLocal = new LocalSet<>();

    public static boolean isAtive(String name){
        return executorActiveLocal.contains(name);
    }

    public static void registerTransaction(String name){
        executorActiveLocal.add(name);
    }

    public static void closeTransaction(String name){
        executorActiveLocal.remove(name);
    }
}
