package com.black.treaty;

public class TreatyBuilder {


    public static TreatyNetworkScheduler base(TreatyConfig config){
        return new BaseTreatyNetworkScheduler(config);
    }

}
