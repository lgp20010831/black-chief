package com.black.project;

public class Project {


    public static void init(Class<?> mainClass){
        ProjectInitGenerator generator = new ProjectInitGenerator(Version.INIT_1_0_FINAL, mainClass);
        generator.init();
    }


}
