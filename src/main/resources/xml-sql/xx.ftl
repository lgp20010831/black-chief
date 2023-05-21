<root>

    <model id="hz">
        3.1 荷载信息
        荷载类型为流动机械荷载，荷载数量m=${size}，最大荷载等级为P${level}。根据第3.2.5条，铺面结构的设计荷载等级为P${level}，设计标准单轮荷载Ps=${hz}kN。
        <for target="list" prefix="---------" suffix="\n">
            ${cw.addLast()}荷载
            （1）荷载基本参数
            荷载名称：集装箱正面吊TL45-5（满载），根据表A.0.5-1，荷载等级为P6，流动机械的单轮荷载Pm=300kN，接地压强q=1.0MPa，回归系数Ac=-0.061，Bc=0.522,与流动机械的当量单轮荷载对应的当量轴载作用次数k=1。
            （2）第ｉ种流动机械当量标准荷载作用次数系数ωi
            当量混凝土面层弯曲刚度Dc=240.825MN·m，当量基层弯曲刚度Db=33.571MN·m，当量地基综合回弹模量Et=60.0MPa，根据公式（C.0.2-5），水泥混凝土面层与基层当量层相对于当量地基的弯曲刚度半径lcb=2.008m，当lcb大于2m时取2m。
            根据公式（A.0.5-1），流动机械当量单轮荷载系数φ= 0.800。根据公式（A.0.4-2），流动机械的当量单轮荷载P~m= Pm*（1+φ）=540kN。根据A.0.3条，换算指数n=17.5，查表A.0.3，动荷系数，γd=1.100，据公式（A.0.4-1），流动机械当量标准荷载作用次数系数ωi=20.384。
            （3）第ｉ种流动机械的年运行次数(次/年) Ni
            第ｉ种流动机械的装运量扩大系数Zi=1，第ｉ种流动机械每年所分担的货运数量(ｔ或TEU箱)Wi=200000TEU，不平衡系数ψ=1.50，轮迹重叠系数Δ=1，第ｉ种装卸机械、运输车辆的常用起吊、装载量(ｔ或TEU箱)wi=1TEU，可以分流的道路、通道条数nt=8，根据公式（3.4.1），Ni=37500(次/年)。
            （4）第ｉ种流动机械的车道系数αi
            根据表3.4.2，αi=0.5。
            3.2 铺面设计使用年限内的标准荷载作用次数Ns
            铺面设计使用年限(年)t=30，需换算的流动机械数量m=1，根据公式（3.4.2），Ns=11465955(次).
        </for>
    </model>

    <model id="controller">
        package ${location.generatePath};

        import com.alibaba.fastjson.JSONObject;
        import com.black.api.GetApiProperty;
        import com.black.api.PostApiProperty;
        import com.black.core.sql.annotation.OpenSqlPage;
        import com.black.core.annotation.ChiefServlet;
        import com.black.core.autoTree.builder.ApiRemark;
        import com.black.core.sql.annotation.OpenTransactional;
        import com.black.sql_v2.action.AbstractSqlOptServlet;
        import lombok.extern.log4j.Log4j2;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestParam;

        import java.io.Serializable;
        import java.util.List;

        @ApiRemark("${source.remark}")
        @ChiefServlet("${source.lowName}") @Log4j2 @OpenTransactional @SuppressWarnings("all")
        public class ${source.className}Controller extends AbstractSqlOptServlet{

        public String getTableName(){
            return "${source.tableName}";
        }


        //****************************************************************
        //          A   U   T   O           C   R   E   A   T   E
        //****************************************************************

        @OpenSqlPage
        @PostApiProperty(url = "list", request = "$S: ${source.tableName}{}  + {pageSize:每页数量, pageNum:当前页数}",
        response = "${source.tableName}[]", remark = "列表查询", hide = true)
        Object list(@RequestBody JSONObject json){
        return list0(json);
        }

        @GetApiProperty(url = "queryById", response = "${source.tableName}{}", remark = "根据id查询", hide = true)
        Object queryById(@RequestParam Serializable id){
        return queryById0(id);
        }

        @PostApiProperty(url = "save", request = "${source.tableName}{}", remark = "更新/添加", hide = true)
        void save(@RequestBody JSONObject json){
        save0(json);
        }

        @GetApiProperty(url = "deleteById", remark = "根据id删除", hide = true)
        void deleteById(@RequestParam Serializable id){
        deleteById0(id);
        }

        }

    </model>

</root>