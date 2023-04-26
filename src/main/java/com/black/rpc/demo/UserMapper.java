package com.black.rpc.demo;

import com.alibaba.fastjson.JSONArray;
import com.black.rpc.annotation.Output;

public interface UserMapper {

    String select(@Output String sql, @Output(required = false) JSONArray params);

}
