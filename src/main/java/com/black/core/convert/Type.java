package com.black.core.convert;

import com.black.core.Beacon;
import com.black.vfs.VfsLoader;

import java.util.Set;

public class Type {


    public static void load(){
        load(Beacon.getPackageName());
    }

    public static void load(String range){
        GlobalTypeConvertComponent component = new GlobalTypeConvertComponent();
        VfsLoader loader = new VfsLoader();
        Set<Class<?>> set = loader.load(range);
        component.scan(set);
    }



}
