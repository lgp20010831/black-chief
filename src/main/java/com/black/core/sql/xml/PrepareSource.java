package com.black.core.sql.xml;

import com.black.core.sql.code.AliasColumnConvertHandler;
import lombok.Getter;

import java.sql.Connection;

/**
 * @author 李桂鹏
 * @create 2023-05-08 9:52
 */
@SuppressWarnings("all") @Getter
public class PrepareSource {


    private final Connection connection;


    private final AliasColumnConvertHandler convertHandler;

    public PrepareSource(Connection connection, AliasColumnConvertHandler convertHandler) {
        this.connection = connection;
        this.convertHandler = convertHandler;
    }
}
