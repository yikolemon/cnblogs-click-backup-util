package com.yikolemon.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 文章实体类
 **/

@Data
@Accessors(chain = true)
public class Article {

    @SerializedName(value = "id",alternate = {"Id"})
    private String id;//文章主键

    @SerializedName(value = "title",alternate = {"Title"})
    private String title; //文章名

    @SerializedName(value = "content",alternate ={"Content"})
    private String content; //文章内容

    @SerializedName(value = "createTime",alternate = {"PostDate"})
    private Date createTime;

    //文章标签
    @SerializedName(value = "tags")
    private List<String> tags;

    //文章分类
    @SerializedName(value = "category")
    private String category;

}


