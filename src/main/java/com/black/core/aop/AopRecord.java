package com.black.core.aop;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AopRecord {

    String path;

    Class<?> controllerClass;

    Method controllerMethod;

    Object[] args;

    JSONObject argsJson = new JSONObject();

    public Object get(String key){
        return argsJson.get(key);
    }

    public String showArgs(){

        if (args == null || args.length == 0)
            return "No Args";

        List<String> showArgStrs = new ArrayList<>();

        List<Object> afterFilter = Arrays.stream(args).filter(
                arg -> {
                    return !(arg instanceof HttpServletRequest || arg instanceof HttpServletResponse);
                }
        ).peek(
               arg ->{

                   showArgStrs.add(arg == null ? "" : arg.toString());
               }
        ).collect(Collectors.toList());

        return showArgStrs.toString();
    }


    public AopRecord(String path, Class<?> controllerClass, Method controllerMethod, Object[] args) {
        this.path = path;
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
        this.args = args;

        Parameter[] parameters = this.controllerMethod.getParameters();

        for (int i = 0; i < parameters.length; i++)
            argsJson.put(parameters[i].getName(), args[i]);
    }

}
