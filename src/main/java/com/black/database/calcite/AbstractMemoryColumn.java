package com.black.database.calcite;

/**
 * @author 李桂鹏
 * @create 2023-06-26 13:33
 */
@SuppressWarnings("all")
public abstract class AbstractMemoryColumn implements MemoryColumn{

    protected final String name;

    protected final Class<?> javaType;

    protected String remark;

    public AbstractMemoryColumn(String name, Class<?> javaType) {
        this.name = name;
        this.javaType = javaType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getJavaType() {
        return javaType;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
