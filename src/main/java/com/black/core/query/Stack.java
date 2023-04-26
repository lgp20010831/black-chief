package com.black.core.query;

public interface Stack<E> {

    /***
     * Displays the element at the top
     * of the stack. If it does not exist,
     * it is empty
     * @return top of element
     */
    E peek();

    /**
     * Returns how many elements exist in the stack
     * @return size
     */
    int size();

    /***
     * Returns whether the stack is empty
     * @return is empty
     */
    boolean isEmpty();

    /***
     * Remove the element at the top of the stack
     * @return Returns the stack top element to be removed
     */
    E poll();

    /***
     * Put the element at the top of the stack
     * @param e element
     */
    void push(E e);

    /***
     * Positive order shows the specified number
     * of elements, starting from the top of the stack
     * If the specified number is greater than the number
     * of elements in the stack, an exception is thrown
     * @param len specified quantity
     * @return return the display elements in an array in order
     */
    E[] peeksAsc(int len);

    /***
     * Positive order shows the specified number
     * of elements, starting from the top of the stack
     * If the specified number is greater than the number
     * of elements in the stack, an exception is thrown
     * @param len specified quantity
     * @return return the display elements in an array in order
     */
    E[] peeksDesc(int len);

    /***
     * empty the elements in the stack
     */
    void clear();

    /***
     * After copying a copy of the data in the stack,
     * it is converted into the encapsulated class of the array
     * @return Encapsulation class of array
     */
    Array<E> toArray();

    /***
     * After copying a copy of the data in the stack,
     * it is converted into the encapsulated class of the array
     * @return Array encapsulation class obtained in reverse order
     */
    Array<E> toArrayDesc();

    /***
     * Directly get the array in the stack
     * @param copy Whether to return a copy of the array in the stack
     * @return array
     */
    E[] array(boolean copy);
}
