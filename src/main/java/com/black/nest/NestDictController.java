package com.black.nest;

import com.black.core.annotation.ChiefServlet;
import com.black.sql_v2.Sql;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.util.List;

@Api(tags = "nest--dict管理")
@ChiefServlet("v2/nest_dict")
public class NestDictController {

    private final String nestTableName = "t_nest";
    private final String dictTableName = "t_dict";

    @GetMapping("init_nest_dict")
    @ApiOperation("初始化 nest 和 dict")
    void init(){
        Connection connection = Sql.opt().getConnection();
        try {
            NestManager.init(connection);
        }finally {
            Sql.opt().closeConnection();
        }
    }

    @PostMapping("listNest")
    @ApiOperation("查询 nest 列表")
    List<Nest> listNest(@RequestBody Nest nest){
        return Sql.opt().query(nestTableName, nest).javaList(Nest.class);
    }

    @PostMapping("listDict")
    @ApiOperation("查询 dict 列表")
    List<Dict> listDict(@RequestBody Dict dict){
        return Sql.opt().query(dictTableName, dict).javaList(Dict.class);
    }

    @GetMapping("findNestById")
    @ApiOperation("查询 nest 详情")
    Nest findNestById(@RequestParam String id){
        return Sql.opt().queryById(nestTableName, id).javaSingle(Nest.class);
    }

    @GetMapping("findDictById")
    @ApiOperation("查询 dict 详情")
    Dict findDictById(@RequestParam String id){
        return Sql.opt().queryById(dictTableName, id).javaSingle(Dict.class);
    }

    @PostMapping("insertNest")
    @ApiOperation("添加 nest 对象")
    void insertNest(@RequestBody Nest nest){
        Sql.opt().insert(nestTableName, nest);
    }

    @PostMapping("insertDict")
    @ApiOperation("添加 dict 对象")
    void insertDict(@RequestBody Dict dict){
        Sql.opt().insert(dictTableName, dict);
    }

    @PostMapping("updateNest")
    @ApiOperation("更新 nest 对象")
    void updateNest(@RequestBody Nest nest){
        Sql.opt().updateById(nestTableName, nest, nest.getId());
    }

    @PostMapping("updateDict")
    @ApiOperation("更新 dict 对象")
    void updateDict(@RequestBody Dict dict){
        Sql.opt().updateById(dictTableName, dict, dict.getId());
    }

    @GetMapping("deleteNest")
    @ApiOperation("删除 nest 对象")
    void deleteNest(@RequestParam String id){
        Sql.opt().deleteById(nestTableName, id);
    }

    @GetMapping("deleteDict")
    @ApiOperation("删除 dict 对象")
    void deleteDict(@RequestParam String id){
        Sql.opt().deleteById(dictTableName, id);
    }
}
