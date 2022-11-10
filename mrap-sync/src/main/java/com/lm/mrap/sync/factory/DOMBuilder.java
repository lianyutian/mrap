package com.lm.mrap.sync.factory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/10/20 下午2:17
 */
public class DOMBuilder {
    public DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
}
