package com.black.nio.code;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class NioServerContext extends AbstractNioServerContext{

    public NioServerContext(@NonNull Configuration configuration)  {
        super(configuration);
    }

}
