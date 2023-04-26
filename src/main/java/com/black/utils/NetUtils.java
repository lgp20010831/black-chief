package com.black.utils;

import com.black.holder.SpringHodler;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Query;
import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class NetUtils {


    public static Set<String> getLocalIp4Address() {
        Set<String> result = new HashSet<>();
        List<Inet4Address> list = null;
        try {
            list = getLocalIp4AddressFromNetworkInterface();
            list.forEach(d -> {
                result.add(d.toString().replaceAll("/", ""));
            });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return result;
    }


    /*
     * 获取本机所有网卡信息   得到所有IPv4信息
     * @return Inet4Address>
     */
    public static List<Inet4Address> getLocalIp4AddressFromNetworkInterface() throws SocketException {
        List<Inet4Address> addresses = new ArrayList<>(8);
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        if (networkInterfaces == null) {
            return addresses;
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    addresses.add((Inet4Address) inetAddress);
                }
            }
        }
        return addresses;
    }
    public static String getObjectIpAddress(){
        try {
            String host = getNetObjectHost();
            int port = getNetObjectPost();
            if (port == -1){
                port = getTomcatSpringEnvPort();
            }
            return "http" + "://" + host + ":" + port;
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }

    }

    public static ObjectName getNetObjectName(){
        try {
            MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                    Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
            return CollectionUtils.firstElement(objectNames);
        }catch (Throwable e){
            throw new IllegalStateException(e);
        }
    }

    public static int getNetObjectPost(){
        ObjectName netObjectName = getNetObjectName();
        return netObjectName != null ? Integer.parseInt(netObjectName.getKeyProperty("port")) : -1;
    }

    public static String getNetObjectHost(){
        Set<String> localIp4Address = getLocalIp4Address();
        localIp4Address.remove("127.0.0.1");
        return CollectionUtils.firstElement(localIp4Address);
    }

    public static int getTomcatSpringEnvPort(){
        ApplicationContext context = SpringHodler.getApplicationContext();
        if (context instanceof ServletWebServerApplicationContext){
            return ((ServletWebServerApplicationContext) context).getWebServer().getPort();
        }
        return -1;

    }

    public static void main(String[] args) {
        System.out.println(getNetObjectHost());
    }


}
