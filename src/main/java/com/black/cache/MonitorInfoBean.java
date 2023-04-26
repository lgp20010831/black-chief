package com.black.cache;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.StringTokenizer;

@Getter @Setter @ToString
public class MonitorInfoBean {

    /** 可使用内存. */

    private long totalMemory;

    /** 剩余内存. */

    private long freeMemory;

    /** 最大可使用内存. */

    private long maxMemory;

    /** 操作系统. */

    private String osName;

    /** 总的物理内存. */

    private long totalMemorySize;

    /** 剩余的物理内存. */

    private long freePhysicalMemorySize;

    /** 已使用的物理内存. */

    private long usedMemory;

    /** 线程总数. */

    private int totalThread;

    /** cpu使用率. */

    private double cpuRatio;

    private static final int CPUTIME = 30;

    private static final int PERCENT = 100;

    private static final int FAULTLENGTH = 10;

    private static final File versionFile = new File("/proc/version");

    private static String linuxVersion = null;

    public MonitorInfoBean(){
        int kb = 1024;
// 可使用内存
        long totalMemory = Runtime.getRuntime().totalMemory() / kb;
// 剩余内存
        long freeMemory = Runtime.getRuntime().freeMemory() / kb;
// 最大可使用内存
        long maxMemory = Runtime.getRuntime().maxMemory() / kb;
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

// 操作系统
        String osName = System.getProperty("os.name");

// 总的物理内存
        long totalMemorySize = osmxb.getTotalPhysicalMemorySize() / kb;

// 剩余的物理内存
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize() / kb;

// 已使用的物理内存
        long usedMemory = (osmxb.getTotalPhysicalMemorySize() - osmxb.getFreePhysicalMemorySize()) / kb;

// 获得线程总数
        ThreadGroup parentThread;
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
                .getParent() != null; parentThread = parentThread.getParent());
        int totalThread = parentThread.activeCount();
        double cpuRatio = 0;
        if (osName.toLowerCase().startsWith("windows")) {
            cpuRatio = 0;
        } else {
            cpuRatio = getCpuRateForLinux();
        }

// 构造返回对象
        setFreeMemory(freeMemory);
        setFreePhysicalMemorySize(freePhysicalMemorySize);
        setMaxMemory(maxMemory);
        setOsName(osName);
        setTotalMemory(totalMemory);
        setTotalMemorySize(totalMemorySize);
        setTotalThread(totalThread);
        setUsedMemory(usedMemory);
        setCpuRatio(cpuRatio);
    }

    private static double getCpuRateForLinux() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader brStat = null;
        StringTokenizer tokenStat = null;

        try {
            System.out.println("Get usage rate of CUP , linux version: " + linuxVersion);
            Process process = Runtime.getRuntime().exec("top -b -n 1");
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            brStat = new BufferedReader(isr);
            if (linuxVersion.equals("2.4")) {
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();
                brStat.readLine();
                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                String user = tokenStat.nextToken();
                tokenStat.nextToken();
                String system = tokenStat.nextToken();
                tokenStat.nextToken();
                String nice = tokenStat.nextToken();
                System.out.println(user + " , " + system + " , " + nice);
                user = user.substring(0, user.indexOf("%"));
                system = system.substring(0, system.indexOf("%"));
                nice = nice.substring(0, nice.indexOf("%"));
                float userUsage = new Float(user);
                float systemUsage = new Float(system);
                float niceUsage = new Float(nice);
                return (userUsage + systemUsage + niceUsage) / 100;
            } else {
                brStat.readLine();
                brStat.readLine();
                tokenStat = new StringTokenizer(brStat.readLine());
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                tokenStat.nextToken();
                String cpuUsage = tokenStat.nextToken();
                System.out.println("CPU idle : " + cpuUsage);
                Float usage = new Float(cpuUsage.substring(0, cpuUsage.indexOf("%")));
                return (1 - usage / 100);
            }

        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
            return 1;
        } finally {

            try {
                is.close();
                isr.close();
                brStat.close();
            } catch (IOException e) {
            }
        }
    }
}
