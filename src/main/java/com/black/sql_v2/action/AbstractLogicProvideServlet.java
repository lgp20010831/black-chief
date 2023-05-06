package com.black.sql_v2.action;

import com.black.api.GetApiProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 李桂鹏
 * @create 2023-05-06 9:32
 */
@SuppressWarnings("all")
public abstract class AbstractLogicProvideServlet extends AbstractSqlOptServlet{

    protected Map<String, Object> getSetLogicParam(){
        return new HashMap<>();
    }

    @ApiOperation(value = "根据id逻辑删除", hidden = false)
    @GetApiProperty(url = "logicById", remark = "根据id逻辑删除", hide = false)
    public void logicById(@RequestParam Serializable id){
        opt().updateById(getTableName(), getSetLogicParam(), id);
    }

}
