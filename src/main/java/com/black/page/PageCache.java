package com.black.page;

import com.black.core.sql.code.page.Page;

public class PageCache {

    private static final ThreadLocal<Page<?>> cache = new ThreadLocal<>();

    private static boolean isOpen(){
        return cache.get() != null;
    }

    public static void remove(){
        cache.remove();
    }

    public static Page<?> getPage(){
        return cache.get();
    }

    public static void setPage(Page<?> page){
        cache.set(page);
    }
}
