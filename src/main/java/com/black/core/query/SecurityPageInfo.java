package com.black.core.query;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public class SecurityPageInfo{
    public <T> AicEntry<T> pag(int pageNum, int pageSize, List<T> dataSource){

        //不允许当前页<0
        assert pageNum > 0 : "pageNum <= 0";
        if (pageNum <= 0)
            throw new RuntimeException("pageNum 不能小于等于0");

        if (dataSource == null)
            throw new NullPointerException("dataSet is null");

        //将数据总集进行一次分割 分割开始：从第pageSize*pageNum-1 开始，前提pageNum不为1
        //startData:数据开始index，数据结束的index
        int startData, endData,
                total = dataSource.size(),
                e;

        List<T> aicResult;

        //for循环的目的是为了调节pagenum的值，保证在一个符合值,顺便给startData赋值
        for (;;){

            if (pageNum == 1
                    &&      // (startData=pageNum-1)==0 必为true
                    (startData = pageNum-1) == 0)
                break;

            if ((startData = pageSize*(pageNum-1)) > total
                    &&   // --pageNum > 0必为true
                    --pageNum > 0 )
                continue;
            break;
        }

        aicResult= (List<T>) dataSource.subList(startData,
                (e=pageNum*pageSize) > total? total : e );

        //构造AicEntry，实现结果集
        AicEntry<T> entry=new AicEntry(pageSize, pageNum, total,
                total%pageSize > 0 ? (total/pageSize) + 1 : total/pageSize,
                aicResult
        );

        return entry;
    }

    /**
     * 子类，AycPageInfo进行一次计算后返回的
     * 结果类
     */
    @Data
    @AllArgsConstructor
    public static class AicEntry<K>{

        int pageSize;       /* 每页的size*/

        int pageNum;        /* 当前页数*/

        int total;          /* 总数据量*/

        int pages;          /* 总页数*/

        List<K> aicResult;  /* 维护的结果集*/

        /** 判断是否为第一页 */
        public boolean isFirstPage()        {return pageNum == 1;}

        /** 判断是否为最后一页 */
        public boolean isLastPage()         {return pageNum == pages;}
    }
}
