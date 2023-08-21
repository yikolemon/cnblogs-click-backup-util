package com.yikolemon.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TokenUrlUtil {


    @Value("${cnblogs.oauth2.authorizeUrl}")
    public String url=null;


    public String getUrl(){
        return url+ "&nonce="+UUID.randomUUID();
    }

}