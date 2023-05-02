package com.black.sql_v2.serialize;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class GlobalSerializerAdvocateManager {

    private final static LinkedBlockingQueue<SerializeAdvocate> advocateQueue = new LinkedBlockingQueue<>();

    static {
        advocateQueue.add(new SqlSerializerAdapterAdvocate());
        advocateQueue.add(new JSONSerializerAdvocate());
    }

    public static LinkedBlockingQueue<SerializeAdvocate> getAdvocateQueue() {
        return advocateQueue;
    }

    public static void register(SerializeAdvocate... advocates){
        advocateQueue.addAll(Arrays.asList(advocates));
    }

}
