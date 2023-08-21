package com.yikolemon.dao;

import cn.hutool.core.io.IoUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yikolemon.entity.Article;
import com.yikolemon.entity.AuthToken;
import com.yikolemon.exception.ErrorAuthException;
import com.yikolemon.exception.FetchBlogException;
import com.yikolemon.util.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class Oauth2BlogClient {

    private static final String CNBLOGS_URI = "https://api.cnblogs.com/api";

    @Value("${cnblogs.username}")
    private String userName;

    @Value("${cnblogs.oauth2.client_id}")
    private  String clientId = null;

    @Value("${cnblogs.oauth2.client_secret}")
    private String clientSecret = null;

    /**
     * 获取所有的博客列表（无内容,分类,标签）
     */
    public void auth(String code){
        HttpPost post = new HttpPost("https://api.cnblogs.com/token");
        ArrayList<NameValuePair> body = new ArrayList<>();
        body.add(new BasicNameValuePair("client_id",clientId));
        body.add(new BasicNameValuePair("client_secret",clientSecret));
        body.add(new BasicNameValuePair("grant_type","authorization_code"));
        body.add(new BasicNameValuePair("code",code));
        body.add(new BasicNameValuePair("redirect_uri","https://oauth.cnblogs.com/auth/callback"));
        post.setEntity(new UrlEncodedFormEntity(body));
        post.setConfig(HttpClientUtil.getTimeOutRequestConfig());
        try (CloseableHttpClient client= HttpClients.createDefault()){
            try (CloseableHttpResponse response = client.execute(post)) {
                int resCode = response.getCode();
                if (resCode!=200){
                    //出错
                    throw new ErrorAuthException("认证错误");
                }else {
                    HttpEntity entity = response.getEntity();
                    String res = EntityUtils.toString(entity);
                    Map<String,String> resMap= new Gson().fromJson(res, Map.class);
                    //登录成功了，在本地存储access_token和refresh_token；
                    AuthToken.access_token=resMap.get("access_token");
                    //AuthToken.refresh_token=resMap.get("refresh_token");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<Article> getAllArticleList() throws FetchBlogException {
        int curPage = 1;
        boolean hasNextPage = true;
        ArrayList<Article> cnblogsArticleList;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            cnblogsArticleList = new ArrayList<>();
            while (hasNextPage) {
                StringBuilder builder = new StringBuilder(CNBLOGS_URI);
                builder.append("/blogs/");
                builder.append(userName);
                builder.append("/posts/?pageIndex=");
                builder.append(curPage);
                HttpGet httpGet = new HttpGet(builder.toString());
                httpGet.setConfig(HttpClientUtil.getTimeOutRequestConfig());
    //            httpGet.addHeader("Authorization", "Bearer " + AuthToken.access_token);
                httpGet.addHeader("Authorization", "Bearer " + AuthToken.access_token);

                try {
                    CloseableHttpResponse response = client.execute(httpGet);
                    int code = response.getCode();
                    if (code != 200) {
                        log.error(builder + "===>" + code);
                        throw new FetchBlogException("认证错误");
                    } else {
                        HttpEntity entity = response.getEntity();
                        try (InputStream inputStream = entity.getContent();
                             InputStreamReader reader = new InputStreamReader(inputStream)) {
                            String responseJson = IoUtil.read(reader);
                            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
                            Article[] articles = gson.fromJson(responseJson, Article[].class);
                            if (articles != null && articles.length > 0) {
                                //说明反序列化到了Article
                                Collections.addAll(cnblogsArticleList, articles);
                            } else {
                                hasNextPage = false;
                            }
                        }
                    }
                    curPage++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cnblogsArticleList;
    }


}
