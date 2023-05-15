package com.black.ftl;

import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Av0;
import com.black.utils.CountWare;

import java.util.Arrays;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-13 15:49
 */
@SuppressWarnings("all")
public class Demo {

    public static void main(String[] args) {
        ApplicationUtil.programRunMills(() -> {
            FtlResolver resolver = new FtlResolver();
            resolver.loadPackages("xml-sql");
            resolver.setNullDefaultValue("!无数据");
            NameSpace nameSpace = resolver.getNameSpace("xx.ftl");
            Map<String, Object> env = Av0.js("list", Arrays.asList("1", "2"), "cw", new SerialNum("3.1.0"));
//            String all = nameSpace.resolveAll(env);
//            System.out.println(all);
        });

    }
}
