package com.black.core.sql.code.dome;

import com.black.core.sql.annotation.*;
import com.black.core.sql.code.mapping.GlobalParentMapping;

import java.util.List;
import java.util.Map;

@OpenEntity
//@GlobalConfiguration(logImpl = SystemLog.class)
@MapperLocations("classpath*:iu/*.xml")
@GlobalSetPlatform("amend[${idt}, ${dt}], update[${ut}]")
@GlobalPlatform("select[${df}], insert[${it}, ${ut}, ${uuids}, ${df}], update[${df}], delete[${df}]")
public interface ParentMapper extends GlobalParentMapping {


    @RunScript("select * from ^{tableName} where name like '^{name}%' and plm_use_state = #{state} and team = #{team}")
    List<Map<String, Object>> select(String tableName, String name, Boolean state, String team);

    @Configurer
    @Dictionary({"${dict(typeName, ZZZT, project_type, d1)}", "${dict(deveName, BGLX, develop_type, d2)}"})
    List<Map<String, Object>> getProject();


    @Configurer
    @ProvidePlatform({"amend", "update"})
    boolean updateProgress(@EQ String progress_type, @EQ String project_supplier_id, @Set String state);


    @RunScript("  select \n" +
            "               string_agg(r.psid::varchar, ',') psid, r.plmid,\n" +
            "               round(round( r.sort :: NUMERIC / T.total :: NUMERIC, 2) * 100, 0) gro\n" +
            "        from(\n" +
            "             select\n" +
            "             rank() over(PARTITION by pro.state, p.plm_project_id order by sort desc) rn,\n" +
            "             pro.progress_type, pro.state, pro.sort, p.plm_project_id plmid, ps.id psid\n" +
            "             from\n" +
            "             project p\n" +
            "             left join project_supplier ps on ps.project_id = p.id\n" +
            "             left join progress pro on pro.project_supplier_id = ps.id and pro.state = '2'\n" +
            "\n" +
            "         ) r\n" +
            "        left join(\n" +
            "            select\n" +
            "                p.plm_project_id plmid, max(pro.sort) total\n" +
            "            from\n" +
            "                project p\n" +
            "                    left join project_supplier ps on ps.project_id = p.id\n" +
            "                    left join progress pro on pro.project_supplier_id = ps.id\n" +
            "            group by p.plm_project_id\n" +
            "        ) t on t.plmid = r.plmid\n" +
            "        where r.rn = 1 and r.plmid = #{plmid} GROUP BY r.plmid, round(round( r.sort :: NUMERIC / T.total :: NUMERIC, 2) * 100, 0) ")
    Map<String, Object> getProjectProgress(String plmid);
}
