package com.black.syntax;

import com.black.core.sql.code.mapping.GlobalMapping;
import com.black.core.sql.code.mapping.GlobalMappingComponent;
import com.black.core.util.StringUtils;
import com.black.utils.ServiceUtils;

import java.util.HashMap;
import java.util.Map;

public class SyntaxMethodContextParser {



    public static String parseTxt(String txt, Map<String, Object> source){
        if (source == null){
            source = new HashMap<>();
        }
        int of = txt.indexOf("(");
        int ofend = txt.indexOf(")");
        StringBuilder txtBuilder = new StringBuilder();
        while (of != -1 && ofend != -1 && ofend > of){
            String paramString = txt.substring(of + 1, ofend);
            String prefix = txt.substring(0, of);
            StringBuilder methodNameBuilder = new StringBuilder();
            for (int i = of - 1; i >= 0; i--) {
                char c = prefix.charAt(i);
                if (c == ' ' || i == 0){
                    if (i != 0 && i == of -1){
                        throw new IllegalStateException("no method name: " + prefix);
                    }
                    txtBuilder.append(i == 0 ? "" : prefix.substring(0, i + 1));
                    methodNameBuilder.append(i == 0 ? prefix : prefix.substring(i + 1));
                    break;
                }
            }
            String methodName = methodNameBuilder.toString();
            createSource(source, paramString);
            String methodBody = GlobalMapping.obtain(methodName);
            if (methodBody == null){
                methodBody = "";
            }
            Map<String, Object> finalSource = source;
            String afterParseBody = ServiceUtils.parseTxt(methodBody, "${", "}", arg -> {
                Object val = finalSource.get(arg);
                return val == null ? "null" : val.toString();
            });
            txtBuilder.append(afterParseBody);
            txt = txt.substring(ofend + 1);
            of = txt.indexOf("(");
            ofend = txt.indexOf(")");
        }
        txtBuilder.append(txt);
        return txtBuilder.toString();
    }


    private static void createSource(Map<String, Object> source, String paramString){
        String[] params = paramString.split(",");
        if (params != null){
            for (int u = 1; u <= params.length; u++) {
                String argkey = "arg" + u;
                source.put(argkey, StringUtils.removeFrontSpace(params[u - 1]));
            }
        }
    }

    public static void main(String[] args) {
        GlobalMappingComponent.loads();
        String txt = "1.${m(lgp, 12)}\n2.${m(zs, 25)} 以上学生";
        String m = "表格:  m(lgp, 12), m(lgp2, 14), m(lgp3, 18) hhhhhhh";
        System.out.println(GlobalMapping.dynamicParse(txt, null));
    }
}
