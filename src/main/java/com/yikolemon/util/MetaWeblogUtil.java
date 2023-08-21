package com.yikolemon.util;


public class MetaWeblogUtil {
    public static String getBody(String[] strs){
        String before="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<methodCall>\n" +
                "  <methodName>metaWeblog.getPost</methodName>\n" +
                "  <params>";

        String after="  </params>\n" +
                "</methodCall>";
        StringBuilder builder=new StringBuilder(before);
        for (String str : strs) {
            builder.append(getParamStr(str));
        }
        builder.append(after);
        return builder.toString();
    }

    public static String getParamStr(String parm){
        String before="    <param>\n" +
                "        <value><string>";
        String after="</string></value>\n" +
                "    </param>";
        return before+parm+after;
    }

}
