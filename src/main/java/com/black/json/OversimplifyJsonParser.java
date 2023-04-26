package com.black.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.util.StringUtils;
import lombok.Data;

@Data
public class OversimplifyJsonParser implements JsonParser {

    private char jsonStart = '{';

    private char jsonEnd = '}';

    private char jsonConn = ':';

    private char stringValueFlag = '\'';

    private char kvSeparate = ',';

    private char arrayStart = '[';

    private char arrayEnd = ']';

    private char arrayElementSeparate = ',';

    @Override
    public Object parse(String text) {
        if (isArray(text)) {
            return parseArray(text);
        }else if (isJson(text)){
            return parseJson(text);
        }
        return text;
    }
    public boolean isArray(String text){
        text = StringUtils.removeFrontSpace(text);
        text = StringUtils.removeTrailingSpace(text);
        return text.startsWith(String.valueOf(arrayStart)) && text.endsWith(String.valueOf(arrayEnd));

    }

    public boolean isJson(String text){
        text = StringUtils.removeFrontSpace(text);
        text = StringUtils.removeTrailingSpace(text);
        return text.startsWith(String.valueOf(jsonStart)) && text.endsWith(String.valueOf(jsonEnd));
    }

    public void checkArray(String text){
        text = StringUtils.removeFrontSpace(text);
        text = StringUtils.removeTrailingSpace(text);
        if (!text.startsWith(String.valueOf(arrayStart)) || !text.endsWith(String.valueOf(arrayEnd))){
            throw new JsonParseException("无法解析 jsonArray 文本: " + text);
        }
    }

    public void checkJson(String text){
        text = StringUtils.removeFrontSpace(text);
        text = StringUtils.removeTrailingSpace(text);
        if (!text.startsWith(String.valueOf(jsonStart)) || !text.endsWith(String.valueOf(jsonEnd))){
            throw new JsonParseException("无法解析 jsonObject 文本: " + text);
        }
    }

    public JSONArray parseArray(String text){
        checkArray(text);
        char[] charArray = text.toCharArray();
        JSONArray array = new JSONArray();
        TOKEN token = null;
        StringBuilder valueCollector = new StringBuilder();
        StringBuilder jsonContentValue = new StringBuilder();
        StringBuilder arrayContentValue = new StringBuilder();
        int currentSonJson = 1;
        int currentSonArray = 1;
        int index = -1;
        for (char c : charArray) {
            index++;
            if (c == arrayStart){
                if (token == TOKEN.JSON_VALUE){
                    jsonContentValue.append(c);
                }else if (token == TOKEN.CHAR_VALUE){
                    valueCollector.append(c);
                }else if (token == TOKEN.ARRAY_NEXT){
                    token = TOKEN.ARRAY_VALUE;
                    arrayContentValue.append(c);
                }else if (token == TOKEN.ARRAY_VALUE){
                    currentSonArray++;
                    arrayContentValue.append(c);
                }else {
                    token = TOKEN.ARRAY_NEXT;
                }
                continue;
            }

            if (c == jsonStart){

                //如果已经在收集 子 json 数据了
                if (token == TOKEN.JSON_VALUE){
                    currentSonJson ++;
                    jsonContentValue.append(jsonStart);
                    continue;
                }

                //如果收集值的时候发现是个 json
                if (token == TOKEN.ARRAY_NEXT){
                    //说明要去解析子 json
                    token = TOKEN.JSON_VALUE;
                    jsonContentValue.append(jsonStart);
                    continue;
                }

                if (token != TOKEN.CHAR_VALUE){
                    continue;
                }
            }
            if (token == TOKEN.JSON_VALUE && c != jsonEnd){
                jsonContentValue.append(c);
                continue;
            }

            if (token == TOKEN.ARRAY_VALUE && c != arrayEnd){
                arrayContentValue.append(c);
                continue;
            }

            if (token == TOKEN.CHAR_VALUE && c != stringValueFlag){
                valueCollector.append(c);
                continue;
            }



            //只要这个空格不在 '' 包裹的 value 以内直接过滤
            if (c == ' '){
                if (token != TOKEN.CHAR_VALUE){
                    continue;
                }
            }


            if (c == arrayElementSeparate || c == arrayEnd || c == jsonEnd){
                Object val = valueCollector.toString();
                //如果子json content 收集完毕
                if (c == jsonEnd && token == TOKEN.JSON_VALUE){
                    currentSonJson--;
                    jsonContentValue.append(jsonEnd);
                    //如果已经到达最外层 json, 完成收集
                    if (currentSonJson == 0){
                        try {
                            val = parseJson(jsonContentValue.toString());
                        }catch (Throwable e){
                            throw new JsonParseException("解析嵌套 json 文本时发生错误", e, text, index);
                        }
                        clearCollector(jsonContentValue);
                    }else {
                        continue;
                    }
                }else if (c == arrayEnd && token == TOKEN.ARRAY_VALUE){
                    currentSonArray --;
                    arrayContentValue.append(arrayEnd);
                    //如果达到最外层 array 完成收集
                    if (currentSonArray == 0){
                        try {
                            val = parseArray(arrayContentValue.toString());
                        }catch (Throwable e){
                            throw new JsonParseException("解析嵌套 array 文本时发生错误", e, text, index);
                        }
                        clearCollector(arrayContentValue);
                    }else {
                        continue;
                    }
                }
                if (token == TOKEN.ARRAY_NEXT && c == kvSeparate){
                    continue;
                }


                if (token == TOKEN.ARRAY_NEXT && c == arrayEnd){
                    continue;
                }
                array.add(token == TOKEN.CHAR_VALUE ? val : Util.deepAnalysisObject(val));
                clearCollector(valueCollector);
                token = TOKEN.ARRAY_NEXT;
                continue;
            }

            if (c == stringValueFlag){

                if (token == TOKEN.CHAR_VALUE){
                    //完成了对 char value 的收集
                    token = TOKEN.FINISH_COLLECT_VALUE;
                    continue;
                }

                if (token != TOKEN.ARRAY_NEXT){
                    throw new JsonParseException(" '' 出现的位置错误", text, index);
                }
                token = TOKEN.CHAR_VALUE;
                continue;
            }

            switch (token){
                case UNKNOWN_VALUE:
                case CHAR_VALUE:
                    valueCollector.append(c);
                    break;
                case ARRAY_NEXT:
                    valueCollector.append(c);
                    token = TOKEN.UNKNOWN_VALUE;
                    break;
            }
        }

        return array;
    }


    public JSONObject parseJson(String text){
        checkJson(text);
        char[] charArray = text.toCharArray();
        JSONObject jsonObject = new JSONObject(true);
        TOKEN token = TOKEN.PARSE_KEY;
        StringBuilder keyCollector = new StringBuilder();
        StringBuilder valueCollector = new StringBuilder();
        StringBuilder jsonContentValue = new StringBuilder();
        StringBuilder arrayContentValue = new StringBuilder();
        int currentSonJson = 1;
        int currentSonArray = 1;
        int index = -1;
        for (char c : charArray) {
            index++;
            if (c == arrayStart){
                if (token == TOKEN.JSON_VALUE){
                    jsonContentValue.append(c);
                }else if (token == TOKEN.CHAR_VALUE){
                    valueCollector.append(c);
                }else if (token == TOKEN.CONNECT){
                    token = TOKEN.ARRAY_VALUE;
                    arrayContentValue.append(c);
                }else if (token == TOKEN.ARRAY_VALUE){
                    currentSonArray++;
                    arrayContentValue.append(c);
                }else {
                    valueCollector.append(c);
                }
                continue;
            }

            if (c == jsonStart){

                //如果已经在收集 子 json 数据了
                if (token == TOKEN.JSON_VALUE){
                    currentSonJson ++;
                    jsonContentValue.append(jsonStart);
                    continue;
                }

                //如果收集值的时候发现是个 json
                if (token == TOKEN.CONNECT){
                    //说明要去解析子 json
                    token = TOKEN.JSON_VALUE;
                    jsonContentValue.append(jsonStart);
                    continue;
                }

                if (token != TOKEN.CHAR_VALUE){
                    continue;
                }
            }

            if (token == TOKEN.JSON_VALUE && c != jsonEnd){
                jsonContentValue.append(c);
                continue;
            }

            if (token == TOKEN.ARRAY_VALUE && c != arrayEnd){
                arrayContentValue.append(c);
                continue;
            }

            if (token == TOKEN.CHAR_VALUE && c != stringValueFlag){
                valueCollector.append(c);
                continue;
            }

            //只要这个空格不在 '' 包裹的 value 以内直接过滤
            if (c == ' '){
                if (token != TOKEN.CHAR_VALUE){
                    continue;
                }
            }

            if (c == jsonConn){
                token = TOKEN.CONNECT;
                //完成对key的收集， nameCollector 里的值是 key
                continue;
            }

            if (c == kvSeparate || c == jsonEnd || c == arrayEnd){
                String key = keyCollector.toString();
                Object val = valueCollector.toString();
                //如果子json content 收集完毕
                if (c == jsonEnd && token == TOKEN.JSON_VALUE){
                    currentSonJson--;
                    jsonContentValue.append(jsonEnd);
                    //如果已经到达最外层 json, 完成收集
                    if (currentSonJson == 0){
                        try {
                            val = parseJson(jsonContentValue.toString());
                        }catch (Throwable e){
                            throw new JsonParseException("解析嵌套 json 文本时发生错误", e, text, index);
                        }
                        clearCollector(jsonContentValue);
                    }else {
                        continue;
                    }

                }else if (c == arrayEnd && token == TOKEN.ARRAY_VALUE){
                    currentSonArray --;
                    arrayContentValue.append(arrayEnd);
                    //如果达到最外层 array 完成收集
                    if (currentSonArray == 0){
                        try {
                            val = parseArray(arrayContentValue.toString());
                        }catch (Throwable e){
                            throw new JsonParseException("解析嵌套 array 文本时发生错误", e, text, index);
                        }
                        clearCollector(arrayContentValue);
                    }else {
                        continue;
                    }
                }
                if (token == TOKEN.PARSE_KEY && c == kvSeparate){
                    continue;
                }

                if (token == TOKEN.PARSE_KEY && c == jsonEnd){
                    continue;
                }

                jsonObject.put(key, token == TOKEN.CHAR_VALUE ? val : Util.deepAnalysisObject(val));
                clearCollector(keyCollector);
                clearCollector(valueCollector);
                token = TOKEN.PARSE_KEY;
                continue;
            }

            if (c == stringValueFlag){

                if (token == TOKEN.CHAR_VALUE){
                    //完成了对 char value 的收集
                    token = TOKEN.FINISH_COLLECT_VALUE;
                    continue;
                }

                if (token != TOKEN.CONNECT){
                    throw new JsonParseException(" '' 出现的位置错误", text, index);
                }
                token = TOKEN.CHAR_VALUE;
                continue;
            }

            switch (token){
                case PARSE_KEY:
                    keyCollector.append(c);
                    break;
                case UNKNOWN_VALUE:
                case CHAR_VALUE:
                    valueCollector.append(c);
                    break;
                case CONNECT:
                    valueCollector.append(c);
                    token = TOKEN.UNKNOWN_VALUE;
                    break;
            }
        }

        return jsonObject;
    }

    protected void clearCollector(StringBuilder builder){
        builder.delete(0, builder.length());
    }



    static enum TOKEN {
        PARSE_KEY,
        CONNECT,
        FINISH_COLLECT_VALUE,

        ARRAY_NEXT,

        CHAR_VALUE,
        UNKNOWN_VALUE,
        JSON_VALUE,

        ARRAY_VALUE
    }

    public static void main(String[] args) {
        OversimplifyJsonParser jsonParser = new OversimplifyJsonParser();
        System.out.println(jsonParser.parseJson("{age : 1, phone : '19853149113=:}{{}.', " +
                "son: {txt:hello, son2: {txt2:hello2}}, son3: [1,2,3, [4,5,6]]}"));

        System.out.println(jsonParser.parseArray("[1, 'hello', {name : lgp},, [1,2,3]]"));
        System.out.println(jsonParser.parseJson("{a:b,c:s,d:{ers : po, 1 : 89, oi : {ere : ss, pp : {wewew : qq, 45 : 78}, pods : [sui, sdw]}, pods: 234}, sqwq: 456}"));
        System.out.println(jsonParser.parseJson("{a:b,c:s,d:{ers : po, 1 : 89, oi : {ere : ss, pp = {wewew : qq, 45: 78}, pods : [sui, sdw]}, pods: 234}, sqwq: 456}"));

    }
}
