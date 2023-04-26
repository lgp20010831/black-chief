package com.black.core.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hp lgp
 * 对pageinfo 实现更人性化的简化,但不需要依赖pageInfo
 *
 * 流程:首先对一个数据表进行分页，先获取其数据表的所有数据的集合
 *
 * 如果要获取一个数据表的分页，就需要提供当前页，页size，AycPageInfo根据
 * 计算返回对应的list集合
 *
 * ---------------------------------------
 *使用步骤，每次分页需要传递一个数据源（list）
 *          updateDataSet就是更新数据源，然后主要方法
 *          pag进行分页
 */

@Component
@SuppressWarnings("all")
public class PageInfo {


        /**
         * 数据的总集，但这个数据的总集需要外部的更新
         * 初始化的结果集应该从pageInfo中获取
         */
        private List<?> dataSet;

        /**
         * 对数据总集的更新
         * @param newDataSet 新的数据总集
         * @return 返回当前AycPageInfo
         */
        public <T> PageInfo updateDataSet(List<T> newDataSet)
        {
            dataSet=newDataSet;
            return this;
        }

        /** 无参构造*/
        public PageInfo(){}

        /**
         * 加入传入的当前页是 第3页
         * 一共7个数据，size=4，最多只有两页
         * 在准备时先判断，如果当前页乘以size的数据量大于总数据量
         *
         * 开始页：第8个数据，已经超过了数据总量的下标
         * @param pageNum 当前页
         * @param pageSize 每页的size
         * @return 返回一个结果集
         */
        @SuppressWarnings("all")
        public <T> AicEntry<T> pag(int pageNum, int pageSize){

            //不允许当前页<0
            assert pageNum>0 : "pageNum<0";

            if (dataSet==null)
                throw new NullPointerException("dataSet is null");

            //将数据总集进行一次分割 分割开始：从第pageSize*pageNum-1 开始，前提pageNum不为1
            //startData:数据开始index，数据结束的index
            int startData,endData,
                    total=dataSet.size(),
                    e;

            List<T> aicResult;

            //for循环的目的是为了调节pagenum的值，保证在一个符合值,顺便给startData赋值
            for (;;){

                if (pageNum == 1
                        &&      // (startData=pageNum-1)==0 必为true
                        (startData = pageNum - 1) == 0)
                    break;

                if ((startData = pageSize * (pageNum - 1)) > total
                        &&   //--pageNum>0必为true
                        -- pageNum > 0)
                    continue;
                break;
            }

            aicResult = (List<T>) dataSet.subList(startData,
                    (e = pageNum * pageSize) > total ? total : e );

            //构造AicEntry，实现结果集
            AicEntry<T> entry = new AicEntry(pageSize, pageNum, total,
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
