package com.black.core.asyn;

@AsynAdministrators
public class DefaultAsynAdministrators implements ManageConfiguration{

    public static boolean printConfiguration = false;

    @Override
    public void postConfiguration(AsynConfiguration configuration) {
        if (printConfiguration){
            System.out.println(configuration);
        }
    }
}
