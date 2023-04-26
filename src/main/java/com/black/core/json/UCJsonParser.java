package com.black.core.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.api.ApiV2Utils;
import com.black.core.spring.util.ApplicationUtil;
import com.black.core.util.Body;
import lombok.Setter;
import org.springframework.util.StringUtils;

//将字符串转成 json 对象
//逻辑写完就忘
@SuppressWarnings("all") @Setter
public class UCJsonParser {

    private char startFlag;

    private char endFlag;

    private char matching;

    private char groupSegmentation;

    private char LIST_START_FLAG;

    private char LIST_END_FLAG;

    private char LIST_Segmentation;

    private boolean trim = true;


    public UCJsonParser(){
        this('{', '}', ':', ',', '[', ']', ',');
    }

    public UCJsonParser(char startFlag, char endFlag, char matching, char groupSegmentation, char lsf, char lef, char ls) {
        this.startFlag = startFlag;
        this.endFlag = endFlag;
        this.matching = matching;
        this.groupSegmentation = groupSegmentation;
        this.LIST_START_FLAG =lsf;
        LIST_END_FLAG = lef;
        LIST_Segmentation = ls;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    public Body parseBody(String text){
        return new Body(parseJson(text));
    }


    public JSONObject parseJson(String text){
        Object json;
        try{
             json = parse(text);
        }catch (RuntimeException e){
            throw new JsonParseException(e);
        }
        if (json instanceof JSONObject){
            return (JSONObject) json;
        }else {
            throw new JsonParseException("无法解析成 json 对象, 文本: " + text);
        }
    }

    public Object parse(String context){
        if (!StringUtils.hasText(context)){
            return context;
        }
        int start = context.indexOf(startFlag);
        if (start == -1){
            return context;
        }
        JSONObject result = new JSONObject();
        int end = context.lastIndexOf(endFlag);
        if (end == -1){
            throw new JsonParseException("lost end flag: " + context);
        }
        String textBody = getString(context.substring(start + 1, end));
        int i;
        while ( (i = textBody.indexOf(matching)) != -1){
            String key = getString(textBody.substring(0, i));
            Object value = getString(textBody.substring(i + 1));
            int fn = 0;
            int ln = 0;
            boolean array = false;
            boolean arrayPrior = false;
            String valStr = value.toString();
            char[] charArray = valStr.toCharArray();
            int last = -1;
            for (int r = 0; r < charArray.length; r++) {
                char c = charArray[r];
                if (c == LIST_START_FLAG) {ln++; if (fn == 0) arrayPrior = true;}
                if (c == LIST_END_FLAG) {ln--; if (ln == 0 && (fn == 0 || arrayPrior)){last = r + 1; array = true; break;}}
                if (c == startFlag) {fn++;}
                if (c == endFlag)   {fn--; if (fn == 0 && (ln == 0 || !arrayPrior)){last = r + 1; break;}}
                if (c == groupSegmentation) {if (fn == 0 && ln == 0) {last = r; break;}}
            }

            last = last == -1 ? valStr.length() : last;

            if (ApiV2Utils.staticColumnValueMap.containsKey(key)){
                value = ApiV2Utils.staticColumnValueMap.get(key).get();
            }else
                value = array ? parseArray(valStr.substring(0, last)) : parse(valStr.substring(0, last));
            result.put(key, value);
            int nb = i + 1 + last + 1;
            if (nb > textBody.length() - 1){
                break;
            }
            textBody = textBody.substring(nb);
        }

        return result;
    }

    public JSONArray parseArray(String text){
        if (!text.startsWith(String.valueOf(LIST_START_FLAG)) || !text.endsWith(String.valueOf(LIST_END_FLAG))){
            throw new IllegalStateException("error array");
        }
        String textBody = getString(text.substring(text.indexOf(LIST_START_FLAG) + 1, text.lastIndexOf(LIST_END_FLAG)));
        JSONArray array = new JSONArray();
        String[] eles = textBody.split(String.valueOf(LIST_Segmentation));
        String sub = null;
        for (String ele : eles) {
            if (sub == null){
                if (ele.startsWith(String.valueOf(startFlag))){
                    int i = find(ele, array);
                    if (i != 0){
                        sub = ele;
                    }
                }

                else if (ele.startsWith(String.valueOf(LIST_START_FLAG))){
                    int i = findArray(ele, array);
                    if (i != 0){
                        sub = ele;
                    }
                }
                else
                    array.add(ele);
            }else {
                sub = sub + LIST_Segmentation + ele;
                if (sub.startsWith(String.valueOf(startFlag))){
                    int i = find(sub, array);
                    if (i == 0){
                        sub = null;
                    }
                }

                else if (sub.startsWith(String.valueOf(LIST_START_FLAG))){
                    int i = findArray(sub, array);
                    if (i == 0){
                        sub = null;
                    }
                }
            }

        }
        return array;
    }

    private int find(String ele, JSONArray array){
        int layer = 0;
        for (char ic : ele.toCharArray()) {
            if (ic == startFlag){
                layer++;
            }
            if (ic == endFlag){
                layer--;
            }
        }
        if (layer == 0){
            array.add(parse(ele));
        }
        return layer;
    }

    private int findArray(String ele, JSONArray array){
        int layer = 0;
        for (char ic : ele.toCharArray()) {
            if (ic == LIST_START_FLAG){
                layer++;
            }
            if (ic == LIST_END_FLAG){
                layer--;
            }
        }
        if (layer == 0){
            array.add(parseArray(ele));
        }
        return layer;
    }

    protected String getString(String txt){
        if (txt != null){
            if (trim){
                return StringUtils.trimAllWhitespace(txt);
            }
        }
        return txt;
    }

    //dome
    public static void main(String[] args) {
        UCJsonParser parser = new UCJsonParser();
        ApplicationUtil.programRunMills(() ->{
//            System.out.println(parser.parse("{at = [s,e,{ab=po, lo = io}]}"));
//            System.out.println(parser.parse("{at = {ab=po, lo = io, ui = [s, i]}}"));
            JSONObject json = parser.parseJson("{a:b,c:s,d:{ers : po, 1 : 89, oi : {ere : ss, pp : {wewew : qq, 45 : 78}, pods : [sui, sdw]}, pods: 234}, sqwq: 456}");
            String string = json.toString();
            JSONObject object = JSONObject.parseObject(string);
            System.out.println(object);
            System.out.println(object);
        });

        ApplicationUtil.programRunMills(() ->{
//            System.out.println(parser.parse("{at = [s,e,{ab=po, lo = io}]}"));
//            System.out.println(parser.parse("{at = {ab=po, lo = io, ui = [s, i]}}"));
            System.out.println(parser.parseJson("{a:b,c:s,d:{ers : po, 1 : 89, oi : {ere : ss, pp = {wewew : qq, 45: 78}, pods : [sui, sdw]}, pods: 234}, sqwq: 456}"));
        });
        System.out.println(parser.parseArray("[{aycAsd:xx,values:[byc]}]"));
    }

}
