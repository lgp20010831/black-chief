package com.black.core.sql.code.page;

public class PageHelper {


    public static Page<?> openPage(int pageNum, int pageSize){
        Page<Object> page = new Page<>();
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        PageManager.registerPage(page);
        return page;
    }

}
