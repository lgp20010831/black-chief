package com.black.sql_v2.with;

import lombok.Data;

/**
 * @author shkstart
 * @create 2023-04-14 9:49
 */
@Data
public class WithAs {


    private final String name;

    private String alias;

    private final Object[] params;

    public static WithAs of(String name, String alias, Object... params){
        return new WithAs(name, alias, params);
    }

    public WithAs(String name, String alias, Object[] params) {
        this.name = name;
        this.alias = alias;
        this.params = params;
    }



}
