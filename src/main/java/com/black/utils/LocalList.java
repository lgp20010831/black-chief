package com.black.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LocalList<E> extends AbstractLocal<List<E>> implements List<E>{
    @Override
    List<E> create() {
        return new ArrayList<>();
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
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        return current().addAll(index, c);
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
    public E get(int index) {
        return current().get(index);
    }

    @Override
    public E set(int index, E element) {
        return current().set(index, element);
    }

    @Override
    public void add(int index, E element) {
        current().add(index, element);
    }

    @Override
    public E remove(int index) {
        return current().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return current().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return current().lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        return current().listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return current().listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return current().subList(fromIndex, toIndex);
    }
}
