package com.black.nio.code.run;

import java.util.List;

public interface Future<C> {

    boolean isCancel();

    void cancel();

    boolean isDone();

    boolean isError();

    C get();

    C get(long timeout);

    List<FutureListener> getListeners();

    void addListener(FutureListener listener);
}
