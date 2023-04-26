package com.black.core.test.at;

import com.black.role.SkipVerification;
import com.black.core.annotation.ChiefServlet;
import com.black.core.util.Av0;
import com.black.sql_v2.action.AbstractProvideSupportChiefApiController;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

//@ApiController
//@Api(tags = "测试")
@SkipVerification
@ChiefServlet("test") @Getter
public class testAction extends AbstractProvideSupportChiefApiController {

    String tableName = "supplier_bad";


    @Override
    protected Object list0(Map<String, Object> json) {
        return opt().query(tableName, json, "$B: like[src, url]").jsonList();
    }

    @PostMapping("hello")
    @ApiOperation(value = "测试接口")
    Object loc(@RequestBody Usercls usercls){
        return Av0.js("name", "lgp").toJSONString();
    }


    @PostMapping("upload")
    @ApiOperation(value = "上传文件")
    void upload(int id, @RequestPart("execl")MultipartFile execl,
                @RequestPart List<MultipartFile> files, @RequestPart Usercls usercls){

    }

    @GetMapping("t")
    void t(HttpServletRequest request){
        System.out.println(request);
    }
}
