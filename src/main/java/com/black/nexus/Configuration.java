package com.black.nexus;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter @Setter
public class Configuration {

    //private static String startRoot = "com/";
    private List<String> downloadRoot = new ArrayList<>();

    //private static final String NEXUS_REPOSITORY_ROOTURL = "http://10.20.252.204:8081/service/rest/repository/browse/maven-etouwa/";
    private String htmlUrl;

    //private static final String DOWNLOAD_PATH = "http://10.20.252.204:8081/repository/maven-etouwa/";
    private String downUrl;

    //private static final String LOCAL_DISK_NAME = "E:" + File.separator + "cloneJar0222";
    private FileStreamReader fileStreamReader;

    public Configuration(String htmlUrl) {
        this.htmlUrl = htmlUrl;
        downUrl = htmlUrl;
    }

    public Configuration setFileStreamReader(FileStreamReader fileStreamReader) {
        this.fileStreamReader = fileStreamReader;
        return this;
    }

    public Configuration setDownUrl(String downUrl) {
        this.downUrl = downUrl;
        return this;
    }

    public boolean downloadAll(){
        return downloadRoot.isEmpty();
    }

    public Configuration addStartRoot(String... roots){
        downloadRoot.addAll(Arrays.asList(roots));
        return this;
    }
}
