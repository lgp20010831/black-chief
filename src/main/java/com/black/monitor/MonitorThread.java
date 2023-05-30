package com.black.monitor;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class MonitorThread extends Thread{

    private final List<Monitor<?, ?>> monitors;

    private volatile boolean shutdown = false;

    public MonitorThread() {
        monitors = new ArrayList<>();
    }

    public void shutdown(){
        shutdown = true;
    }

    public int monitorSize(){
        return monitors.size();
    }

    public void registerMonitor(Monitor<?, ?> monitor){
        monitors.add(monitor);
    }

    @Override
    public void run() {
        while (!shutdown){

            long recently = -1;
            for (Monitor<?, ?> monitor : monitors) {
                long inspect = monitor.inspect();
                recently = recently == -1 ? inspect : Math.min(inspect, recently);
            }
            if (recently == -1){
                //park
            }else {
                //sleep
            }
        }
    }
}
