package com.black.core.sql.code.listener;

import com.black.utils.LocalSet;

public class DictionaryControlManager {


    private static LocalSet<String> localSet = new LocalSet<>();

    public static void registerDictAlias(String alias){
        localSet.add(alias);
    }

    public static void clearAlias(){
        localSet.clear();
    }

    public static boolean isOpen(String alias){
        return localSet.contains(alias);
    }
}
