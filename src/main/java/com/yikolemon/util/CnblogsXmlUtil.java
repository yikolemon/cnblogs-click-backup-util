package com.yikolemon.util;


import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * @author yikolemon
 * @date 2023/8/1 22:14
 * @description
 */
public class CnblogsXmlUtil {


    public static List<Element> getTargetElementByName(Document document){
        Element rootElement = document.getRootElement();
        rootElement.normalize();
        // 通过element对象的elementIterator方法获取迭代器
        Queue<Element> elements=new LinkedList<>();
        elements.add(rootElement);
        //在没找到之前一直层序遍历
        ArrayList<Element> res = new ArrayList<>();
        while (!elements.isEmpty()){
            Element poll = elements.poll();
            Iterator<Element> iterator = poll.elementIterator();
            while (iterator.hasNext()){
                Element next = iterator.next();
                if ("member".equals(next.getName())){
                    res.add(next);
                }else {
                    elements.add(next);
                }
            }
        }
        return res;
    }

    /**
     * 通过List<Element> member列表，获取其中的name-value键值对，以Map<String,String>形式返回
     */
    public static Map<String, Object> getMemberKV(List<Element> list){
        HashMap<String, Object> map = new HashMap<>();
        for (Element tempElement : list) {
            Element nameElement = tempElement.element("name");
            Element valueElement = tempElement.element("value");
            String nameValue = nameElement.getText();
            //在value解析时，会把\n解析成一个defaultText对象，所以需要去除这些对象
            Element valueValue = getValueElements(valueElement);
            assert valueValue != null;
            if (valueValue.content().size() == 0) {
                //不注入
            } else if (valueValue.content().size() == 1) {
                map.put(nameValue, valueValue.content().get(0).getText());
            } else {
                ArrayList<String> strs = new ArrayList<>();
                //说明是array,因为array有\n，content为3
                ArrayList<Element> queue = new ArrayList();
                queue.add(valueValue);
                while (queue.size() > 0) {
                    Element poll = queue.remove(0);
                    Iterator<Element> iterator = poll.elementIterator();
                    while (iterator.hasNext()) {
                        Element cur = iterator.next();
                        if (cur.getNodeType() == Node.ELEMENT_NODE) {
                            if ("string".equals(cur.getName())) {
                                strs.add(cur.getText());
                            } else {
                                queue.add(cur);
                            }
                        }
                    }
                }
                map.put(nameValue, strs);
            }
        }
        return map;
    }

    /**
     * 通过dom4j的document获取其中所有需要的kv对
     */
    public static Map<String,Object> getKVByDocument(Document document){
        List<Element> list = getTargetElementByName(document);
        Map<String, Object> memberKV = getMemberKV(list);
        return memberKV;
    }

    /**因为单个Value的Element还包含一层类型的标签，所以需要特殊处理，取出内容
     * @return Value的String内容
     */
    public static Element getValueElements(Element valueElement){
        Iterator<Element> iterator = valueElement.elementIterator();
        while (iterator.hasNext()) {
            Element next = iterator.next();
            if (next.getNodeType()== Node.ELEMENT_NODE){
                //说明是element节点
                return next;
            }
        }
        return null;
    }

}
