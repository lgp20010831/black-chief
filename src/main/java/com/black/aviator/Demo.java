package com.black.aviator;

import com.black.aviator.annotation.BooleanExpress;
import com.black.aviator.annotation.ObjectExpress;
import com.black.core.util.Av0;
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

    public static void main(String[] args) throws IllegalAccessException, NoSuchMethodException {

//        Po po = new Po();
//        po.setName("lgp");
//        String e = "A: p.name";
//        Map<String, Object> map = Av0.of("p", po);
//        Object item = SyntaxResolverManager.resolverItem(e, map, null);
//        //System.out.println(AviatorManager.getInstance().execute(e, ));
//        System.out.println(item);
        AviatorManager.getInstance().addInstanceFunctions("po", Po.class);
        System.out.println(AviatorManager.execute("let square = lambda(x) -> x*2 end; for x in range(0, 10) { p(square(x)); }", Av0.js("poi", new Po())));
    }

    @Getter @Setter
    public static class Po{
        private String name;

    protected String make(String name){
        this.name = name;
        return name;
    }
    }
}
