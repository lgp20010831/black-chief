package com.black.excel;

import com.black.core.log.IoLog;
import com.black.core.log.LogFactory;
import lombok.Getter;
import lombok.Setter;

/* 读取 excel 各种条件配置类 */
@Setter @Getter
public class Configuration {


    private ExcelReader excelReader;

    private IoLog log = LogFactory.getLog4j();

}
