package com.black.lock;

import lombok.NonNull;

public interface Lock {

    /***
     * 尝试占有锁
     * @return 是否获取成功
     */
    boolean trylock();

    /**
     * 释放锁, 只有当前对象占有锁的情况下
     * 才会成功释放
     */
    void releaseLock();

    /**
     * 绑定一个对象
     * @param target 目标对象, 且不能为空
     */
    void bind(@NonNull Object target);
}
