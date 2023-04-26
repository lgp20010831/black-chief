package com.black.core.data;

import java.io.Serializable;

public interface DataListener extends Serializable {

    boolean supportStatus(DataStatus status);

    void statusCallBack(Data<?> data);
}
