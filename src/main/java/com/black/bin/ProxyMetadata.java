package com.black.bin;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author shkstart
 * @create 2023-04-18 9:53
 */
@Data @AllArgsConstructor
public class ProxyMetadata {

    private final Object proxyBean;

    private final CommonProxyHandler proxyHandler;


}
