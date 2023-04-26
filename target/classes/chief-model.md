# chief - model 各模块文档
<details>

<summary>
[01] JDBC模块
</summary>

<details>
<summary>
前言
</summary>

````
    宗旨: 摒弃实体类, 只需要 map 就能实现添加数据和查询数据
    在数据库字段特别多的情况下, 写实体类太痛苦了, 就算有代码自动生成也是感觉繁重
    外部传来一组 map 数据, 希望不转化成实体类, 就能够直接操作数据库
    这个组件就是为了实现这个, 只需要一个 mapper 类
    同时自动开启事务, 列名和数据名开放转换, 并可以添加监听器, 尽可能做到灵活
    同时适配多个数据源, 额外数据源的提供需要实现接口自己提供
````

</details>

<details>
<summary>
启用模块
</summary>

```

//只需要在启动类上标注该注解即可(前提是确保chiefApplication的加载)
@EnabledMapSQLApplication
public class Application{

}
```

</details>

<details>

<summary>
创建访问数据库的Mapper
</summary>


######涉及注解
````
@GlobalConfiguration : 该注解其实对应了一个数据源的配置,
                        所以该框架支持多数据源配置
@ImportMapper        : 该注解用于导入其他 mapper, 如果其他 mapper 
                       标注了 @GlobalConfiguration 注解, 则表示跟导入
                       mapper 使用同一配置
                       
                       
             @GlobalConfiguration 注解重要属性:
             String value(): 表示该数据源的唯一标识, 不同GlobalConfiguration的标识不能重复
             Class<? extends DataSourceBuilder> builderClass():
                             该属性提供一个获取数据源的接口, 几个主要的实现类:
                             SpringDataSourceBuilder: 跟 spring 共用同一个数据源
                             YmlDataSourceBuilder: 读取配置文件, spring.datasource ... 属性, 构造一个新的数据源
                             PropertiesHikariDataSource: 读取配置文件, 以唯一标识(value属性值) 为前缀, 读取 url, username ..
                                                         构造一个新的数据源(建议)
             Class<? extends AliasColumnConvertHandler> convertHandlerType(): 用于转换java属性名称 和 数据库字段名称接口, 主要的几个实现类:
                            HumpColumnConvertHandler: 驼峰式转换实现类
                                                           
                            
````


###### 多数据源示例

```

数据源1:
@GlobalConfiguration(value = "demo1", builderClass = PropertiesHikariDataSource.class)
public interface DemoMapper1{

}
application.yml 配置文件:
demo1:
   driver-class:xxxx
   url:xxxxx
   username:xxxx
   password:xxxx 

----------------------------------
数据源2:
@GlobalConfiguration(value = "demo2", builderClass = PropertiesHikariDataSource.class)
public interface DemoMapper2{

}

@ImportMapper(DemoMapper2.class)
public interface ImportDemo2{

}

application.yml 配置文件:
demo2:
   driver-class:xxxx
   url:xxxxx
   username:xxxx
   password:xxxx 

```
</details>


<details>
<summary>
编写 sql 方法 
</summary>

######主要注解:

````
@Configurer 标注在编写的方法上, 被此注解标记的方法,会被自动解析成 sql 语句执行
            属性:
                String applySql() 拼接在最终生成的 sql 后面, 通常用于排序写法
                String tableName() 指定操作的表名, 这个属性是可以替代的, 可以在方法上
                                   标注@TableName 注解, 也可以写方法名的时候按照规范来实现
                                   方法名称被分为 操作类型|表名 例如 querySupplier 标识 查询方法, 表名是 supplier
                                   各个类型的前缀关键字参考 SqlNameUtils
                String[] sqlSequences() 语句表达式, 如果时 查, 改, 删, 那么该属性的值会被作为查询条件
                                        如果 insert 过程中想添加默认值, 例如 del=0
                                        则只需要表达式 del=0
                                        写法: 字段名 = substring(?, 1, 2)
                                        xxx = now()
                                        字段名是列名
                                        该属性存在的字段, 优先级大于外部参数, 往往定义些写死的属性   
                String[] setValues()  只有方法为 update 时才有效, 语法: column = val                                          
                String[] returnColumns() 查询时有效, 指定返回哪些列

 示例: 
 @Configurer(sqlSequences = "is_deleted = false", applySql = 'order by code')
 List<Map<String, Object>> selectSupplier();
 
 上述方法等同生成的 sql 语句: select * from supplier where is_deleted = false order by code 
 
 -----------------------------------------------------
 @BlendMap:
 另一个重要的参数注解 @BlendMap 该注解作用于参数上, 并且参数的类型必须是 map                                                             
                    String value() 表达式, 操作符[列名], 操作符[列名1, 列名2]
 
           他能实现动态生成条件语句, 比如:
           map: {name = lgp}
           生成 sql:  name = 'lgp'  
           map: {name = lgp, age = 10}
           生成 sql:  name = 'lgp' and age = 10
           搭配 blendMap 的表达式:
           map: {name = lgp, age = 10} 表达式: like[name], >[age]
           生成 sql: name like '%lgp%' and age > 10
           对于 map 中那些在表中不存在的字段, 会自动过滤掉, 
           保证 sql 不会出现不符合当前表的列
           
 示例:                            
  @Configurer(sqlSequences = "is_deleted = false", applySql = 'order by code')
  List<Map<String, Object>> selectSupplier(@BlendMap("like[name, code]")Map<String, Object> cond)
 
  上述方法等同生成的 sql 语句: select * from supplier where [name like '%?%' and code like '%?%' ... and] is_deleted = false order by code

---------------------------------------------------                   
 @RunScript:                  
 如果执行完整的 sql 可以使用 @RunScript
 该注解与  @Configurer 底层执行并不一样,所以不能搭配 @BlendMap .... 注解
 
 属性:
   
    String[] value() 用来编写 sql, 这是个数组, 可以执行多条 sql, 但是结果只会取最后一条 sql 的结果
    
    最重要的是语句参数的映射表达式
    #{xx.xxx} 获取值, 并且根据值的类型判断是否需要加 ''
    ${xxxx} 读取映射配置, 与GlobalMappingComponent组件搭配执行, 
            例如在配置文件中配置了:
                    mapping:
                         df: is_deleted = false   
            那么 ${df} 就会替换成 is_deleted = false
    ^{xx}  获取值,但不会拼接 ''
    ?[xxx1 ? xxx2 : xxx3] 三目运算, xxx1 是 boolean表达式, 解析 xxx1 主要使用了谷歌 aviator 框架
                        xxx2 是为true时保留的文本(必需), xxx3  是为true时保留的文本(非必需) 
                        示例:
                        ?[isNotNull(map.name) ? and name = #{map.name} : and name is null]
                        ?[isNotNull(map.name) ? and name = #{map.name}]
                        以上两种均成立( ? / : 两边必须有空格)
    @RunScript 示例:
    @RunScript("select * from supplier where name like '%^{name}%' and code = #{map.code} and ${df} 
     ?[isNotNull(map.phone) ? and phone = #{map.phone}]")
    List<Map<String, Object>> getSupplierList(String name, Map<String, Object> map);
    
    上述解析后的 sql 可能是:  select * from supplier where name like '%lgp%' and code = '0001' and is_deleted = false  and phone = '123'                              
````
###### 因为注解太多了, 目前实现的注解得有几十个, 所以只介绍常用的注解
###### 全部注解在包 com.black.core.sql.annotation 下

</details>

<details>
<summary>
自动化Mapper GlobalParentMapping
</summary>

###### 实现这个框架就是为了减少操作数据库的成本, 所以实现了一个通用访问不同表的 mapper, 只需要切换表名就可以访问不同的表

``````
 GlobalParentMapping
 需要继承该 mapper 就可以使用其所有方法
 示例:   
 
    @GlobalConfiguration(value = "demo1", builderClass = PropertiesHikariDataSource.class)
    public interface DemoMapper1 extends GlobalParentMapping{
    
    }

常用的增删改查方法:
查: globalSelect(String name(表名), Map<String, Object> map(条件参数), String blendString(blend表达式))
增: globalInsertSingle(String name(表名), Map<String, Object> map(数据)) 返回值为添加的主键
改: globalUpdate(String name(表名), Map<String, Object> setMap(set参数Map), Map<String, Object> condition(条件参数), String blend(blend表达式))
删: globalDelete(String name(表名), Map<String, Object> map(条件参数), String blendString(blend表达式))
``````

</details>


<details>
<summary>
自动化控制器 DynamicController
</summary>

######因为 GlobalParentMapping 的存在可以灵活的访问数据库, 那么依据 spring mvc 的特性便可编写一个通用的控制器来针对某一个表实现增删改查的接口
###### DynamicController 内提供了 14 个接口方法, 只需用户定义自己的控制器去继承他, 然后重写一些方法就完成了, 示例:

````
DynamicController 代码部分粘贴:
    
    @Sort(1)
    @TestedNozzle(8)
    @GetMapping("selectById")
    @ApiJdbcProperty(request = "url: ?id=xxxx", response = "$<getTableName>{}", remark = "根据 id 查询接口")
    public Object selectById(@RequestParam(DEFAULT_PRIMARY_KEY) Object id){
        return doSelectById(id);
    }

    @Sort(2)
    @OpenSqlPage
    @TestedNozzle(7)
    @PostMapping("list")
    @ApiJdbcProperty(response = "$<getTableName>[]", request = "$<getTableName>{}", remark = "列表查询接口")
    public Object select(@RequestBody(required = false) JSONObject body){
        return doSelectList(body);
    }

    /** 子类可以重写查询逻辑 */
    protected Object doSelectList(Map<String, Object> body){
        return getMapper().globalSelect(getTableName(), body);
    }

    @Sort(3)
    @TestedNozzle(9)
    @PostMapping("findSingle")
    @ApiJdbcProperty(response = "$<getTableName>{}", request = "$<getTableName>{}", remark = "查询一条数据")
    public Object findSingle(@RequestBody JSONObject body){
        return doSelectSingle(body);
    }

    protected Object doSelectSingle(Map<String, Object> body){
        List<Map<String, Object>> list = (List<Map<String, Object>>) doSelectList(body);
        if (list.size() > 1){
            throw new IllegalStateException("结果不唯一");
        }
        return list.isEmpty() ? null : list.get(0);
    }

    @Sort(4)
    @TestedNozzle(2)
    @PostMapping("insert")
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "普通插入接口")
    public Object insert(@RequestBody JSONObject body){
        return doInsertBatch(Collections.singletonList(body));
    }

    ........

    用户控制器如果继承:
    public class SupplierController extends DynamicController{
    
        //一定要用 LazyAutoWried, 而不是 AutoWired, 因为 sqlApplication 启动的时机比较晚
        @LazyAutoWried
        Demo1Mapper demo1Mapper
    
        //重写返回要操作的表名
        protected String getTableName(){
            return "supplier";
        }
    
        //重写返回要使用的 mapper
        protected GlobalParentMapping getMapper(){
            return demo1Mapper;
        }
    }
    
    后来又经过加工,又出现几个子类更简化了 DynamicController 的代码, 
    所以现在最简便的写法是: 
    //类名要规范: 表名 +  [ Controller / Servlet/ Action ...]
    public class SupplierController extends AbstractCRUDBaseController<Demo1Mapper>{
            
            //内部不需要一行代码, 这个控制器就有对 supplier 14个接口方法
    }
    
    
````
</details>
</details>
<details>
<summary>
[02] chief-servlet 模块
</summary>
    <details>
        <summary>
            功能介绍
        </summary>

````
利用 spring 的 aop 机制, 代理了所有标注了 @ChiefSerlvet 注解的控制器
并增强标注了 @RequestMapping 注解的 web 方法
提供的默认增强: 
    1. 日志打印: 当请求顺利通过过滤器和拦截器后, 会打印请求的信息, 比如请求地址,
                请求方式, 请求参数等
    2. 响应封装: 对于返回值类型为 Object 的web方法实现响应封装, 或者当方法抛出异常后
                会捕获然后封装成响应类, 具体的响应类型可以在注解属性上进行标注, 默认的响应类是
                Response.java, 自定义的响应类需要实现接口 RestResponse.java
    3. 自动分页: 对于标注了注解 @OpenIbatisPage 的方法实现mybatis的自动分页 
                会自动读取参数列表中 pageNum 和 pageSize 属性, 若都不为空, 则
                开启分页, 并且在响应里填充 totel 属性
    4. 自动取值: 当请求参数为 json 的时候, 我们可能通常需要从 json 里取出个别的值
                然后进行空值判断或者类型转换, 很麻烦, 所以提供了注解 @RequiredVal 标注
                在参数上, 之后会自动根据参数名称读取 json 里的值,并进行空值判断, 若为空
                则会抛出异常
    5. 响应封装进阶版: 由于后期引入了 ResponseVoidWritor.java 类, 所以可以对任意返回值类型
                 的 web 方法进行封装了, 比如 void, int, Pojo
    6. 获取请求参数:
                提供了几个注解: @RequestAttribute 获取请求属性
                               @Header 获取请求头属性
    7. 异常封装: 通常对于异常抛出会被框架捕获, 然后封装到响应类的 message 为异常的 message
                当异常的 msg 过长会导致响应过大, 所以提供注解 @AvoidThrowable 标注在类上
                当捕获到指定类型的异常后, 替换异常的 message 为注解里的 runtimeMessage 值
                另有注解 @PostAvoidThrowable: 
                                            作用在方法上, 作用域为当前类
                                            当成功规避异常后, 执行标注的方法
                                            方法的参数, 会通过 instanceFactory 注入
                                            方法如果存在返回值, 则会注入到响应类的 result 里
                                            同一个类里标注此注解的方法只能有一个                                                 
                
    8. 响应值转换: 
                @AdditionalResponseBody 会后置处理返回的响应结果, 比如通过查询数据库的值为
                带有下划线, 若自动转换为驼峰式的话需要指定注解里的 ResponseBodyHandler 为 
                HumpBodyHandler.class                   
    9. 自定义:  提供接口 GlobalAroundResolver.java 和注解 @GlobalAround 来自定义处理器处理
                请求方法的前后织入            
````

</details>

<details>
<summary>冲突</summary>

```
spring 对代理的类方法执行前是走了一条代理链, 并且控制事务的拦截器也在链中
与 chief 提供的拦截器属于同一级别, 但是由于 sort 的设置, chief 的链总是
先执行, 并且因为 chief 封装的方法的异常都被捕获, 所以异常不会流入到事务链中
这样就会导致事务失效, 解决方法只有将需要事务控制的代码写到 impl 里,并加上事务
控制    
```

</details>

</details>

<details>
    <summary>[03] 线程池模块</summary>

<details>
<summary>功能介绍</summary>

```
该功能十分简单, 只是在项目中维护一个全局的线程池和定时线程池
通过 AsynGlobalExecutor.execute(@NonNull Runnable runnable) 调用
维护的线程池属性都在全局配置 AsynConfiguration 如若修改属性:
    AsynConfiguration config = AsynConfigurationManager.getConfiguration();
    config.setxxx(xxx) ....
```

</details>

</details>

<details>
<summary>[04] http 调用模块</summary>

<details>
<summary>建造者模式</summary>

```
    HttpBuilder:
        发起 get请求: String res = HttpBuilder.get(url).executeAndGetBody();
        发起 post请求: String res = HttpBuilder.post(url).body(map).executeAndGetBody();
        发起 part(上传文件) 请求: String res = HttpBuilder.part(url).addBytePart("file", new byte[0]).executeAndGetBody();
        
```

</details>

<details>
<summary>http mapper 模式</summary>

```

//http 请求前缀地址
@OpenHttp("#{http_prefix}")
public interface HttpMapper {

    //发起 post 请求
    @PostUrl("#{post_url}")
    String doPost(@JsonBody JSONObject body, @UrlMap Map<String, Object> urlParam, @HttpHeaders Map<String, String> headers);

    //发起 get 请求
    @GetUrl("#{get_url}")
    String doGet(@UrlMap Map<String, Object> urlParams, @HttpHeaders Map<String, String> headers);

    //发起 part 请求
    @PartUrl("#{part_url}")
    String doPart(@UrlMap Map<String, Object> urlParams, @HttpPart JSONObject json,
                  @HttpHeaders Map<String, String> headers, @HttpPart Object... files);
}

#{} 读取的是配置文件里的值

```

</details>

</details>



<details>
<summary>[05] minio 模块</summary>

<details>
<summary>功能介绍</summary>

```
想要启动该模块, 需要在启动类上添加注解 @EnabledMinios
该注解会解析配置文件, 根据特定的前缀来生成操作 minio 的类

前缀:

minios:
    alias1:
        url: minio 访问地址
        accessKey: key
        secretKey: 密钥
        bucket: 甬道
    alias2:
        ....
然后通过类 Minios.def() //默认是获取 alias = default 的minio handler
或者通过方法 Minios.get(alias) 获取到操作 minio 的类 BucketHandler            


核心上传方法: 
    MinioBody upload(@NonNull MultipartFile mf);

    MinioBody upload(@NonNull File file);

    MinioBody upload(byte[] buf, String name, String contentType);

    MinioBody upload(InputStream in, String name, String contentType);

返回值 MinioBody 里聚合了上传文件的名称和访问url地址

```

</details>


</details>