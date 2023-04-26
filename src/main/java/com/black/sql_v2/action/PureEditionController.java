package com.black.sql_v2.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.sql.annotation.OpenTransactional;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

@Getter @Log4j2 @OpenTransactional
public class PureEditionController extends AbstractSqlOptServlet{

    public String tableName = "img", alias = "chiefSql";


    //****************************************************************
    //          A   U   T   O           C   R   E   A   T   E
    //****************************************************************


    @PostMapping("list")
    Object list(@RequestBody JSONObject json){
        return list0(json);
    }

    @GetMapping("queryById")
    Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostMapping("single")
    Object single(@RequestBody JSONObject json){
        return single0(json);
    }


    @PostMapping("save")
    void save(@RequestBody JSONObject json){
        save0(json);
    }

    @PostMapping("saveBatch")
    void saveBatch(@RequestBody JSONArray array){
        saveBatch0(array);
    }

    @PostMapping("delete")
    void delete(@RequestBody JSONObject json){
        delete0(json);
    }

    @GetMapping("deleteById")
    void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }

    @PostMapping("deleteByIds")
    void deleteByIds(@RequestBody List<Object> ids){
        deleteByIds0(ids);
    }

}
