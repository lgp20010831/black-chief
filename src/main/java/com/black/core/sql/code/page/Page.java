package com.black.core.sql.code.page;


import lombok.*;

@Getter  @Setter @ToString
@AllArgsConstructor @NoArgsConstructor
public class Page<R> {

    Integer pageSize;       /* 每页的size*/

    Integer pageNum;        /* 当前页数*/

    int total;          /* 总数据量*/

    int pages;          /* 总页数*/

    /** 判断是否为第一页 */
    public boolean isFirstPage()        {return pageNum == 1;}

    /** 判断是否为最后一页 */
    public boolean isLastPage()         {return pageNum == pages;}


    public void setTotal(int total) {
        this.total = total;
        pages = total /pageSize + ((total % pageSize == 0) ? 0 : 1);
    }
}
