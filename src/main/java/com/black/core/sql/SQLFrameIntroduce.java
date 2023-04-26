package com.black.core.sql;

import com.black.core.mvc.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class SQLFrameIntroduce {

    public static final String TXT = "该框架主要以注解为主进行开发, 减少对于表增删改查的操作, \n" +
            "区别于 mybatis, jpa 在于对于简单增删改查不再依赖于实体类, 而是通过读取数据库表字段\n" +
            "创建一个虚拟的实体类, 用户数据交换的介质主要是 Map, 另外框架提供更加灵活的查询方式, \n" +
            "自带很多校验的功能, 保证生成的 sql 不会报错, 该框架主要以 mybatis 为榜样, 同样也支持 xml\n" +
            " 格式进行书写 sql, 但是技术并不成熟, 功能标签也不能太多样化. \n" +
            "\n" +
            "mapper 的规范书写格式:\n" +
            "一个项目中要配制一个全局mapper(等于配置了一个数据源):\n" +
            "\n" +
            "@GlobalConfiguration\n" +
            "public interface GlobalDemoMapper{\n" +
            "\n" +
            "}\n" +
            "\n" +
            "在全局 mapper 中配置 sql 片段, 相同数据源下的 mapper 只需要声明属于该全局 mapper 即可\n" +
            "\n" +
            "\n" +
            "@ImportMapper(GlobalDemoMapper.class)\n" +
            "public interface tableMapper{\n" +
            "\n" +
            "  在这里书写对于指定表的 sql 操作\n" +
            "\n" +
            "}\n" +
            "\n" +
            "\n" +
            "由于该框架对于 sql 不再依赖实体类, 所以描述一个表, 只需要知道表名就可以了, \n" +
            "在此灵活性上, 特地提供了一个动态的控制器 DynamicController 使得书写增删改查接口变得更加简单\n" +
            "\n" +
            "书写一个对于 user 表操作的控制器:\n" +
            "\n" +
            "@RequestMapping('该属性保证了接口的单一性')\n" +
            "@RestController\n" +
            "public class UserController extend DynamicController{\n" +
            "\n" +
            "   @LazyAutoWried\n" +
            "   UserMapper mapper;\n" +
            "\n" +
            "   重写获取操作 mapper 的方法, 一般提供全局 mapper\n" +
            "   @Override\n" +
            "   protected GlobalParentMapping getMapper(){\n" +
            "       return getBean(GlobalDemoMapper.class);\n" +
            "   } \n" +
            "\n" +
            "  @Override\n" +
            "  protected String getTableName(){\n" +
            "       return 'user';\n" +
            "   }\n" +
            "}\n" +
            "\n" +
            "\n" +
            "仅仅需要几张代码就能完成对一个表的增删改查, 具体接口支持什么可以去看下源码\n";

    public static InputStream getInputStream(){
        return new ByteArrayInputStream(TXT.getBytes());
    }

    public static void flush(){
        System.out.println(TXT);
    }

    public static void writeFile(String path){
        File file = FileUtil.dropAndcreateFile(path);
        FileUtil.writerFile(file, TXT);
    }

}
