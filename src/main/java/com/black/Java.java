package com.black;

import com.black.core.json.Ignore;
import lombok.Getter;

@Getter
public class Java extends JsonBean{

    //版本号
    private String version;

    //提供商
    private String vendor;

    //jre目录
    private String jreHome;

    //Java虚拟机版本号
    private String vmVersion;

    //Java虚拟机名称
    private String vmName;

    //Java规范名称
    private String specificationName;

    //Java类版本号
    private String classVersion;

    @Ignore //Java类路径
    private String classPath;

    @Ignore //Java lib路径
    private String libPath;

    //Java输入输出临时路径
    private String ioTmpdir;

    //Java编译器
    private String compiler;

    //Java执行路径
    private String extDirs;

    //操作系统名称
    private String osName;

    //操作系统的架构
    private String osArch;

    //操作系统版本号
    private String osVersion;

    //文件分隔符
    private String fileSeparator;

    //路径分隔符
    private String pathSeparator;

    //直线分隔符
    private String lineSeparator;

    //操作系统用户名
    private String userName;

    //操作系统用户的主目录
    private String userHome;

    //当前程序所在目录
    private String userDir;

    public Java(){
        version = System.getProperty("java.version");
        vendor = System.getProperty("java.vendor");
        jreHome = System.getProperty("java.home");
        vmVersion = System.getProperty("java.vm.version");
        vmName = System.getProperty("java.vm.name");
        specificationName = System.getProperty("java.specification.version");
        classVersion = System.getProperty("java.class.version");
        classPath = System.getProperty("java.class.path");
        libPath = System.getProperty("java.library.path");
        ioTmpdir = System.getProperty("java.io.tmpdir");
        compiler = System.getProperty("java.compiler");
        extDirs = System.getProperty("java.ext.dirs");
        osName = System.getProperty("os.name");
        osArch = System.getProperty("os.arch");
        osVersion = System.getProperty("os.version");
        fileSeparator = System.getProperty("file.separator");
        pathSeparator = System.getProperty("path.separator");
        lineSeparator = System.getProperty("line.separator");
        userName = System.getProperty("user.name");
        userHome = System.getProperty("user.home");
        userDir = System.getProperty("user.dir");
    }
}
