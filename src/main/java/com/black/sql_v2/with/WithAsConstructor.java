package com.black.sql_v2.with;

import java.util.StringJoiner;

/**
 * @author shkstart
 * @create 2023-04-14 9:43
 */
public class WithAsConstructor {


    private final StringJoiner joiner = new StringJoiner(", ", "with", " ");


    private int i = 0;

    public int addSequence(){
        i++;
        joiner.add("#{table" + i + "} as (#{query" + i + "})");
        return i;
    }

    public static void main(String[] args) {
        String prefix = "with #{table1} as (#{query1}), #{table2} as (query2)";
    }
}
