package com.black.excel.active;

import com.alibaba.fastjson.JSONObject;
import com.black.core.query.ClassWrapper;
import com.black.core.sql.HumpColumnConvertHandler;
import com.black.core.sql.code.DefaultDataSourceBuilder;
import com.black.core.util.Av0;
import com.black.role.UserLocal;
import com.black.sql_v2.Environment;
import com.black.sql_v2.Sql;
import com.black.sql_v2.SqlType;
import com.black.table.TableMetadata;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.black.utils.ServiceUtils.*;
/**
 * @author 李桂鹏
 * @create 2023-07-06 16:31
 */
@SuppressWarnings("all")
public class Demo {

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

    static void readStrust(){
        ClassWrapper<ActiveSheet> classWrapper = ClassWrapper.get(ActiveSheet.class);
        System.out.println(classWrapper);
    }

    public static void main(String[] args) {
//        prepare();
//        readSheet2();
        readStrust();
    }


    static void readSheet2(){
        ActiveSheet sheet = ActiveSheet.create(com.black.utils.ServiceUtils.getResource("ds.xlsx"),
                workbook -> workbook.getSheetAt(2));
        sheet.setTitleIndex(1);
        sheet.setTrimTitle(true);
        sheet.setTitleMergeLenght(2);
        sheet.setDistinctKey("序号");
        TableMetadata metadata = Sql.getTableMetadata("project");
        sheet.setTitleEscape(com.black.utils.ServiceUtils.groupMetadataByRemark(metadata, new HumpColumnConvertHandler()));
        Map<String, CellValueWrapper> titleInfo = sheet.getTitleInfo();
//        List<Map<String, Object>> datas = sheet.getDatas(4, true);
//        System.out.println(datas);
//        List<Map<String, Object>> mergeDatas = sheet.getMergeDatas(4, true);
//        System.out.println(mergeDatas);
        List<Map<String, Object>> datas = sheet.readDatas(4, true, true);
        System.out.println(datas);
        for (Map<String, Object> data : datas) {
            data.put("unitName", data.get("权属单位"));
            data.put("equityInvestment", "否".equals(data.get("是否参股投资")) ? 0 : 1);
            data.put("projectType", 2);
            data.put("plannedInvestment", getProperty(data, "投资额.0.预计投资额/万元"));
            data.put("actualInvestment", getProperty(data, "投资额.0.完成投资额/万元"));
            Sql.insert("project", data);
            Object id = data.get("id");
            JSONObject y2019 = Av0.js("estimatedRevenue", getProperty(data, "收入指标-2019.0.当年预计收入/万元"),
                    "realIncome", getProperty(data, "收入指标-2019.0.当年实际收入/万元"),
                    "estimatedProfit", getProperty(data, "利润指标-2019.0.当年预计利润/万元"),
                    "realProfit", getProperty(data, "利润指标-2019.0.当年实际利润/万元"),
                    "year", "2019", "projectId", id,
                    "evaluateResults", data.get("2019年评价结果"));
            Sql.insert("year_project_speed", y2019);
        }
    }


    static void newProject(){

        ActiveSheet sheet = ActiveSheet.create(com.black.utils.ServiceUtils.getResource("ds.xlsx"), workbook -> workbook.getSheetAt(0));
        sheet.setTitleIndex(1);
        sheet.setTrimTitle(true);
        sheet.setTitleMergeLenght(2);
        sheet.setDistinctKey("序号");
        Map<String, CellValueWrapper> titleInfo = sheet.getTitleInfo();
        TableMetadata metadata = Sql.getTableMetadata("project");
        sheet.setTitleEscape(com.black.utils.ServiceUtils.groupMetadataByRemark(metadata, new HumpColumnConvertHandler()));
        List<Map<String, Object>> mergeDatas = sheet.getMergeDatas(3, true);
        System.out.println(mergeDatas);
        for (Map<String, Object> mergeData : mergeDatas) {
            Sql.insert("project", mergeData);
            List<Map<String, Object>> TZJD = (List<Map<String, Object>>) mergeData.get("投资进度");
            for (Map<String, Object> map : TZJD) {
                map.put("taskName", map.get("任务分解"));
                map.put("progress", map.get("完成进度"));
                map.put("projectId", mergeData.get("id"));
                Sql.insert("invest_speed", map);
            }
        }
    }
}
