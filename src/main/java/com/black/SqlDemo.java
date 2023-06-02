package com.black;

import com.black.core.asyn.AsynConfigurationManager;
import com.black.core.sql.annotation.GlobalConfiguration;
import com.black.core.sql.annotation.RunScript;
import com.black.core.sql.code.YmlDataSourceBuilder;
import com.black.core.util.Av0;
import com.black.core.util.Utils;
import com.black.datasource.MybatisPlusDynamicDataSourceBuilder;
import com.black.db.DbBuffer;
import com.black.db.DbBufferManager;
import com.black.db.SpringDBConnection;
import com.black.sql.NativeSql;
import com.black.sql.NativeV2Sql;
import com.black.sql_v2.Sql;
import com.black.thread.ThreadUtils;
import com.black.utils.ServiceUtils;
import com.black.xml.XmlMapper;
import com.black.xml.XmlSql;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author 李桂鹏
 * @create 2023-05-08 17:51
 */
@SuppressWarnings("all") @Log4j2
public class SqlDemo {

    static String str = "@RequestMapping(method=POST, value=[list])\n" +
            "@ApiOperation(value=列表查询)\n" +
            "@OpenSqlPage()\n" +
            "Object list(@RequestBody(required=true) JSONObject $1)\n" +
            " {\n" +
            "Object result = XmlSql.opt(\"master_v2\").select(\"list\", new Object[]{$1}).list();return result;\n" +
            "}";

    public static void main(String[] args) {
        System.out.println(overallIndent(str, 2));
    }

    public static String overallIndent(String str, int size){
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            indentBuilder.append(' ');
        }
        String indent = indentBuilder.toString();
        StringBuilder builder = new StringBuilder(indent);
        for (char c : str.toCharArray()) {
            builder.append(c);
            if (c == '\n'){
                builder.append(indent);
            }

        }
        return builder.toString();
    }
}
