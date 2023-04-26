package com.black.utils;

import java.util.Collection;
import java.util.HashSet;

public class FutureSet<E> extends FutureCollection<E>{
    @Override
    Collection<E> create() {
        return new HashSet<>();
    }
}
