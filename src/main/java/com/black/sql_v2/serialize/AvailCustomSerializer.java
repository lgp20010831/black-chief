package com.black.sql_v2.serialize;

/**
 * @author shkstart
 * @create 2023-04-27 16:51
 */
public interface AvailCustomSerializer {

    String toSerialize(Object attr);

    Object deSerialize(String text);
}
