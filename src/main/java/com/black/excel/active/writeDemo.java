package com.black.excel.active;

import com.alibaba.fastjson.JSONObject;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.DefaultDataSourceBuilder;
import com.black.role.UserLocal;
import com.black.sql_v2.Environment;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlType;

import javax.print.ServiceUI;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author 李桂鹏
 * @create 2023-07-11 11:13
 */
@SuppressWarnings("all")
public class writeDemo {

    static void prepare(){
        Sql.optEnvironment().setDataSourceBuilder(new DefaultDataSourceBuilder(
                "root", "", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/invest?serverTimezone=UTC&stringtype=unspecified"
        ));
        Environment environment = Sql.optEnvironment();
        environment.registerKeyAndValue(SqlType.INSERT, "create_user", (Supplier)() -> UserLocal.getName());
        environment.registerKeyAndValue(SqlType.INSERT, "create_time", (Supplier)() -> com.black.utils.ServiceUtils.now());
        environment.registerKeyAndValue(SqlType.INSERT_SET, "update_user", (Supplier)() -> UserLocal.getName());
        environment.registerKeyAndValue(SqlType.INSERT_SET, "update_time", (Supplier)() -> com.black.utils.ServiceUtils.now());
    }

    public static void main(String[] args) {
        prepare();
        ActiveSheet sheet = ActiveSheet.create("新增项目模板.xlsx", workbook -> workbook.getSheetAt(0));
        sheet.setTrimTitle(true);
        sheet.setTitleIndex(1);
        Map<String, String> map = com.black.utils.ServiceUtils.groupMetadataByRemark(Sql.getTableMetadata("project"), new HumpColumnConvertHandler());
        map.put("任务分解", "taskName");
        map.put("完成进度", "progress");
        sheet.setTitleEscape(map);
        List<JSONObject> list = Sql.nativeQuery("select * from project where project_type = 0").jsonList();
        Map<String, List<Map<String, Object>>> group = Sql.nativeQuery("select * from invest_speed").listGroup("${projectId}");
        for (JSONObject project : list) {
            String id = project.getString("id");
            List<Map<String, Object>> speeds = group.get(id);
            project.put("投资进度", speeds);
        }
        sheet.writeEmbeddedData(list, 3);
        sheet.writeFile("D:\\idea_pros\\black-chief\\src\\main\\resources\\生成.xlsx");
    }
}
