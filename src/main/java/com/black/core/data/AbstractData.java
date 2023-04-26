package com.black.core.data;

import java.util.Collection;
import java.util.HashSet;

public abstract class AbstractData<D> implements Data<D>{

    protected D data;
    protected DataStatus status;
    protected Collection<DataListener> listeners;

    public AbstractData(D data){
        listeners = initListenerQueue();
        this.data = data;
        setStatus(DataStatus.CREATED);
    }

    protected Collection<DataListener> initListenerQueue(){
        return new HashSet<>();
    }

    @Override
    public Collection<DataListener> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(DataListener listener) {
        if (listener != null){
            listeners.add(listener);
        }
    }

    @Override
    public void setStatus(DataStatus status) {
        this.status = status;
        for (DataListener listener : getListeners()) {
            if (listener.supportStatus(status)) {
                listener.statusCallBack(this);
            }
        }
    }

    @Override
    public D getInternalData() {
        return data;
    }

    @Override
    public DataStatus getStatus() {
        return status;
    }
}
