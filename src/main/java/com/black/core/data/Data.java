package com.black.core.data;

import java.io.Serializable;
import java.util.Collection;

public interface Data<D> extends Serializable {

    Collection<DataListener> getListeners();

    void addListener(DataListener listener);

    void setStatus(DataStatus status);

    D getInternalData();

    DataStatus getStatus();
}
