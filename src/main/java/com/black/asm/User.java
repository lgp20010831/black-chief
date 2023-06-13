package com.black.asm;

import com.black.core.json.Alias;
import lombok.Data;

import java.util.List;

/**
 * @author 李桂鹏
 * @create 2023-06-12 14:35
 */
@SuppressWarnings("all") @Data
public class User {

    int age;

    String name;

    public void say(){
        System.out.println("say");
    }

    public List<String> get(User user){
        return null;
    }
}
