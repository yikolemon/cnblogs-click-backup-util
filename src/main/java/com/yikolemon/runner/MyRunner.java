package com.yikolemon.runner;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import com.yikolemon.dao.MetaWbelogClient;
import com.yikolemon.dao.Oauth2BlogClient;
import com.yikolemon.entity.Article;
import com.yikolemon.exception.FetchBlogException;
import com.yikolemon.util.BareBonesBrowserLaunch;
import com.yikolemon.util.FileNameUtil;
import com.yikolemon.util.TokenUrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.util.UriEncoder;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Component
public class MyRunner implements ApplicationRunner {

    @Autowired
    private Oauth2BlogClient oauth2BlogClient;

    @Autowired
    private MetaWbelogClient metaWbelogClient;


    @Autowired
    private TokenUrlUtil tokenUrlUtil;


    private String storageDirectory;

    private String pictureDirectory;

    @Value("${download_picture}")
    private Boolean downloadPicture;

    @Value("${storageDirectory}")
    public void setStorageDirectory(String storageDirectory) {
        String s = storageDirectory.replaceAll("\\\\", "/");
        this.storageDirectory = "file:///"+s;
        this.pictureDirectory = "file:///"+s + "/pictures";
    }

    @Override
    public void run(ApplicationArguments args) {
        //读取配置文件
        URI postUri;
        URI pictureUri;
        URI cnblogs;
        try {
            postUri= new URI(storageDirectory);
            pictureUri=new URI(pictureDirectory);
            cnblogs = new URI(storageDirectory + "/cnblogs");
        } catch (URISyntaxException e) {
            System.out.println("本地存储路径错误");
            System.out.println("=====按回车键退出=====");
            finish();
            return;
        }
        //创建目录
        File file = new File(postUri);
        file.mkdirs();

        if (file.isFile()){
            System.out.println("本地存储路径不为目录");
            System.out.println("=====按回车键退出=====");
            finish();
            return;
        }

        if (Boolean.TRUE.equals(downloadPicture)){
            File pictureFile = new File(pictureUri);
            pictureFile.mkdirs();
            File cnblogsFile = new File(cnblogs);
            cnblogsFile.mkdirs();
        }
        System.out.println("=====请登录博客园并复制access token=====");
        System.out.print("输入你的Code:");
        BareBonesBrowserLaunch.openURL(tokenUrlUtil.getUrl());//调用工具类BareBonesBrowserLaunch类中的openURL方法实现页面跳转
        String code = new Scanner(System.in).next();
        oauth2BlogClient.auth(code);
        List<Article> allArticleIdList;
        try {
            allArticleIdList = oauth2BlogClient.getAllArticleList();
        } catch (FetchBlogException e) {
            System.out.println(e.getMessage());
            System.out.println("=====按回车键退出=====");
            finish();
            return;
        }
        if (allArticleIdList==null){
            System.out.println("文章列表获取失败");
            System.out.println("=====按回车键退出=====");
            finish();
            return;
        }
        System.out.println("=====博客共"+allArticleIdList.size()+"篇=====");
        for (Article tempArticle : allArticleIdList) {
            Article article;
            try {
                article = metaWbelogClient.getPost(tempArticle.getId());
            } catch (FetchBlogException e) {
                System.out.println(e.getMessage() + "===>" + tempArticle.getTitle());
                continue;
            }
            if (article == null) {
                System.out.println("博客获取失败===>" + tempArticle.getTitle());
                continue;
            }
            try {
                String tempTitle = FileNameUtil.filenameFilter(article.getTitle());
                article.setTitle(tempTitle);
                String originalPath;
                // 第一份文件：原始内容，保留原始链接，不下载图片
                if (!Boolean.TRUE.equals(downloadPicture)){
                    originalPath = postUri + "/" + article.getTitle() + ".md";
                }else{
                    originalPath = postUri + "/cnblogs/" + article.getTitle() + "-cnblogs.md";
                }
                saveToFile(originalPath, article.getContent());

                if (Boolean.TRUE.equals(downloadPicture)){
                    // 第二份文件：下载图片并替换链接
                    String modifiedPath = postUri + "/" + article.getTitle() + ".md";
                    String modifiedContent = replaceImagesWithLocalPaths(article.getContent());
                    saveToFile(modifiedPath, modifiedContent);

                }
                System.out.println("备份成功===>" + article.getTitle());
            } catch (URISyntaxException e) {
                System.out.println("文件存储位置错误===>" + article.getTitle());
            } catch (IOException e) {
                System.out.println("IO错误===>" + article.getTitle());
            }
        }

        System.out.println("备份完成，按回车键退出");
        finish();
    }

    // 保存内容到指定文件路径
    private void saveToFile(String filePath, String content) throws URISyntaxException, IOException {
        String encode = UriEncoder.encode(filePath);
        URI fileUri = new URI(encode);
        Writer fw = new FileWriter(new File(fileUri));
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.flush();
        bw.close();
    }

    // 下载图片并替换Markdown中的图片链接为本地路径
    private String replaceImagesWithLocalPaths(String content) {
        // 匹配 ![alt](image_url) 格式的正则表达式
        String regex = "!\\[(.*?)\\]\\((.*?)\\)";
        return ReUtil.replaceAll(content, regex, (matcher) -> {
            String imageUrl = matcher.group(2); // 获取图片的URL

            // 下载图片
            String fileName = pictureDirectory + "/" + FileUtil.getName(imageUrl);
            HttpUtil.downloadFile(imageUrl, FileUtil.file(fileName));

            // 返回新的Markdown语法，替换图片的URL为本地路径
            return "![" + matcher.group(1) + "](" + fileName + ")";
        });
    }

    public void finish(){
        try {
            System.in.read(); // 阻塞，直到输入
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}