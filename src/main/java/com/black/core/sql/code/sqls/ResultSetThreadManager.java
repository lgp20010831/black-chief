package com.black.core.sql.code.sqls;




import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ResultSetThreadManager {

    private static final ThreadLocal<Map<ResultType, Object>> resultLoacl = new ThreadLocal<>();

    private static LinkedBlockingQueue<ResultTypeHandler> resultTypeHandlers = new LinkedBlockingQueue<>();

    public static void close(){
        Map<ResultType, Object> map = init();
        map.clear();;
    }


    public static void prepare(ResultType resultType, ResultSet resultSet){
        Map<ResultType, Object> init = init();

        for (ResultTypeHandler resultTypeHandler : resultTypeHandlers) {
            if (resultTypeHandler.support(resultType)) {
                init.put(resultType, resultTypeHandler.resolver(resultSet));
                break;
            }
        }
    }

    public static Object getResult(ResultType resultType){
        return init().get(resultType);
    }

    public static Object getResultAndParse(ResultType resultType, ResultSet resultSet){
        Map<ResultType, Object> init = init();
        if (init.containsKey(resultType)){
            return init.get(resultType);
        }

        for (ResultTypeHandler resultTypeHandler : resultTypeHandlers) {
            if (resultTypeHandler.support(resultType)) {
                init.put(resultType, resultTypeHandler.resolver(resultSet));
                break;
            }
        }
        return init.get(resultType);
    }

    public static void add(ResultTypeHandler resultTypeHandler) {
        if (resultTypeHandler != null){
            resultTypeHandlers.add(resultTypeHandler);
        }
    }


    private static Map<ResultType, Object> init(){
        Map<ResultType, Object> map = resultLoacl.get();
        if (map == null){
            map = new ConcurrentHashMap<>();
            resultLoacl.set(map);
        }
        return map;
    }
}
