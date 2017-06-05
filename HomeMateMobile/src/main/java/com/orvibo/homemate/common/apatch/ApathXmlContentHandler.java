package com.orvibo.homemate.common.apatch;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by wuliquan on 2016/5/8.
 */
public class ApathXmlContentHandler extends DefaultHandler {

    //声明一个装载ApatchItem类型的List
    private ArrayList<ApatchItem> mList;
    //声明一个ApatchItem类型的变量
    private ApatchItem apatchItem;
    //声明一个字符串变量
    private String content;

    /**
     * MySaxHandler的构造方法
     *
     * @param list 装载返回结果的List对象
     */
    public ApathXmlContentHandler(ArrayList<ApatchItem> list){
        this.mList = list;
    }

    /**
     * 当SAX解析器解析到XML文档开始时，会调用的方法
     */
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    /**
     * 当SAX解析器解析到XML文档结束时，会调用的方法
     */
    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    /**
     * 当SAX解析器解析到某个属性值时，会调用的方法
     * 其中参数ch记录了这个属性值的内容
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        content = new String(ch, start, length);
    }

    /**
     * 当SAX解析器解析到某个元素开始时，会调用的方法
     * 其中localName记录的是元素属性名
     */
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);

        if("apatch".equals(localName)){
            apatchItem = new ApatchItem(); //新建Video对象
        }
    }

    /**
     * 当SAX解析器解析到某个元素结束时，会调用的方法
     * 其中localName记录的是元素属性名
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);

        if("name".equals(localName)){
            apatchItem.setName(content);
        }else if("md5".equals(localName)){
            apatchItem.setMd5(content);
        }else if("target".equals(localName)){
            apatchItem.setVersionList(content);
        }else if ("url".equals(localName)){
            apatchItem.setUrl(content);
        }
        else if("apatch".equals(localName)){
            //将apatchItem对象加入到List中
            mList.add(apatchItem);
        }
    }
}
