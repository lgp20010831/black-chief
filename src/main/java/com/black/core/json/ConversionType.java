package com.black.core.json;

public interface ConversionType {

    /***
     * 类型转换方法,将 类型为 P 的转换成为类型为 R
     * {@link JSONConvert} 会主动映射, 根据返回值参数类型
     * @param param 传递的参数
     * @param <P> 将要被转换的类型
     * @param <R> 转换后返回的类型
     * @return 返回转换后结果
     */
    <P,R> R converts(P param);

}
