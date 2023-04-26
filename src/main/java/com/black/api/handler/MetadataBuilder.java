package com.black.api.handler;

import java.sql.Connection;

public interface MetadataBuilder {


    Object buildMatedata(String plane, Connection connection);

}
