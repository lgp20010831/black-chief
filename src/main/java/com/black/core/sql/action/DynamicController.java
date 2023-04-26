package com.black.core.sql.action;

import com.alibaba.fastjson.JSONObject;
import com.black.api.ApiJdbcProperty;
import com.black.core.annotation.Sort;
import com.black.core.aop.servlet.AnalyzedMethod;
import com.black.core.aop.servlet.RequiredVal;
import com.black.core.sql.annotation.OpenSqlPage;
import com.black.core.sql.annotation.OpenTransactional;
import com.black.core.sql.code.mapping.GlobalParentMapping;
import com.black.core.util.Assert;
import com.black.core.util.ParentController;
import com.black.swagger.ChiefMapSqlAdaptive;
import com.black.test.TestedNozzle;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.black.utils.ServiceUtils.getString;
import static com.black.utils.ServiceUtils.ofMap;

/** 动态控制器, 提供 14 个数据库基本操作, 子类需要重写某些方法来启用 **/
@OpenTransactional
public abstract class DynamicController extends ParentController implements Dynamic{

    protected String primaryName;

    public static final String DEFAULT_PRIMARY_KEY = "id";

    //返回要使用的 mapper
    public GlobalParentMapping getMapper(){
        throw new UnsupportedOperationException("子类需要重写提供查询 mapper");
    }

    //返回要操作的表名
    protected String getTableName(){
        throw new UnsupportedOperationException("子类需要重写提供操作的表名");
    }

    /** 获取主键名称 */
    protected String getPrimaryKey(){
        if (primaryName == null){
            String pn = getMapper().findIdOfTable(getTableName());
            Assert.notNull(pn, "unknow primary key in table: " + getTableName());
            primaryName = pn;
        }
        return primaryName;
    }

    /** 获取虚拟删除的 set map, 比如: del=1, is_deleted = true */
    protected Map<String, Object> getIllSetMap(){
        throw new UnsupportedOperationException("子类需要重写提供虚拟删除 map 数据");
    }


    @Sort(1)
    @TestedNozzle(8)
    @GetMapping("selectById")
    @ApiJdbcProperty(request = "url: ?id=xxxx", response = "$<getTableName>{}", remark = "根据 id 查询接口")
    public Object selectById(@RequestParam(DEFAULT_PRIMARY_KEY) Object id){
        return doSelectById(id);
    }

    protected Object doSelectById(Object id){
        return getMapper().findById(getTableName(), getPrimaryKey(), id);
    }

    @Sort(2)
    @OpenSqlPage
    @TestedNozzle(7)
    @PostMapping("list")
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
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
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
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
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "普通插入接口")
    public Object insert(@RequestBody JSONObject body){
        return doInsertBatch(Collections.singletonList(body));
    }

    @Sort(5)
    @TestedNozzle(1)
    @PostMapping("insertBatch")
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
    @ApiJdbcProperty(request = "$<getTableName>[]", remark = "批次插入接口")
    public Object insertBatch(@RequestBody List<Map<String, Object>> array){
        return doInsertBatch(array);
    }

    protected Object doInsertBatch(List<Map<String, Object>> array){
        return getMapper().fastJoin(getTableName(), array);
    }

    @Sort(6)
    @TestedNozzle(5)
    @AnalyzedMethod
    @PostMapping("update")
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "更新接口, 主键必需")
    public Object update(@RequestBody @NullRepair JSONObject body, @RequiredVal(DEFAULT_PRIMARY_KEY) String id){
        return doUpdate(body, id);
    }

    protected Object doUpdate(Map<String, Object> body, String id){
        return getMapper().globalUpdate(getTableName(), body, ofMap(getPrimaryKey(), id));
    }


    @Sort(7)
    @TestedNozzle(3)
    @PostMapping("save")
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "修改或者添加, 有主键则修改, 否则添加")
    public Object save(@RequestBody @NullRepair JSONObject body){
        return doSave(body);
    }

    protected Object doSave(Map<String, Object> body){
        String id = getString(body, getPrimaryKey());
        if (id == null){
            return doInsertBatch(Collections.singletonList(body));
        }else {
            return doUpdate(body, id);
        }
    }

    @Sort(8)
    @TestedNozzle(4)
    @PostMapping("saveBatch")
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
    @ApiJdbcProperty(request = "$<getTableName>[]", remark = "批次: 修改或者添加, 有主键则修改, 否则添加")
    public Object saveBatch(@RequestBody @NullRepair List<Map<String, Object>> batch){
        for (Map<String, Object> map : batch) {
            doSave(map);
        }
        return null;
    }

    @Sort(9)
    @TestedNozzle(14)
    @PostMapping("delete")
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "根据条件 map 删除接口")
    public Object delete(@RequestBody JSONObject body){
        return doDelete(body);
    }

    protected Object doDelete(Map<String, Object> map){
        return getMapper().globalDelete(getTableName(), map);
    }

    @Sort(10)
    @TestedNozzle(13)
    @GetMapping("deleteById")
    @ApiJdbcProperty(request = "url: ?id=xxxx", remark = "根据主键删除接口")
    public Object deleteById(@RequestParam(DEFAULT_PRIMARY_KEY) Object id){
        return doDelete(id);
    }

    protected Object doDelete(Object id){
        return getMapper().globalDelete(getTableName(), ofMap(getPrimaryKey(), id));
    }

    @Sort(11)
    @TestedNozzle(15)
    @PostMapping("deleteAllById")
    @ApiJdbcProperty(request = "$R: [id]", remark = "根据主键集合删除数据")
    public Object deleteAllById(@RequestBody List<Object> idList){
        return doDeleteAllById(idList);
    }

    protected Object doDeleteAllById(List<Object> idList){
        return getMapper().deleteAllById(getTableName(), getPrimaryKey(), idList);
    }

    @Sort(12)
    @TestedNozzle(11)
    @GetMapping("illDelById")
    @ApiJdbcProperty(request = "url: ?id=xxxx", remark = "根据主键逻辑删除数据", hide = true)
    public Object illDelById(@RequestParam(DEFAULT_PRIMARY_KEY) String id){
        return doUpdate(getIllSetMap(), id);
    }

    @Sort(13)
    @TestedNozzle(12)
    @PostMapping("illDel")
    @ChiefMapSqlAdaptive(mappingTableNameMethodName = "getTableName")
    @ApiJdbcProperty(request = "$<getTableName>{}", remark = "根据条件 map 逻辑删除数据", hide = true)
    public Object illDel(@RequestBody JSONObject body){
        return doIllDel(body);
    }

    protected Object doIllDel(JSONObject body){
        return getMapper().globalUpdate(getTableName(), getIllSetMap(), body);
    }

    @Sort(14)
    @TestedNozzle(17)
    @PostMapping("illDelAllById")
    @ApiJdbcProperty(request = "$R: [id]", remark = "根据主键集合逻辑删除数据", hide = true)
    public Object illDelAllById(@RequestBody List<Object> idList){
        return doIllDelAllById(idList);
    }

    protected Object doIllDelAllById(List<Object> idList){
        String blendString = "in[" + getPrimaryKey() + "]";
        return getMapper().globalUpdate(getTableName(), getIllSetMap(), ofMap(getPrimaryKey(), idList), blendString);
    }
}
