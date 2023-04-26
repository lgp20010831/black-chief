package com.black.core.log;

import com.black.core.util.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IoLog {


    void setPrefix(String prefix);

    /**
     * get the status of the current log.
     * If the log has status control enabled
     * If - 1 is returned, it indicates that
     * the log system does not support state control
     * @return int of state
     */
    default int getState(){
        return -1;
    }

    /***
     * The displayed setting state and the state change
     * of the manual intervention log, provided that the
     * log has enabled the state control and allows the
     * manual intervention state change
     * @param state new state
     */
    default void setState(int state){

    }

    /***
     * print program info level information
     * It is allowed to use {} expressions to
     * explicitly pass parameters
     * @param msg message
     * @param params params
     * @return and the log system is defined according to logic
     */
    int info(Object msg, Object... params);

    /***
     * print program debug level information
     * It is allowed to use {} expressions to
     * explicitly pass parameters
     * @param msg message
     * @param params params
     * @return and the log system is defined according to logic
     */
    int debug(Object msg, Object... params);


    default int error(Throwable e){
        return error(e, "");
    }

    default int error(Object msg, Object... params){
        return error(null, msg, params);
    }

    /**
     * print program error level information
     * It is allowed to use {} expressions to
     * explicitly pass parameters
     * and will output the exception stack of the incoming exception
     * @param e throwable
     * @param msg message
     * @param params params
     * @return and the log system is defined according to logic
     */
    int error(Throwable e, Object msg, Object... params);

    /***
     * print program trace level information
     * It is allowed to use {} expressions to
     * explicitly pass parameters
     * @param msg message
     * @param params params
     * @return and the log system is defined according to logic
     */
    int trace(Object msg, Object... params);

    /**
     * whether to allow log printing at INFO level
     * @return support
     */
    default boolean isInfoEnabled(){
        return true;
    }

    /**
     * whether to allow log printing at DEBUG level
     * @return support
     */
    default boolean isDebugEnabled(){
        return true;
    }

    /**
     * whether to allow log printing at TRACE level
     * @return support
     */
    default boolean isTraceEnabled(){
        return true;
    }

    /**
     * whether to allow log printing at ERROR level
     * @return support
     */
    default boolean isErrorEnabled(){
        return true;
    }

    /***
     * If the log system supports delay,
     * this method can obtain one item without
     * the specified subscript of the refreshed data.
     * This method is easy to throw the subscript exceeding
     * exception, If delay is not supported, it may be thrown
     * UnsupportedOperationException
     * @param index index of stack
     * @return a log message of hits
     */
    default String get(int index){
        throw new UnsupportedOperationException("un support stack");
    }

    /***
     * set the delay of the log system,
     * provided that the log system supports setting the delay
     * @param delay delay value
     */
    default void enabledDelay(boolean delay){

    }

    /***
     * If the system supports delay operation,
     * this method will brush out the data whose
     * delay is brushed out. If delay is not enabled,
     * calling this method will not play a substantive
     * role
     * @return returns how many pieces of data have been brushed out
     */
    default int flush(){
        return flush(-1);
    }

    /***
     * refresh only one piece of data with a certain
     * subscript. If the subscript is - 1, it means refresh all
     * @param index index
     * @return returns how many pieces of data have been brushed out
     */
    int flush(int index);

    /***
     * close the log system, and the specific
     * processing is realized by the log system
     */
    default void close(){

    }

    /***
     * If the latency is enabled, get byte input
     * stream of delay cache data
     * @return inputStream
     * @throws IOException IO errors that may occur during the process
     */
    default InputStream getInputStream() throws IOException {
        throw new IOException("not support get inputStream");
    }

    /***
     * If the latency is enabled, the log data of
     * the latency cache is written to the specified
     * output stream
     * @param out outputStream
     * @throws IOException IO errors that may occur during the process
     */
    default void writeOutputStream(OutputStream out) throws IOException {
        InputStream in = getInputStream();
        int b;
        while ((b = in.read()) != -1){
            b = ByteUtils.read((byte) b);
            out.write(b);
        }
        in.close();
    }

    /***
     * The delayed cached information is spliced back through the builder
     * @return strings
     */
    default String stringStack(){
        throw new UnsupportedOperationException("no stack wired string");
    }

}
