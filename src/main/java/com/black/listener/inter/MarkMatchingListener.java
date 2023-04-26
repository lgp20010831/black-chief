package com.black.listener.inter;

import lombok.NonNull;

public interface MarkMatchingListener<T> extends Listener<T>{

    boolean matching(@NonNull Object sign);

}
