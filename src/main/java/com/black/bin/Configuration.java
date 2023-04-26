package com.black.bin;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Configuration {

    //每种类型的对象最小在池中维护的数量
    private int coreObjectSize = 10;

    //实例化对象的方式
    private InstanceType instanceType = InstanceType.REFLEX;


}
