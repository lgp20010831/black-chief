package com.black.weixin;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

//子类 feign 接口去继承, 没找到配置注入的方法
//参考文档地址 https://developer.work.weixin.qq.com/document/
//@FeignClient(name = "chief_weixin_feign$1", url = "https://qyapi.weixin.qq.com")
public interface WeixinFeign {

    //获取企业微信 token
    @GetMapping("cgi-bin/gettoken")
    JSONObject getAccessToken(@RequestParam String corpid, @RequestParam String corpsecret);

    //发送应用消息接口
    @PostMapping("cgi-bin/message/send")
    JSONObject sendAppMessage(@RequestParam("access_token") String token, @RequestBody JSONObject json);

    //推送群聊消息
    @PostMapping("cgi-bin/appchat/send")
    JSONObject sendChatMessage(@RequestParam("access_token") String token, @RequestBody JSONObject json);


}
