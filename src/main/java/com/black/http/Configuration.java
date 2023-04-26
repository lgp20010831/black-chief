package com.black.http;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Configuration {

    private String httpHost = "0.0.0.0";

    private int httpPort = 8000;

    private String remoteHost = "0.0.0.0";

    private int remotePort = 7000;

    private int threadNum = 30;

    private HttpTransitAgreement transitAgreement;

}
