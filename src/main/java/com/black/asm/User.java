package com.black.asm;

import com.black.core.json.Trust;
import com.black.core.spring.ChiefApplicationConfigurer;
import lombok.Data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
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

    public void may(String name, int age){}

    public static void main(String[] args) throws NoSuchMethodException {
        Method method = User.class.getMethod("get", User.class);
    }

    public @Test List<String> get(User user){
        @Test
        String key;

        return null;
    }


    @Trust
    public void yet(ChiefApplicationConfigurer chiefApplicationConfigurer){}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PACKAGE, ElementType.TYPE_USE, ElementType.LOCAL_VARIABLE})
    @interface Test{

    }
}
