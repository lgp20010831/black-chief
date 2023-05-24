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

    static final String sql = "select * from user";

    static void dxc(){
        //10
        //2
        //10
        AsynConfigurationManager.getConfiguration().setCorePoolSize(4);
        Sql.opt();
        ThreadUtils.allAliasTransaction().latch(() -> {
            log.info("执行业务");
            Utils.sleep(2000);
            throw new RuntimeException();
        }, 10);
        System.out.println("执行完成----------------------");
    }

    static void sql_v1(){
        DbBuffer dbBuffer = DbBufferManager.alloc(new SpringDBConnection());
        List<Object> list = dbBuffer.queryList(sql, null);
    }

    @GlobalConfiguration
    interface Mapper{

        @RunScript(sql)
        List<Map<String, Object>> select();
    }

    static Mapper mapper;
    static void sql_v2(){
        List<Map<String, Object>> list = mapper.select();
    }

    static void sql_v3(){
        //key = name--age val = sutend map
        Map<String, Map<String, Object>> map = NativeV2Sql.queryBySpring(sql).singleGroup("${name}--${age}");
        //key = name val = age
        Map<String, String> map2 = NativeV2Sql.queryBySpring(sql).custom("${map.name.size()}-", "${age}");
    }

    static void sql_v4(){
        List<Map<String, Object>> list = Sql.nativeQuery(sql).list();
    }

    static void sql_v5(){
        List<Map<String, Object>> list = XmlSql.selectByArray("countSupplier", "lgp").list();
        List<Map<String, Object>> list1= XmlSql.select("countSupplier", Av0.js("name", "lgp", "list", Arrays.asList(1,2,3),
                "map", Av0.js("phone", 123))).list();

    }


    public static void main(String[] args) {
//        Sql.configDataSource(new MybatisPlusDynamicDataSourceBuilder());
//        XmlSql.opt().scanAndParse("iu", "xml-sql/");
        dxc();
    }

    @XmlMapper
    interface UserMapper{

        List<Map<String,Object>> countSupplier(Map<String,Object> map);
    }
}
