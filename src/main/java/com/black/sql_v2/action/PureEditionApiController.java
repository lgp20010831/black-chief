package com.black.sql_v2.action;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.api.ApiRemark;
import com.black.api.GetApiProperty;
import com.black.api.PostApiProperty;
import com.black.core.sql.annotation.OpenSqlPage;
import com.black.core.sql.annotation.OpenTransactional;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;

@Getter @ApiRemark("pure")
@Log4j2 @OpenTransactional
public class PureEditionApiController extends AbstractSqlOptServlet{

    public String tableName = "img", alias = "chiefSql";


    //****************************************************************
    //          A   U   T   O           C   R   E   A   T   E
    //****************************************************************
    @OpenSqlPage
    @PostApiProperty(url = "list", request = "img{}",
            response = "img[]", remark = "列表查询", hide = true)
    Object list(@RequestBody JSONObject json){
        return list0(json);
    }

    @GetApiProperty(url = "queryById", response = "img{}", remark = "根据id查询", hide = true)
    Object queryById(@RequestParam Serializable id){
        return queryById0(id);
    }

    @PostApiProperty(url = "single", request = "img{}",
            response = "img{}", remark = "查询一条", hide = true)
    Object single(@RequestBody JSONObject json){
        return single0(json);
    }

    @PostApiProperty(url = "save", request = "img{}", remark = "更新/添加", hide = true)
    void save(@RequestBody JSONObject json){
        save0(json);
    }

    @PostApiProperty(url = "saveBatch", request = "img[]", remark = "批次-更新/添加", hide = true)
    void saveBatch(@RequestBody JSONArray array){
        saveBatch0(array);
    }

    @PostApiProperty(url = "delete", request = "img{}", remark = "删除", hide = true)
    void delete(@RequestBody JSONObject json){
        delete0(json);
    }

    @GetApiProperty(url = "deleteById", remark = "根据id删除", hide = true)
    void deleteById(@RequestParam Serializable id){
        deleteById0(id);
    }

    @PostApiProperty(url = "deleteByIds", request = "$R: [id]", remark = "根据id数组删除", hide = true)
    void deleteByIds(@RequestBody List<Object> ids){
        deleteByIds0(ids);
    }

}
