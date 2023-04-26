package com.black.core.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TransferStationManager {

    private static final Set<String> shutdownServerNames = new HashSet<>();

    private static final Map<String, TransferStation> transferStations = new ConcurrentHashMap<>();

    public static void registerConsumer(DataConsumer consumer, String name){
        if (!transferStations.containsKey(name)){
            openTransferStation(name);
        }
        TransferStation station = transferStations.get(name);
        if (station != null){
            station.registerConsumer(consumer);
        }
    }

    public static void pushData(Data<?> data, String name){
        if (!transferStations.containsKey(name)){
            openTransferStation(name);
        }
        TransferStation station = transferStations.get(name);
        if (station != null){
            station.push(data);
        }
    }

    public static void openTransferStation(String name){
        if (!transferStations.containsKey(name)){
            if (shutdownServerNames.contains(name)){
                throw new IllegalStateException("transfer station server " + name + " is aleary shutdown");
            }
            TransferStation station = new DefaultTransferStation(name);
            transferStations.put(name, station);
        }
    }

    public static TransferStation getStation(String name){
        return transferStations.get(name);
    }

    public static Set<String> getAliveServerNames(){
        return transferStations.keySet();
    }

    public static Set<String> getShutdownServerNames() {
        return shutdownServerNames;
    }

    public static void shutdown(String name){
        TransferStation station = transferStations.get(name);
        station.shutdown();
        shutdownServerNames.add(name);
        transferStations.remove(name);
    }

    public static void restart(String name){
        if (!transferStations.containsKey(name) && shutdownServerNames.contains(name)){
            shutdownServerNames.remove(name);
            openTransferStation(name);
        }
    }
}
