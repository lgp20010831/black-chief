package com.black.database;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.black.core.annotation.ChiefServlet;
import com.black.core.mvc.response.ResponseUtil;
import com.black.core.query.ClassWrapper;
import com.black.core.query.MethodWrapper;
import com.black.core.sql.annotation.OpenSqlPage;
import com.black.core.sql.annotation.OpenTransactional;
import com.black.core.sql.code.DataSourceBuilder;
import com.black.core.sql.code.SpringDataSourceBuilder;
import com.black.core.tools.BeanUtil;
import com.black.core.util.Assert;
import com.black.core.util.ExcelUtils;
import com.black.core.util.ExcelWritor;
import com.black.core.util.StringUtils;
import com.black.datasource.DataSourceBuilderTypeManager;
import com.black.sql.QueryResultSetParser;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlExecutor;
import com.black.table.ColumnMetadata;
import com.black.table.TableMetadata;
import com.black.utils.ScriptRunner;
import com.black.utils.ServiceUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Api(tags = "visit database unsefa manager")
@ChiefServlet("chief/visit") @OpenTransactional
public class VisitDatabaseController {

    public static Class<? extends DataSourceBuilder> DATA_SOURCE_TYPE = SpringDataSourceBuilder.class;

    public static final String ALIAS = "visit-database";

    private final SqlExecutor executor;

    public VisitDatabaseController(){
        DataSourceBuilder dataSourceBuilder = DataSourceBuilderTypeManager.getBuilder(DATA_SOURCE_TYPE);
        Sql.configDataSource(ALIAS, dataSourceBuilder);
        executor = Sql.opt(ALIAS);
    }

    @PostMapping(value = "runSql", consumes = "text/plain")
    @ApiOperation("执行 sql 语句")
    void runSql(@RequestBody String body){
        body = StringUtils.removeIfStartWith(body, "\"");
        body = StringUtils.removeIfEndWith(body, "\"");
        executor.nativeExec(body);
    }

    @PostMapping("runQuery")
    @ApiOperation("执行查询 sql 语句, 返回结果通过 returnMethod 自定义")
    Object runQuery(@RequestParam(defaultValue = "list") String returnMethod, @RequestBody String sql){
        QueryResultSetParser parser = executor.nativeQuery(sql);
        ClassWrapper<?> wrapper = BeanUtil.getPrimordialClassWrapper(parser);
        MethodWrapper method = wrapper.getSingleMethod(returnMethod);
        Assert.notNull(method, "can not find return method: " + returnMethod);
        return method.invoke(parser);
    }

    @PostMapping("runSqlScript")
    @ApiOperation("执行sql 文件")
    void runSqlScript(@RequestPart MultipartFile file) throws IOException {
        ScriptRunner scriptRunner = new ScriptRunner(executor.getConnection());
        try {
            scriptRunner.runScript(new InputStreamReader(file.getInputStream()));
        }finally {
            executor.closeConnection();
        }
    }

    @GetMapping("getTableMetadata")
    @ApiOperation("获取表元数据")
    Object getTableMetadata(@RequestParam String tableName){
        TableMetadata metadata = executor.getTableMetadata(tableName);
        JSONObject response = new JSONObject();
        response.put("tableName", metadata.getTableName());
        response.put("primaryKeysSize", metadata.primaryKeysSize());
        response.put("remark", metadata.getRemark());
        JSONArray array = new JSONArray();
        for (ColumnMetadata columnMetadata : metadata.getColumnMetadatas()) {
            JSONObject object = new JSONObject();
            object.put("name", columnMetadata.getName());
            object.put("typeName", columnMetadata.getTypeName());
            object.put("size", columnMetadata.getSize());
            object.put("autoIncrement", columnMetadata.autoIncrement());
            object.put("remark", columnMetadata.getRemarks());
            array.add(object);
        }
        response.put("columns", array);
        return response;
    }


    protected String getValueAndAppendItem(JSONObject json, String key, String prefix){
        String val = json.getString(key);
        return val == null ? null : prefix + val;
    }

    @OpenSqlPage
    @PostMapping("queryData")
    @ApiOperation("查询表数据")
    Object queryData(@RequestParam String tableName, @RequestBody JSONObject json){
        String $B = getValueAndAppendItem(json, "$B", "$B:");
        String $A = getValueAndAppendItem(json, "$A", "$A:");
        String $W = getValueAndAppendItem(json, "$W", "$W:");
        String $JSON = getValueAndAppendItem(json, "$JSON", "JSON:");
        String $S = getValueAndAppendItem(json, "$S", "$S:");
        return executor.query(tableName, json, $B, $A, $W, $JSON, $S).list();
    }

    @PostMapping("insertData")
    @ApiOperation("插入表数据")
    void insertData(@RequestParam String tableName, @RequestBody List<Map<String, Object>> list){
        executor.insertBatch(tableName, list);
    }

    @PostMapping("updateData")
    @ApiOperation("更新表数据")
    void updateData(@RequestParam String tableName, @RequestBody JSONObject json){
        executor.saveAndEffect(tableName, json, true);
    }

    @PostMapping("deleteData")
    @ApiOperation("删除表数据")
    void deleteData(@RequestParam String tableName, @RequestBody JSONObject json){
        executor.deleteEffect(tableName, json);
    }

    @PostMapping("uploadExcelData")
    @ApiOperation("上传 excel 导入数据")
    void uploadExcelData(@RequestParam String tableName, @RequestPart MultipartFile file) throws IOException {
        List<Map<String, Object>> dataList = ExcelUtils.readAll(workbook -> workbook.getSheetAt(0), file.getInputStream(), null, 0, null);
        Map<String, String> remarkMap = ServiceUtils.groupMetadataByRemark(executor.getTableMetadata(tableName), executor.getEnvironment().getConvertHandler());
        dataList = ServiceUtils.replaceListMapKey(dataList, remarkMap);
        executor.saveAndEffectBatch(tableName, dataList, true);
    }

    @GetMapping("downloadExcelData")
    @ApiOperation("下载表数据到 excel 文件")
    void downloadExcelData(@RequestParam String tableName, HttpServletResponse response) throws IOException {
        ResponseUtil.configResponse(response, tableName + "表数据.xlsx");
        List<Map<String, Object>> maps = executor.query(tableName).list();
        Map<String, String> title = ServiceUtils.groupMetadataByName(executor.getTableMetadata(tableName), executor.getEnvironment().getConvertHandler());
        ExcelWritor.prepare(tableName)
                .titleMap(title)
                .dataMap(maps)
                .write(response.getOutputStream());
    }

}
