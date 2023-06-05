package com.black.compile.sj;

import com.black.core.util.StringUtils;

import java.util.*;

import static com.black.compile.sj.CompileUtils.*;

/**
 * @author 李桂鹏
 * @create 2023-06-05 17:11
 */
@SuppressWarnings("all")
public abstract class AbstractSjCompiler implements SjCompiler{

    protected final LinkedList<String> statementStack = new LinkedList<>();

    protected final Set<String> specialChars = new LinkedHashSet<>();

    public void addSpecialChars(String... chars){
        specialChars.addAll(Arrays.asList(chars));
    }

    @Override
    public String complie(String code) throws UnableCompileSjException {
        if (!StringUtils.hasText(code)){
            return "";
        }
        inStack(code);
        System.out.println(statementStack);
        return null;
    }
    
    protected void inStack(String code){
        //解析出行代码
        String[] lineCodes = spilts(code, '\n');
        for (String lineCode : lineCodes) {
            inLineCode(lineCode);
        }
    }

    protected void inLineCode(String lineCode){
        //解析出分号代码块
        String[] semicolonCodes = spilts(lineCode, ';');
        for (String semicolonCode : semicolonCodes) {
            inSemicolonCode(semicolonCode);
        }
    }

    //{
    //String str = "";
    //xx{}
    protected void inSemicolonCode(String semicolonCode){
        StringBuilder builder = new StringBuilder();
        for (char c : semicolonCode.toCharArray()) {
            if (specialChars.contains(c)){
                if (builder.length() > 0){
                    inStack0(builder.toString());
                    clearBuilder(builder);
                }
                inStack0(String.valueOf(c));
            }else {
                builder.append(c);
            }
        }
        if (builder.length() > 0){
            inStack0(builder.toString());
        }
    }

    protected void inStack0(String code){
        statementStack.add(code);
    }

    public static void main(String[] args) throws UnableCompileSjException {
        DefaultSjCompiler compiler = new DefaultSjCompiler();
        String code = "        List list = [{\"lgp\":1}, {\"lgp\":2}, {\"lgp\":3}, {\"lgp\":4}]\n" +
                "        list{\n" +
                "            print(it.get(\"lgp\"))\n" +
                "        }";
        String complie = compiler.complie(code);
        System.out.println(complie);
    }
}
