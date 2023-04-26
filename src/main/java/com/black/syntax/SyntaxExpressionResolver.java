package com.black.syntax;

import java.util.Map;

//作者: 李桂鹏, 日期： 2022-07-30
public interface SyntaxExpressionResolver {

    /**
     * 是否支持解析该类型的表达式
     * @param item 通常传递整个表达式
     * @return 返回是否支持
     */
    boolean supportType(String item);

    /***
     * 将表达式中用来表示类型的字符去除掉
     * 通常在 supportType 返回 true 时调用
     * @param item 完整表达式
     * @return 去除类型标志后的表达式
     */
    String cutItem(String item);

    /**
     * 解析表达式
     * @param expression 表达式
     * @param source 外部参数, 数据源
     * @return 模糊的返回类型, 因为不同的表达式经过不同的处理器解析后返回的
     *         类型不会相同, 所以需要支持任意类型的返回值
     */
    Object resolver(String expression, Map<String, Object> source, SyntaxMetadataListener syntaxMetadataListener);
}
