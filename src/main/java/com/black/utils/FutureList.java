package com.black.utils;

import java.util.ArrayList;
import java.util.Collection;

public class FutureList<E> extends FutureCollection<E>{
    @Override
    Collection<E> create() {
        return new ArrayList<>();
    }
}
