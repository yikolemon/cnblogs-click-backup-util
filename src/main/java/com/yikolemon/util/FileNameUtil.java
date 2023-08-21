package com.yikolemon.util;

import java.util.regex.Pattern;

/**
 * @author yikolemon
 * @date 2023/8/21 21:58
 * @description
 */
public class FileNameUtil {

    private static final Pattern FilePattern = Pattern.compile("[\\\\/:*?\"<>|]");
    public static String filenameFilter(String str) {
        return str==null?null:FilePattern.matcher(str).replaceAll("-");
    }

}
