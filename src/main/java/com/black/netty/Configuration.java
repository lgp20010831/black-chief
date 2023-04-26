package com.black.netty;

import com.black.netty.branch.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Configuration {

    /**
     * Bound IP address
     */
    private String ip;

    /**
     * Bound port
     * default port is 9876
     */
    private int port = 9876;

    /**
     * Print some basic logs, not in netty,
     * but in this packaging framework
     */
    private boolean printLog = true;

    /**
     * This callback is triggered when an event
     * of a read operation is received
     */
    private ReadMessage readMessage;

    /**
     * Triggered when the read event operation ends
     */
    private ReadComplete readComplete;

    /**
     * The callback is triggered after
     * the connection operation is completed
     */
    private ConnectComplete connectComplete;

    /**
     * When the service is started successfully,
     * an event is triggered for callback
     */
    private StartComplete startComplete;

    /**
     * This callback is triggered when an
     * exception occurs in the IO event
     */
    private ThrowableCaught throwableCaught;

    /***
     * Triggered when it detects that it is about to close
     */
    private CloseSettlement closeSettlement;

    /**
     * Triggered when it is detected that the connected
     * object sends a close event, not when it closes itself
     */
    private LossConnection lossConnection;
}
