package com.black.function;

import java.io.File;

public interface FileFilter {

    //在接下来过滤的流程中是否包含当前 file
    default boolean containFile(File file){
        return true;
    }

    //替换当前文件参加接下来的流程
    default File replaceFile(File file){
        return file;
    }

    //判断, 适用于各种情况
    boolean judge(File file);
}
