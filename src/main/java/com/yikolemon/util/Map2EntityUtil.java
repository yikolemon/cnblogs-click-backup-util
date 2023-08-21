package com.yikolemon.util;

import cn.hutool.core.bean.BeanUtil;
import com.yikolemon.entity.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 部分map的key命名不好，使用util将之转化为所需的entity
 * @author yikolemon
 * @date 2023/8/1 23:02
 * @description
 */
public class Map2EntityUtil {


    public static Article maptoArticle(Map<String,Object> map) {
        //时间进行特殊处理注入
        String dateCreated = (String) map.remove("dateCreated");
        //将description修改为content
        String description = (String) map.get("description");
        map.remove("description");
        map.put("content",description);
        String postid = (String) map.remove("postid");
        map.put("id",postid);
        List<String> categories = (List<String>)map.remove("categories");
        for (String category : categories) {
            if (category.contains("随笔分类")) {
                map.put("category", category.replace("[随笔分类]", ""));
                break;
            }
        }
        if (map.containsKey("mt_keywords")){
            String keywordsStr = (String)map.remove("mt_keywords");
            String[] keywords = keywordsStr.split(",");
            map.put("tags",keywords);
        }
        Article article = BeanUtil.mapToBean(map, Article.class, true);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
        try {
            Date parse = simpleDateFormat.parse(dateCreated);
            article.setCreateTime(parse);
            return article;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
