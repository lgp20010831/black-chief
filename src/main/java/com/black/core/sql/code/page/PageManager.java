package com.black.core.sql.code.page;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PageManager {

    private static final ThreadLocal<Page<?>> pageLocal = new ThreadLocal<>();

    public static boolean isOpenPage(){
        return pageLocal.get() != null;
    }

    public static Page<?> getPage(){
        return pageLocal.get();
    }

    public static void registerPage(Page<?> page){
        if (isOpenPage()){
            if (log.isWarnEnabled()) {
                log.warn("replace a page that will be opened");
            }
        }
        pageLocal.set(page);
    }

    public static void close(){
        pageLocal.remove();
    }
}
