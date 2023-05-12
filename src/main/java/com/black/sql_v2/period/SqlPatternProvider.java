package com.black.sql_v2.period;

import com.alibaba.fastjson.JSONObject;
import com.black.core.chain.GroupKeys;
import com.black.core.chain.GroupUtils;
import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import com.black.core.sql.code.MapArgHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.Av0;
import com.black.core.util.StreamUtils;
import com.black.sql_v2.Sql;
import com.black.utils.ServiceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 李桂鹏
 * @create 2023-05-12 10:14
 */
@SuppressWarnings("all") @Getter @Setter
public class SqlPatternProvider<T, P> {

    private static final IoLog log = LogFactory.getArrayLog();

    public static boolean openSqlCache = true;

    private static final Map<String, SqlAndPack> sqlCache = new ConcurrentHashMap<>();

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

    private ResultMapping<T> resultMapping;

    public SqlPatternProvider<T, P> resultMapping(ResultMapping<T> resultMapping){
        this.resultMapping = resultMapping;
        return this;
    }

    private final LinkedBlockingQueue<Session<T>> sessions = new LinkedBlockingQueue<>();

    private final Collection<T> elements;

    private final String prepareSql;

    private String sqlAlias = Sql.DEFAULT_ALIAS;

    public SqlPatternProvider<T, P> alias(String alias){
        sqlAlias = alias;
        return this;
    }

    //父类
    private SqlPatternProvider<P, ?> parent;

    private Set<String> items = new HashSet<>();

    //每次填充参数的数量, 为了防止 sql 文太长
    private int batch = -1;

    public SqlPatternProvider<T, P> batch(int b){
        batch = b;
        return this;
    }

    private boolean invoke = false;

    //全局变量
    private Map<String, Object> globalEnv = new LinkedHashMap<>();

    public SqlPatternProvider(String prepareSql, T t){
        this(prepareSql, Av0.as(t));
    }

    public SqlPatternProvider(String prepareSql, Collection<T> elements) {
        this.prepareSql = prepareSql;
        this.elements = elements;
    }

    private void invoke(){
        try {
            SqlAndPack sqlAndPack = getAndHandlerSql(prepareSql);
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
                    if (end >= list.size()){
                        end = list.size() - 1;
                        skip = true;
                    }
                    List<T> sub = list.subList(start, end);
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

    protected void executeSql(String sql, Collection<T> elements){
        List<Map<String, Object>> list = Sql.nativeQuery(sql).list();
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
                Object property = ServiceUtils.getProperty(element, item);
                String val = MapArgHandler.getString(property);
                joiner.add(like ? "like " + val : val);
            }
            return joiner.toString();
        });
        return process;
    }

    public static SqlAndPack getAndHandlerSql(String sql){
        if (openSqlCache){
            return sqlCache.computeIfAbsent(sql, SqlPatternProvider::handlerSql);
        }else {
            return handlerSql(sql);
        }
    }

    public static SqlAndPack handlerSql(String prepareSql){
        List<PatternTextUtils.OperatorPack> packs = new ArrayList<>();
        log.debug("[PATTERN] find operator postition...");
        AtomicInteger index = new AtomicInteger();
        String process = PatternTextUtils.parse("#{", "}", prepareSql, (item, si, ei) -> {
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



    public SqlPatternProvider<T, P> next(Business business){
        if (!invoke){
            invoke();
        }
        for (Session<T> session : sessions) {
            try {
                business.resolve(session, this);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
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

        private final List<Map<String, Object>> mappingResults;

        private final T element;

        private JSONObject attribute = new JSONObject();

        public Session(List<Map<String, Object>> mappingResults, T element) {
            this.mappingResults = mappingResults;
            this.element = element;
        }

        @Override
        public JSONObject getFormData() {
            return attribute;
        }

        @Override
        public List<Map<String, Object>> list() {
            return mappingResults;
        }
    }




}
