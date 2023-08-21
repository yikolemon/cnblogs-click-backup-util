package com.yikolemon.dao;

import com.yikolemon.entity.Article;
import com.yikolemon.exception.FetchBlogException;
import com.yikolemon.util.CnblogsXmlUtil;
import com.yikolemon.util.Map2EntityUtil;
import com.yikolemon.util.MetaWeblogUtil;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author yikolemon
 * @date 2023/7/30 22:51
 * @description
 */
@Component
public class MetaWbelogClient {


    @Value("${cnblogs.url}")
    private String url;

    @Value("${cnblogs.username}")
    private String username;


    @Value("${cnblogs.token}")
    private String token;

    public Article getPost(String id) throws FetchBlogException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type","application/xml");
        String body = MetaWeblogUtil.getBody(new String[]{id, username, token});
        httpPost.setEntity(new StringEntity(body));
        try {
            try (CloseableHttpClient client= HttpClients.createDefault()) {
                try (CloseableHttpResponse response = client.execute(httpPost)) {
                    int resCode = response.getCode();
                    if (resCode!=200){
                        //出错
                        throw new FetchBlogException("获取博客内容失败");
                    }else {
                        HttpEntity entity = response.getEntity();
                        String xml = EntityUtils.toString(entity);
                        Document document = DocumentHelper.parseText(xml);
                        Map<String, Object> articleMap = CnblogsXmlUtil.getKVByDocument(document);
                        return Map2EntityUtil.maptoArticle(articleMap);
                    }
                } catch (DocumentException | ParseException e ) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }}

}
