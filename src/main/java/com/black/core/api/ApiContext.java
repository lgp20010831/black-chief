package com.black.core.api;

public interface ApiContext {


    /**
     * 我要不通过注解注释控制器, 来实现接口文档的生成
     * 必须需要一个配置类, 配置控制器包所在的位置: scannerPackages()
     * 配置实体类包所在的位置: pojoScannerPackages()
     * 收集成 controller class map: Map<String, Class<?>> controllerMap;
     *                          key = 控制器别名, value = 控制器 class 对象
     *       pojo class map: Map<String, Class<?>> pojoMap
     *                          key = 实体类别名, value = 实体类对象
     *      接下来就是对应实体类和控制器之间的关系:
     *       一个控制器可能依赖多个实体类, 所以需要一个调度接口:
     *       和一个记录这个依赖关系的类
     *          public void handlerDependency(Class<?> controllerClass, Stirng alias,
     *                                        Map<String, Class<?>> pojoMap, ? dependencyRegister){
     *               //这样 userController  --- 依赖于 ---> user 实体类
     *              dependencyRegister.register(pojoMap.get(alias.substring(alias.indexOf("controller"))))
     *          }
     *
     *     该收集每个控制器有效的方法
     *     ApiMethodCollector 目的将所有 api 方法进行受集,
     *     然后可以通过 config 接口提供方法过滤器:
     *     ApiMethodFilter:
     *     public boolean filterApiMethod(Class<?> controllerClass, Method apiMethod){
     *         return true; true = 放行, false = 过滤掉, 此方法无效
     *     }
     *
     *     config 收集过滤器形成一个 collection<ApiMethodFilter>, 然后遍历执行
     *     继续向下走
     *
     *      ApiMethodCollector  收集所有有效的方法
     *      Map<Class<?>, List<Method>> apiMethods
     *
     *      ApiResponseCollector
     *      需要执行一个接口方法:
     *      public Class<?> registerResponseClass(Class<?> controllerClass, String alias){
     *          return Response.class;
     *      }
     *
     *     到此阶段: 存贮别名的类:
     *              Map<String, Class<?>> controllerMap;
     *              Map<String, Class<?>> pojoMap
     *              并抛出可以根据别名查询 class 的方法, 注册别名的方法
     *              依赖映射的类:
     *              Map<Class<?>, List<Class<?>>> dependencyMap
     *              key = conrtoller class 对象, value = pojo class 对象
     *              有效方法存贮类:
     *              Map<Class<?>, List<Method>> apiMethods
     *              响应类存贮类:
     *              Map<Class<?>, Class<?>> controllerResponses
     *              key = 控制器 class, value = 响应类 class
     *                          ------------------------------------------------
     *      开始解析阶段
     *      api 需要的东西
     *      控制器注释 ....
     *          接口列表(对 api 方法的封装):
     *                 注释
     *                 url
     *                 http 方法
     *                 请求头:{
     *                          content-type ..
     *                          token ...
     *                        }
     *                 参数别表(解释
     *                        type (字段类型)
     *                        name (字段名称)
     *                       remark (字段说明)
     *                       required (是否必须)
     *
     *                 ),
     *                 参数示例
     *                 响应列表
     *                 响应示例
     *        定义封装类(由下到上)
     *        ApiParameterDetails(api参数详情)
     *        String type
     *        String name
     *        String remark
     *        boolean required;
     *
     *        ApiRestInterface:
     *        String remark
     *        List<Sting> urls
     *        List<String> httpMethod
     *        Map<String, String> requestHeaders
     *        List<ApiParameterDetails> requestListDetails
     *        String requestExample
     *        String responseExample
     *
     *        ApiController:
     *        String remark 注释
     *        List<ApiRestInterface> 接口
     *
     *
     *       处理阶段每处理一个数据, 都应该有相应的处理器
     *
     *        开始一定是处理api 方法
     *
     *        遍历Map<Class<?>, List<Method>> apiMethods
     *             --->最终返回 List<ApiController> 对象， 最终资源
     *
     *       首先要注册一些处理器, 来执行每个阶段
     *       注册的处理器存贮到 context 中, 由context调用
     *
     *
     *       处理参数类：
     *          获取所有控制器 class  对应的实体类 class
     *          提供一个接口:
     *              RequestParamHandler:
     *                  public Annoataion filterUselessParamsAnnoation(Class 实体类 class 对象){
     *                      //注册一个注解, 标注了该注解的字段不会当做请求参数
     *                      //为了配合序列化成 json 的 ignore 注解
     *                  }
     *                  public boolean filterRemainingParam(Class<?> fieldType, Field, field, String fieldName){
     *                      //过滤剩下的实体类字段
     *                      teturn true 放行
     *                  }
     *
     *                  public ApiParameterDetails postWrapper(Class controllerClass, Method method, ApiParameterDetails initDetails){
     *
     *                      //对这个实体类进行属性填充, 最终返回一个完整的封装对象
     *                  }
     *
     *          在遍历      Map<Class<?>, List<Method>> apiMethods 中
     *          通过 RequestParamHandlers 的处理能够收集到
     *          List<ApiParameterDetails> requestListDetails
     *
     *
     *
     *          接下来通过接口
     *          public String handlerRequestParamExample(List<ApiParameterDetails> requestListDetails, String example, ExampleStearmAdapter adapter){
     *              //返回请求示例
     *          }
     *          为了能更好的增加请求参数: 特加适配器
     *          ExampleStearmAdapter：
     *          主要功能自由的增加参数, 删除参数
     *          Collection<String> getExampleMap()  //获取当前示例中都有哪些 key
     *          ExampleStearmAdapter addParam(String param) 增加参数
     *          ExampleStearmAdapter removeParam(String param) 移除参数
     *          String rewrite();   //重写, 根据目前的参数重新书写示例
     *
     *          得到String requestExample
     *
     *          remark, url, httpMethod 可以聚合到一个接口, 三个方法
     *          ApiInterfaceBaseHandler
     *
     *
     *          然后就是headers
     *          public Map<String, String> obtainRequestHeaders(Class controllerClass, Method method, String alias){
     *              //获取 http method
     *          }
     *          得到 Map<String, String> requestHeaders
     *
     *          处理响应要比处理请求简单些
     *          public String handlerApiResponseExample(Class responseClass, Class pojoClass, Method method,
     *                                                  Class controllerClass, String example, ExampleStearmAdapter adapter){
     *
     *          }
     *          得到String responseExample
     *
     *          然后数据全了以后, 封装成 ApiRestInterface
     *          最后调用接口
     *          public String getControllerRemark(){
     *              //获取控制器注释 ... 所有数据齐全
     *          }
     *
     *
     *         context 应该维护
     *          1. 贮别名的类
     *          2. 依赖映射的类:
     *          3. 存贮响应类的类、
     *          4. 有效方法存贮类:
     *          5. 主要配置类
     *          6. 请求参数处理类 Collection<RequestParamHandler>  RequestParamHandlers;
     *          7. 接口基本信息处理类: Collection<ApiInterfaceBaseHandler> ApiInterfaceBaseHandlers;
     *          8. 请求示例处理类 Collection<RequestExampleReader> RequestExampleReaders;
     *          9. 获取请求头处理器 Collection<RequestHeadersHandler> RequestHeadersHandlers;
     *          10. 响应实例处理器 Collection<ResponseExampleReader>  ResponseExampleReaders;
     *
     *          处理请求参数时, 需要内置缓存
     *
     *         最终加入到
     *         Collection<RequestParamHandler>  RequestParamHandlers;
     *
     *
     *
     *
     *
     */
    /** 没有指定则默认生成存在于配置文件中的 class */
    void writeApiDocs(String writeAbsolutelyPosition);

    /** 指定生成哪几个控制器的文档 */
    void writeApiDocs(String writeAbsolutelyPosition, Class<?>... pointClasses);

    /** 指定控制器所在的包 */
    void writeApiDocs(String writeAbsolutelyPosition, String controllerPackage);
}
