package com.black.core.sql.code.dome;

import com.black.core.sql.code.MapperRegister;
import com.black.core.sql.code.mapping.GlobalMappingComponent;
import com.black.core.sql.code.pattern.ConversionTableNameListener;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static com.black.utils.ServiceUtils.ofMap;

public class DOME {


    public static void main(String[] args) throws SQLException, IOException {

        GlobalMappingComponent.loads();
        MapperRegister register = MapperRegister.getInstance();
        ParentMapper mapper = register.getMapper(ParentMapper.class);
        //RunMapper runMapper = register.getMapper(RunMapper.class);
        register.registerListener(new ConversionTableNameListener());
        //System.out.println(mapper.select("project", "测", true, "团队"));
        //System.out.println(mapper.getProjectProgress("PMTASK_c5ccf672b32345f580fb8d973e219824"));
        System.out.println(mapper.globalSelect("supplier", ofMap("name", "山"), "like[name]"));
        register.shutdown();

//        for (int i = 0; i < 5000; i++) {
//            System.out.println(ServiceUtils.now("yyyy-MM-dd HH:mm:ss.sss"));
//        }
    }


    public static List<String> limitUtfString(String txt){
        List<String> list = new ArrayList<>();
        int length = txt.length();
        int utflen = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = txt.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
            builder.append(c);
            if (utflen == 65535 || utflen == 65534 || utflen == 65533 || utflen == 65532){
                list.add(builder.toString());
                builder.delete(0, builder.length());
                utflen = 0;
            }
        }
        if(utflen > 0){
            list.add(builder.toString());
        }
        return list;
    }

}
