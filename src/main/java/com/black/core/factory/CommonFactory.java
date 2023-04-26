package com.black.core.factory;

//该接口提供了创建对象的能力
//p 为 模糊了获取的方式与参数
public interface CommonFactory<RAW, PRO> extends Factory {

    PRO get(RAW p);
}
