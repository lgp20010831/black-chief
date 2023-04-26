package com.black.core.io.bio.dome;

import com.black.core.io.bio.BioServerImpl;
import com.black.core.io.bio.Configuration;

public class Server {


    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        BioServerImpl server = new BioServerImpl(configuration);
        server.bind();
    }

}
