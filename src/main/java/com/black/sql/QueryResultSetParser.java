package com.black.sql;

import com.alibaba.fastjson.JSONObject;
import com.black.core.chain.GroupUtils;
import com.black.core.sql.SQLSException;
import com.black.core.sql.code.AliasColumnConvertHandler;
import com.black.core.sql.code.util.SQLUtils;
import com.black.core.util.StreamUtils;
import com.black.io.in.ObjectInputStream;
import com.black.utils.TypeUtils;
import org.checkerframework.checker.units.qual.K;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.black.utils.ServiceUtils.patternGetValue;

@SuppressWarnings("all")
public class QueryResultSetParser {

    protected final ResultSet resultSet;

    protected AliasColumnConvertHandler convertHandler;

    protected Finish finish;

    public QueryResultSetParser(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public QueryResultSetParser setConvertHandler(AliasColumnConvertHandler convertHandler) {
        this.convertHandler = convertHandler;
        return this;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setFinish(Finish finish) {
        this.finish = finish;
    }

    public void end(){
        if (finish != null)
            finish.finish();
    }

    public int intVal(){
        return intVal(0);
    }

    public int intVal(int defVal){
        Integer intValue = intValue();
        return intValue == null ? defVal : intValue;
    }

    public Integer intValue(){
        return SQLUtils.getSingle(intList());

    }

    public List<Integer> intList(){
        return StreamUtils.mapList(values(), e -> Integer.valueOf(String.valueOf(e)));
    }

    public String stringValue(){
        return SQLUtils.getSingle(stringList());
    }

    public List<String> stringList(){
        return StreamUtils.mapList(values(), TypeUtils::castToString);
    }

    public double doubleVal(){
        return doubleVal(0);
    }

    public double doubleVal(double defVal){
        Double doubleValue = doubleValue();
        return doubleValue == null ? defVal : doubleValue;
    }

    public Double doubleValue(){
        return SQLUtils.getSingle(doubleList());

    }

    public List<Object> values(){
        ArrayList<Object> list = new ArrayList<>();
        List<Map<String, Object>> resultList = list();
        for (Map<String, Object> map : resultList) {
            Collection<Object> values = map.values();
            ArrayList<Object> arrayList = new ArrayList<>(values);
            if (arrayList.isEmpty()) {
                list.add(null);
            }else {
                list.add(arrayList.get(0));
            }
        }
        return list;
    }

    public List<Double> doubleList(){
        return StreamUtils.mapList(values(), e -> Double.valueOf(String.valueOf(e)));
    }

    public boolean booleanVal(){
        return booleanVal(false);
    }

    public boolean booleanVal(boolean defVal){
        Boolean value = booleanValue();
        return value == null ? defVal : value;
    }

    public Boolean booleanValue(){
        return SQLUtils.getSingle(booleanList());
    }

    public List<Boolean> booleanList(){
        return StreamUtils.mapList(values(), e -> Boolean.valueOf(String.valueOf(e)));
    }

    public <T> ObjectInputStream<T> getObjectInputStream(){
        Map<String, Object> map = map();
        if (map == null) return null;
        Collection<Object> values = map.values();
        return new ObjectInputStream<T>((Collection<T>) values);
    }

    public <T> Collection<T> valueList(){
        return (Collection<T>) map().values();
    }

    public boolean isClosed() throws SQLException {
        return resultSet.isClosed();
    }

    public <T> T javaSingle(Class<T> type){
        return SQLUtils.getSingle(javaList(type));
    }

    public <T> List<T> javaList(Class<T> type){
        List<JSONObject> list = jsonList();
        return StreamUtils.mapList(list, json -> JSONObject.toJavaObject(json, type));
    }

    public Map<String, String> custom(String keyPattern, String valPattern){
        List<Map<String, Object>> list = list();
        Map<String, String> map = new LinkedHashMap<>();
        for (Map<String, Object> ele : list) {
            String key = patternGetValue(ele, keyPattern);
            String val = patternGetValue(ele, valPattern);
            map.put(key, val);
        }
        return map;
    }

    public Map<String, List<String>> customList(String keyPattern, String valPattern){
        List<Map<String, Object>> list = list();
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Map<String, Object> ele : list) {
            String key = patternGetValue(ele, keyPattern);
            String val = patternGetValue(ele, valPattern);
            List<String> stringList = map.computeIfAbsent(key, k -> new ArrayList<>());
            stringList.add(val);
        }
        return map;
    }

    public Map<String, Map<String, Object>> singleGroup(String expression){
        return GroupUtils.singleGroupArray(list(), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }

    public Map<String, List<Map<String, Object>>> listGroup(String expression){
        return GroupUtils.groupArray(list(), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }

    public <T> Map<String, T> singleJavaGroup(String expression, Class<T> type){
        return GroupUtils.singleGroupArray(javaList(type), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }

    public <T> Map<String, List<T>> listJavaGroup(String expression, Class<T> type){
        return GroupUtils.groupArray(javaList(type), map -> {
            return String.valueOf(patternGetValue(map, expression));
        });
    }



    public List<Map<String, Object>> list(){
        try {
            return SQLUtils.parseJavaResult(resultSet, convertHandler);
        } catch (SQLException e) {
            throw new SQLSException(e);
        }finally {
            end();
        }
    }

    public Map<String, Object> map(){
        return SQLUtils.getSingle(list());
    }

    public List<JSONObject> jsonList(){
        try {
            return SQLUtils.parseJavaJsonResult(resultSet, convertHandler);
        } catch (SQLException e) {
            throw new SQLSException(e);
        } finally {
            end();
        }
    }

    public JSONObject json(){
        return SQLUtils.getSingle(jsonList());
    }



}
