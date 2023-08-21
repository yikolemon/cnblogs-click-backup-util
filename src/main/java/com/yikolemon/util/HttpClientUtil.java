package com.yikolemon.util;

import org.apache.hc.client5.http.config.RequestConfig;

import java.util.concurrent.TimeUnit;

public class HttpClientUtil {

    public static RequestConfig getTimeOutRequestConfig(){
        return RequestConfig.custom()
                .setConnectionRequestTimeout(1, TimeUnit.MINUTES)
                .setResponseTimeout(1,TimeUnit.MINUTES)
                .build();
    }
}
