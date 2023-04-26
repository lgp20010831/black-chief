package com.black.core.query;

import java.util.List;
import java.util.Set;

/***
 * encapsulate the array to make it easier to operate
 * @param <E> element type
 */
public interface Array<E> extends Iterable<E> {

    /***
     * Get the elements in the array
     * @param i index
     * @return element
     */
    E get(int i);

    /***
     * Stores a data in the array according
     * to the specified subscript
     * @param i point index
     * @param element element
     * @return self
     */
    Array<E> put(int i, E element);

    /***
     * Return array length
     * @return len
     */
    int length();

    /***
     * Directly return the array itself
     * @return array
     */
    E[] array();

    /***
     * Merge an array, the length of the new array will
     * be the length of the original array plus the length
     * of the merged array, and the data will also be copied
     * to the new array
     * @param ne array source
     * @return self
     */
    Array<E> merge(E[] ne);

    /***
     * Set the elements in the array to null
     * @return self
     */
    Array<E> clear();

    /***
     * expansion array. The internal data of the
     * expanded area is empty
     * @param extraLength length to expand
     * @return self
     */
    Array<E> expand(int extraLength);

    /***
     * The length of the array will be discarded
     * and the redundant data will be shrunk
     * @param shrinkageLength shrinkage to length
     * @return self
     */
    Array<E> shrink(int shrinkageLength);

    /***
     * Copy the data in the data source array to
     * the meta array and specify where to start
     * the assignment
     * @param target source
     * @param offset The starting position in the meta array to accept data
     * @return self
     */
    Array<E> copy(E[] target, int offset);

    /***
     * convert to set
     * @return list
     */
    List<E> toList();

    /***
     * convert to set
     * @return set
     */
    Set<E> toSet();
}
