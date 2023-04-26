package com.black.core.factory.beans.dome;

import com.black.core.factory.beans.InitMethod;

public class SOn {


    @InitMethod
    public void init(SonSon sonSon){

    }


    public static class SonSon{

        @InitMethod
        public void init(int i){}
    }
}
