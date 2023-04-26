package com.black.core.query;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AscArray<E> implements Array<E>{

    /** core array */
    protected E[] array;

    public AscArray(@NonNull E[] array){
        this.array = array;
    }

    /***
     * Get the elements in the array
     * @param i index
     * @return element
     */
    @Override
    public E get(int i){
        if (i < 0 || i >= array.length){
            throw new IndexOutOfBoundsException("" + i);
        }
        return array[i];
    }

    /***
     * Stores a data in the array according
     * to the specified subscript
     * @param i point index
     * @param element element
     * @return self
     */
    @Override
    public AscArray<E> put(int i, E element){
        if (i < 0 || i >= array.length){
            throw new IndexOutOfBoundsException("" + i);
        }
        array[i] = element;
        return this;
    }

    /***
     * Return array length
     * @return len
     */
    @Override
    public int length() {
        return array.length;
    }

    /***
     * Directly return the array itself
     * @return array
     */
    @Override
    public E[] array(){
        return array;
    }

    /***
     * Merge an array, the length of the new array will
     * be the length of the original array plus the length
     * of the merged array, and the data will also be copied
     * to the new array
     * @param ne array source
     * @return self
     */
    @Override
    public Array<E> merge(@NonNull E[] ne) {
        Object[] newData = new Object[ne.length + length()];
        System.arraycopy(array, 0, newData, 0, length());
        System.arraycopy(ne, 0, newData, newData.length - ne.length, ne.length);
        array = (E[]) newData;
        return this;
    }

    /***
     * Set the elements in the array to null
     * @return self
     */
    @Override
    public Array<E> clear() {
        for (int i = 0; i < length(); i++) {
            array[i] = null;
        }
        return this;
    }

    /***
     * expansion array. The internal data of the
     * expanded area is empty
     * @param extraLength length to expand
     * @return self
     */
    @Override
    public Array<E> expand(int extraLength) {
        Object[] newArray = new Object[length() + extraLength];
        System.arraycopy(array, 0, newArray, 0, length());
        array = (E[]) newArray;
        return this;
    }

    /***
     * The length of the array will be discarded
     * and the redundant data will be shrunk
     * @param shrinkageLength shrinkage to length
     * @return self
     */
    @Override
    public Array<E> shrink(int shrinkageLength) {
        if (shrinkageLength > length()){
            throw new IndexOutOfBoundsException("" + (length() - shrinkageLength));
        }
        Object[] newArray = new Object[length() - shrinkageLength];
        System.arraycopy(array, 0, newArray, 0, newArray.length);
        array = (E[]) newArray;
        return this;
    }

    /***
     * Copy the data in the data source array to
     * the meta array and specify where to start
     * the assignment
     * @param target source
     * @param offset The starting position in the meta array to accept data
     * @return self
     */
    @Override
    public Array<E> copy(E[] target, int offset) {
        if (target != null){
            int ti = target.length;
            System.arraycopy(target, 0, array, offset, Math.min(ti, length() - offset));
        }
        return this;
    }

    @Override
    public List<E> toList() {
        return new ArrayList<>(Arrays.asList(array.clone()));
    }

    @Override
    public Set<E> toSet() {
        return new HashSet<>(Arrays.asList(array.clone()));
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return Arrays.stream(array).iterator();
    }

    @Override
    public String toString() {
        return Arrays.toString(array);
    }
}
