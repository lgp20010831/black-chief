package com.black.aviator;

import com.black.aviator.annotation.BooleanExpress;
import com.black.aviator.annotation.ObjectExpress;
import lombok.Getter;
import lombok.Setter;

public class Demo {


    @ObjectExpress("r + '卧槽'")
    @BooleanExpress(express = "name != 'lgp'", otherwise = "'dsb'")
    Object get(@ObjectExpress("p.name + '1'") String name){
        return "name: " + name;
    }

    Object set(String name){
        if (name.equals("lgp")){
            return "dsb";
        }
        name = name + "1";
        return "name: " + name + "卧槽";
    }

    public static void main(String[] args) {

//        Po po = new Po();
//        po.setName("lgp");
//        String e = "A: p.name";
//        Map<String, Object> map = Av0.of("p", po);
//        Object item = SyntaxResolverManager.resolverItem(e, map, null);
//        //System.out.println(AviatorManager.getInstance().execute(e, ));
//        System.out.println(item);
        System.out.println(AviatorManager.execute("2 > 1 ? (3 > 2 ? '是3' : '是2') : '是1'", null));
    }

    @Getter @Setter
    public static class Po{
        private String name;
    }
}
