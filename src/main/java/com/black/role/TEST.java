package com.black.role;

import org.springframework.util.AntPathMatcher;

import java.io.File;

public class TEST {


    public static void main(String[] args) {
        AntPathMatcher matcher = new AntPathMatcher(File.separator);
        System.out.println(matcher.match("/api/*", "/swagger.io/api"));

    }
}
