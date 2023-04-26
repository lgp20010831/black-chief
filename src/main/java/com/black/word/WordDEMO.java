package com.black.word;

import com.deepoove.poi.data.MiniTableRenderData;
import com.deepoove.poi.data.PictureRenderData;
import com.black.core.util.Av0;
import com.black.utils.CountWare;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.black.utils.ServiceUtils.findValue;

public class WordDEMO {


    public static void main(String[] args) throws IOException {
        String txt = getTxt();
        Map<String, Object> param = getParam();
        MiniTableRenderData data = TableDataBuilder.builder(Collections.singletonList(param), map -> {
            return Arrays.asList(findValue(map, "user.name"), findValue(map, "user.age"), findValue(map, "user.phone"));
        }).headers("姓名", "年龄", "电话").headerBgColor("0080FF").create();
        param.put("table", data);
        PictureRenderData image = WordUtils.writeImage(300, 300, ".jpg", WordUtils.getResource("test.jpg"));
        param.put("img", image);
        FileOutputStream out = new FileOutputStream("E:\\ideaSets\\SpringAutoThymeleaf\\src\\main\\resources\\result.docx");
        WordUtils.complie(param, "ceshi.docx", out);
        out.flush();
        out.close();
    }

    static String getTxt(){
        String txt = "${序号:  count.addLast(user.age)} 姓名: #{user.name}, 年龄: #{A: toType(user.age, 'int') > 20 ? '寿比南山' : '20多岁'} 岁, " +
                "电话: #{user.phone}, 属性: #{J: [BATCH,1]}";
        return txt;
    }


    static Map<String, Object> getParam(){
        CountWare ware = new CountWare("3.1.0");
        return Av0.js("user", Av0.js("name", "李桂鹏", "age", "21", "phone", "123456"), "count", ware);
    }
}
