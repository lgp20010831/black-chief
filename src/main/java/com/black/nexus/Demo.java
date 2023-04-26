package com.black.nexus;

public class Demo {


    public static void main(String[] args) {
        Configuration configuration = new Configuration("http://10.20.252.204:8081/service/rest/repository/browse/maven-etouwa/")
                .addStartRoot("com")
                .setDownUrl("http://10.20.252.204:8081/repository/maven-etouwa/")
                .setFileStreamReader(new DownloadLocalFileReader("E:\\cloneJar0222"));
        new NexusContext(configuration).start();
    }

}
