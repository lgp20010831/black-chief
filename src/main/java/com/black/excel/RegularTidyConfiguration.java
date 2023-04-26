package com.black.excel;

import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

//规律和整齐的excel格式控制器
@Setter @Getter
public class RegularTidyConfiguration extends Configuration{

    RegularTidyConfiguration(){
        setExcelReader(new RegularTidyExcelReader());
    }

    //标题所在的行
    private int titleRow = 0;

    //标题行缩进
    private int titleRowIndent = 0;

    //标题行标题数量, 默认读完为止
    private int titleRowLength = -1;

    //内容起始行, 默认值为 titleRow + 1
    private int contextStartRow = -1;

    //内容结束行, 默认值为所有行
    private int contextEndRow = -1;

    //解析值的时候如果出现异常则设置为空, 否则抛出异常
    private boolean analyticValueErrorThenSetNull = true;

    //跳过的行
    private List<Integer> skipRows = new ArrayList<>();

    //额外读取的行
    private List<Integer> includeRows = new ArrayList<>();

    private LinkedBlockingQueue<RegularTidyExcelListener> listeners = new LinkedBlockingQueue<>();

    public RegularTidyConfiguration addListener(RegularTidyExcelListener listener){
        listeners.add(listener);
        return this;
    }

    public RegularTidyConfiguration addSkipRows(Integer... rows){
        skipRows.addAll(Arrays.asList(rows));
        return this;
    }

    public RegularTidyConfiguration addIncludeRows(Integer... rows){
        includeRows.addAll(Arrays.asList(rows));
        return this;
    }
}
