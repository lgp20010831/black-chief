package com.black.database.calcite;

/**
 * @author 李桂鹏
 * @create 2023-06-26 11:55
 */
@SuppressWarnings("all")
public interface MemoryColumn {

    String getName();

    Class<?> getJavaType();

    String getRemark();

    void setRemark(String remark);

}
