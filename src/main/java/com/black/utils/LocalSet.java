package com.black.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LocalSet<E> extends AbstractLocal<Set<E>> implements Set<E>{
    @Override
    Set<E> create() {
        return new HashSet<>();
    }

    @Override
    public int size() {
        return current().size();
    }

    @Override
    public boolean isEmpty() {
        return current().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return current().contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return current().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return current().toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return current().toArray(a);
    }

    @Override
    public boolean add(E e) {
        return current().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return current().remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return current().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        return current().addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return current().removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return current().retainAll(c);
    }

    @Override
    public void clear() {
        current().clear();
    }

    @Override
    public String toString() {
        return current().toString();
    }
}
