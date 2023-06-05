package com.black.compile.sj;

/**
 * @author 李桂鹏
 * @create 2023-06-05 16:59
 */
@SuppressWarnings("all")
public interface SjCompiler {

    /*
        将一段 sj 风格代码编译成 java 代码
        采用一行一行进行编译, 每一行如果存在多个语句则使用;分割
     */
    String complie(String code) throws UnableCompileSjException;


}
