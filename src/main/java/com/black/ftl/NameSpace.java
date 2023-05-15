package com.black.ftl;

import java.util.StringJoiner;

/**
 * @author 李桂鹏
 * @create 2023-05-15 16:31
 */
@SuppressWarnings("all")
public interface NameSpace {

    String resolve(Object data);

    default String resolveParts(Object data){
        return resolveParts(new StringJoiner(""), data);
    }

    String resolveParts(StringJoiner joiner, Object data);

    String resolvePart(String id, Object data);
}
