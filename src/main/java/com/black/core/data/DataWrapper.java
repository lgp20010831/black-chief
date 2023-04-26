package com.black.core.data;

public class DataWrapper<Y> extends AbstractData<Y>{

    public DataWrapper(Y y) {
        super(y);
    }


    public static <Y> Data<Y> createData(Y y){
        return new DataWrapper<>(y);
    }

}
