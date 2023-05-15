package com.black.sql_v2.period;

import com.alibaba.fastjson.JSONObject;
import com.black.core.chain.GroupKeys;
import com.black.core.chain.GroupUtils;
import com.black.core.json.JsonUtils;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Av0;
import com.black.core.util.StreamUtils;
import com.black.core.util.Utils;
import com.black.sql_v2.Sql;
import com.black.utils.CollectionUtils;
import com.black.utils.ServiceUtils;
import lombok.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * 该组件是为了解决业务中, 如果出现循环调用 sql 造成的时间花费问题
 * 例如, 业务中通常针对一个 xx 列表, 根据每个 xx 特有的属性进行一次 sql
 * 查询, 这种循环查询数据库十分消耗时间, 该组件提供优化 sql 组装 in 表达式
 * 和使用链式编程, 但存在一些局限性
 * 比如 sql 文中必须要返回查询的参数, 这样组件才能跟结果集进行映射, 否则需要
 * 设置自定义的结果集映射函数
 * @author 李桂鹏
 * @create 2023-05-12 10:14
 */
@SuppressWarnings("all") @Getter @Setter
public class SqlPatternProvider<T, P> {

    private static final IoLog log = LogFactory.getArrayLog();

    public static boolean openSqlCache = true;

    private static final Map<String, SqlAndPack> sqlCache = new ConcurrentHashMap<>();

    public static <T, P> SqlPatternProvider<T, P> provider(String sql, Collection<T> collection, Object... params){
        return new SqlPatternProvider<T, P>(sql, collection, params);
    }

    /*

        希望的编程格式:

        void test(List<User> users){

            Sql.provide(sql, users).next((provider, user, env, session) -> {

                Map<String, Object> map = provider.find(user).map();
                String personId = ... 业务处理....
                session.put("personId", personId);
            }).provide(sql2).next((provider, user, env, session) -> {

                //........
            })
            .finish((provider, user, env, session) -> {
                Result r = new Result();
                //set r in user or session
                Sql.insert(r);
            })
        }

        //难点: 结果集和参数列表的匹配关系
     */

    /**
     * 结果集和每个元素的映射关系
     */
    private ResultMapping<T> resultMapping;

    //设置结果集和元素映射关系
    public SqlPatternProvider<T, P> resultMapping(ResultMapping<T> resultMapping){
        this.resultMapping = resultMapping;
        return this;
    }

    //存放每个元素和对应结果集的会话
    private final LinkedBlockingQueue<Session<T>> sessions = new LinkedBlockingQueue<>();

    //存贮元素
    private final Collection<T> elements;

    //预处理 sql
    private final String prepareSql;

    //流动参数列表
    private final Object[] params;

    //调用 sql 应用程序别名
    //该组件需要 Sql 组件的支持
    private String sqlAlias = Sql.DEFAULT_ALIAS;

    //设置别名
    public SqlPatternProvider<T, P> alias(String alias){
        sqlAlias = alias;
        return this;
    }

    //父类
    private SqlPatternProvider<P, ?> parent;

    //存放 sql 中需要替换的 item 列表
    private Set<String> items = new HashSet<>();

    //每次填充参数的数量, 为了防止 sql 文太长
    private int batch = -1;

    public SqlPatternProvider<T, P> batch(int b){
        batch = b;
        return this;
    }

    //是否已经处理完 sql 了
    private boolean invoke = false;

    //全局变量
    private Map<String, Object> globalEnv = new LinkedHashMap<>();

    //针对元素的属性列表
    private final List<String> propertyKeys;

    //验证每个需要填充的 item 是属于 全局变量中还是元素集合中的
    //如果是全局变量, 则不会检索 pack
    private Predicate<String> predicate;

    public SqlPatternProvider(String prepareSql, T t, Object... params){
        this(prepareSql, Av0.as(t), params);
    }

    public SqlPatternProvider(String prepareSql, @NonNull Collection<T> elements, Object... params) {
        this.prepareSql = prepareSql;
        this.elements = elements;
        if (elements.isEmpty()){
            throw new IllegalStateException("空集合元素不建议使用该组件");
        }
        T element = CollectionUtils.firstElement(elements);
        propertyKeys = PatternTextUtils.getPropertyKeys(element);
        predicate = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return propertyKeys.contains(s);
            }
        };
        this.params = params;
    }

    public SqlPatternProvider<T, P> putEnv(String k, Object v){
        globalEnv.put(k, v);
        return this;
    }

    private List<Map<String, Object>> createNewParams(){
        return StreamUtils.mapList(sessions, session -> {
            Map<String, Object> map = session.map();
            T element = session.getElement();
            Map<String, Object> hashMap = new LinkedHashMap<>(map);
            hashMap.putAll(JsonUtils.letJson(element));
            return hashMap;
        });
    }

    //items = name, age. phone
    public SqlPatternProvider<Map<String, Object>, T> piece(String newSql, String items, Object... params){
        String[] splits = PatternTextUtils.splits(items, ",");
        List<Map<String, Object>> newParams = createNewParams();
        List<Map<String, Object>> maps = StreamUtils.mapList(newParams, newParam -> {
            return ServiceUtils.filterNewMapV2(newParam, Arrays.asList(splits));
        });
        return createSon(newSql, maps, params);
    }

    public SqlPatternProvider<Map<String, Object>, T> provider(String newSql, Object... params){
        return createSon(newSql, createNewParams(), params);
    }

    protected SqlPatternProvider<Map<String, Object>, T> createSon(String newSql, List<Map<String, Object>> maps, Object... params){
        SqlPatternProvider<Map<String, Object>, T> provider = SqlPatternProvider.provider(newSql, maps, params);
        provider.setParent(this);
        Map<String, Object> globalEnv = provider.getGlobalEnv();
        globalEnv.putAll(getGlobalEnv());
        return provider;
    }

    private void invoke(){
        try {
            SqlAndPack sqlAndPack = getAndHandlerSql(prepareSql, predicate);
            int batch = getBatch();
            Collection<T> elements = getElements();

            if (batch <= 0){
                String sql = joinValues(elements, sqlAndPack);
                executeSql(sql, elements);
            }else {
                int size = elements.size();
                List<T> list = (List<T>) SQLUtils.wrapList(elements);
                int start = 0;
                int end = batch;
                boolean skip = false;
                for (;;){
                    if (end > list.size()){
                        end = list.size();
                    }

                    if (end == list.size()){
                        skip = true;
                    }

                    List<T> sub = list.subList(start, end);
                    start = end;
                    end = end + batch;
                    String sql = joinValues(sub, sqlAndPack);
                    executeSql(sql, sub);
                    if (skip)
                        break;
                }
            }
        }finally {
            invoke = true;
        }
    }

    public SqlPatternProvider<T, P> filterNull(){
        sessions.removeIf(session -> Utils.isEmpty(session.getMappingResults()));
        return this;
    }

    protected void executeSql(String sql, Collection<T> elements){
        List<Map<String, Object>> list = Sql.nativeQuery(sql, params).list();
        if (resultMapping == null){
            Map<GroupKeys, List<Map<String, Object>>> group = GroupUtils.groupArray(list, map -> {
                List<Object> objects = StreamUtils.mapList(items, map::get);
                return new GroupKeys(objects.toArray());
            });
            for (T ele : elements) {
                List<Object> objects = StreamUtils.mapList(items, item -> ServiceUtils.getProperty(ele, item));
                GroupKeys groupKeys = new GroupKeys(objects.toArray());
                List<Map<String, Object>> maps = group.get(groupKeys);
                Session<T> session = new Session<>(maps, ele);
                sessions.add(session);
            }
        }else {
            for (T element : elements) {
                List<Map<String, Object>> maps = resultMapping.mapping(list, element);
                Session<T> session = new Session<>(maps, element);
                sessions.add(session);
            }
        }
    }

    protected String joinValues(Collection<T> elements, SqlAndPack sqlAndPack){
        String process = sqlAndPack.getSql();
        List<PatternTextUtils.OperatorPack> packs = sqlAndPack.getPacks();
        log.debug("[PATTERN] replace values...");
        AtomicInteger index = new AtomicInteger();
        process = PatternTextUtils.parse("#{", "}", process, (item, si, ei) -> {
            items.add(item);
            PatternTextUtils.OperatorPack pack = packs.get(index.getAndIncrement());
            boolean like = "like".equalsIgnoreCase(pack.getOperator());
            StringJoiner joiner = new StringJoiner(",", "(", ")");
            for (T element : elements) {
                Object property;
                if (PatternTextUtils.containProperty(element, item)) {
                    property = ServiceUtils.getProperty(element, item);
                }else {
                    property = ServiceUtils.getProperty(globalEnv, item);
                }
                String val = MapArgHandler.getString(property);
                joiner.add(like ? "like " + val : val);
            }
            return joiner.toString();
        });
        return process;
    }

    public static SqlAndPack getAndHandlerSql(String sql, Predicate<String> predicate){
        if (openSqlCache){
            return sqlCache.computeIfAbsent(sql, s -> handlerSql(s, predicate));
        }else {
            return handlerSql(sql, predicate);
        }
    }

    public static SqlAndPack handlerSql(String prepareSql, Predicate<String> predicate){
        List<PatternTextUtils.OperatorPack> packs = new ArrayList<>();
        log.debug("[PATTERN] find operator postition...");
        AtomicInteger index = new AtomicInteger();
        String process = PatternTextUtils.parse("#{", "}", prepareSql, (item, si, ei) -> {
            if (predicate != null){
                if (!predicate.test(item)) {
                    return "#{" + item + "}";
                }
            }
            PatternTextUtils.OperatorPack operatorPack = PatternTextUtils.checkOperator(prepareSql, si);
            operatorPack.setIndex(index.getAndIncrement());
            packs.add(operatorPack);
            return "#{" + item + "}";
        });

        log.debug("[PATTERN] packaging operator...");
        String specialPrefix = "@{";
        String specialSuffix = "}";
        int offset = 0;
        for (PatternTextUtils.OperatorPack pack : packs) {
            int start = pack.getStart();
            start = start + offset;
            int end = pack.getEnd() + 1;
            end = end + offset;
            process = PatternTextUtils.addFlag(process, start, end, specialPrefix, specialSuffix);
            offset+= 3;
        }


        log.debug("[PATTERN] replace operator of 'in'...");
        String replaceItem = "in";
        process = PatternTextUtils.parse("@{", "}", process, (item, si, ei) -> {
            return "in";
        });
        return new SqlAndPack(process, packs);
    }

    public SqlPatternProvider<T, P> next(){
        return next(null);
    }

    public SqlPatternProvider<T, P> next(Business<T, P> business){
        if (!invoke){
            invoke();
        }
        if (business != null){
            for (Session<T> session : sessions) {
                try {
                    business.resolve(session, this);
                } catch (Throwable e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return this;
    }

    @Data @AllArgsConstructor
    public static class SqlAndPack{

        private final String sql;

        private final List<PatternTextUtils.OperatorPack> packs;
    }

    @Data
    public static class Session<T> implements AttributeHandler, AttributeProcessor,
    ListResourceHandler{

        private final List<JSONObject> mappingResults;

        private final T element;

        public Session(List<Map<String, Object>> mappingResults, T element) {
            this.mappingResults = StreamUtils.mapList(mappingResults, JSONObject::new);
            this.element = element;
        }

        @Override
        public JSONObject getFormData() {
            if (mappingResults.size() <= 1){
                return mappingResults.get(0);
            }
            throw new IllegalStateException("当前结果集存在多个元素");
        }

        @Override
        public List<JSONObject> jsonList() {
            return mappingResults;
        }

        @Override
        public List<Map<String, Object>> list() {
            Object list = this.mappingResults;
            return (List<Map<String, Object>>) list;
        }

        public Session<T> modifyAlias(String origin, String after){
            for (Map<String, Object> map : mappingResults) {
                if (map.containsKey(origin)){
                    map.put(after, map.remove(origin));
                }
            }
            return this;
        }
    }
}
