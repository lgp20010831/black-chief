package com.black.core.query;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/***
 * The internal stack is in the form of array.
 * The length of the stack is fixed and does
 * not have the capacity to expand. Multi thread
 * operation can be supported through lock
 * @param <E> element type
 */
public class ArrayStack<E> implements Stack<E> {

    /**
     * The core of the array stack,
     * where the elements are stored
     */
    protected Object[] data;

    /***
     * Display the subscript, which is always equal
     * to the actual number of elements in the core array.
     * Atomic packaging is used to ensure stability in multithreading
     */
    protected final AtomicInteger index = new AtomicInteger(0);

    /***
     * Main tools supporting multithreading
     */
    protected final ReentrantLock lock = new ReentrantLock();

    /***
     * Unique construction method
     * @param len stack capacity
     */
    public ArrayStack(int len){
        if (len <= 0)
            throw new IllegalArgumentException("len must > 0");
        data = new Object[len];
    }

    /***
     * Displays the element at the top
     * of the stack. If it does not exist,
     * it is empty
     * @return top of element
     */
    @Override
    public E peek() {
        int i = index();
        return (E) data[Math.max(i, 0)];
    }

    /***
     * Returns the subscript of an array that can be manipulated directly
     * @return index, possible - 1
     */
    protected int index(){
        return size() - 1;
    }

    /**
     * Returns how many elements exist in the stack
     * @return size
     */
    @Override
    public int size() {
        return index.get();
    }

    /***
     * Returns whether the stack is empty
     * @return is empty
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /***
     * Remove the element at the top of the stack
     * @return Returns the stack top element to be removed
     */
    @Override
    public E poll() {
        lock.lock();
        try {
            return poll0();
        }finally {
            lock.unlock();
        }
    }

    /***
     * poll The main implementation of operation.
     * Other implementation methods are provided by subclasses
     */
    protected E poll0(){
        Object e = data[Math.max(index(), 0)];
        data[index()] = null;
        index.decrementAndGet();
        return (E) e;
    }

    /***
     * Put the element at the top of the stack
     * @param e element
     */
    @Override
    public void push(E e) {
        lock.lock();
        try {
            push0(e);
        }finally {
            lock.unlock();
        }
    }

    protected void push0(E e){
        int size = size();
        if (size >= data.length){
            capacityExpansion();
            push(e);
        }else {
            data[index() + 1] = e;
            this.index.incrementAndGet();
        }
    }

    /***
     * The capacity expansion method is implemented by subclasses.
     * This class does not support capacity expansion and will
     * immediately throw an excess capacity exception
     */
    protected void capacityExpansion(){
        throw new UpperCapacityLimitException("capacity is " + data.length);
    }

    /***
     * Positive order shows the specified number
     * of elements, starting from the top of the stack
     * If the specified number is greater than the number
     * of elements in the stack, an exception is thrown
     * @param len specified quantity
     * @return return the display elements in an array in order
     */
    @Override
    public E[] peeksAsc(int len) {
        if (len > size() || len <= 0){
            throw new IllegalArgumentException("len must not be > size or > 0");
        }
        Object[] es = new Object[len];
        for (int i = 0; i < len; i++) {
            es[i] = data[size() - (i + 1)];
        }
        return (E[]) es;
    }

    /***
     * Positive order shows the specified number
     * of elements, starting from the top of the stack
     * If the specified number is greater than the number
     * of elements in the stack, an exception is thrown
     * @param len specified quantity
     * @return return the display elements in an array in order
     */
    @Override
    public E[] peeksDesc(int len) {
        if (len > size() || len <= 0){
            throw new IllegalArgumentException("len must not be > size or > 0");
        }
        Object[] es = new Object[len];
        for (int i = len; i > 0; i--) {
            es[len - i] = data[size() - i];
        }
        return (E[]) es;
    }

    /***
     * empty the elements in the stack
     */
    @Override
    public void clear() {
        lock.lock();
        try {
            for (int i = 0; i < index.get(); i++) {
                data[i] = null;
            }
            index.set(0);
        }finally {
            lock.unlock();
        }
    }

    /***
     * After copying a copy of the data in the stack,
     * it is converted into the encapsulated class of the array
     * @return Encapsulation class of array
     */
    @Override
    public Array<E> toArray() {
        return toArray(true, copy());
    }

    /***
     * After copying a copy of the data in the stack,
     * it is converted into the encapsulated class of the array
     * @return Array encapsulation class obtained in reverse order
     */
    @Override
    public Array<E> toArrayDesc() {
        return toArray(false, copy());
    }

    protected Array<E> toArray(boolean asc, E[] data){
        return asc ? new AscArray<>(data) : new DescArray<>(data);
    }

    /***
     * Directly get the array in the stack
     * @param copy Whether to return a copy of the array in the stack
     * @return array
     */
    @Override
    public E[] array(boolean copy) {
        if (copy){
           return copy();
        }
        return (E[]) data;
    }

    protected E[] copy(){
        Object[] newData = new Object[data.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        return (E[]) newData;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isEmpty()){
            return "empty stack@" + hashCode();
        }else {
            for (int i = index(); i >= 0 ; i--) {
                builder.append("| ");
                Object e = data[i];
                if (e != null){
                    builder.append(limitStr(e.toString()));
                }else {
                    builder.append(limitStr("null"));
                }
                builder.append(" |\n");
            }
            builder.append("|-------|").append("@").append(hashCode());
        }
        return builder.toString();
    }

    String limitStr(String str){
        if (str == null || "".equals(str)){
            return "     ";
        }else {
            if (str.length() > 5){
                str = str.substring(0, 5);
            }else if (str.length() < 5){
                int length = str.length();
                StringBuilder strBuilder = new StringBuilder(str);
                for (int i = 0; i < 5 - length; i++) {
                    strBuilder.append(" ");
                }
                str = strBuilder.toString();
            }
            return str;
        }
    }
}
