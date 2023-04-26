package com.black.sql_v2.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractProvideServlet extends AbstractSqlOptServlet{

    @PostMapping("list")
    public Object list(@RequestBody JSONObject json){
        return list0(json);
    }

    @GetMapping("queryById")
    public Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostMapping("single")
    public Object single(@RequestBody JSONObject json){
        return single0(json);
    }

    @PostMapping("save")
    public void save(@RequestBody JSONObject json){
        save0(json);
    }

    @PostMapping("saveBatch")
    public void saveBatch(@RequestBody JSONArray array){
        saveBatch0(array);
    }

    @PostMapping("delete")
    public void delete(@RequestBody JSONObject json){
        delete0(json);
    }

    @GetMapping("deleteById")
    public void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }

    @PostMapping("deleteByIds")
    public void deleteByIds(@RequestBody List<Object> ids){
        deleteByIds0(ids);
    }
}
