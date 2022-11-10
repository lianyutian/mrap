package com.lm.mrap.sync.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.concurrent.ConcurrentHashMap;

import static com.lm.mrap.sync.config.DOMConfig.ATTRIBUTES;

/**
 * @author liming
 * @version 1.0
 * @description: xml DOM树类
 * @date 2022/10/20 下午2:52
 */
public class DOMTreeModel {
    private Document document;

    private ConcurrentHashMap<DOMNodeKeyValue, String> domConf;

    public DOMTreeModel(Document document) {

        this.document = document;
        this.domConf = new ConcurrentHashMap<>();

    }

    synchronized public ConcurrentHashMap<DOMNodeKeyValue, String> foreachForDOM() {
        for (Node childNode = getFirstChildNode(); childNode != null; childNode = childNode.getNextSibling()) {
            NodeList childNodesOfNode = getChildNodesOfNode(childNode);
            nodeListParse(childNodesOfNode, getNodeName(childNode));
        }

        return domConf;
    }

    private Node getFirstChildNode() {
        return getParentElement().getFirstChild();
    }

    private Element getParentElement() {
        return document.getDocumentElement();
    }

    private NodeList getChildNodesOfNode(Node node) {
        return node.getChildNodes();
    }

    private void nodeListParse(NodeList nodeList, String nodeName) {
        for (int i = 1; i <= getLengthOfNode(nodeList); i++) {
            Node item = nodeList.item(i);
            if (item instanceof Element) {
                Element element = (Element) item;
                getKeyValueForNode(element, nodeName);
            }
        }
    }

    private int getLengthOfNode(NodeList nodeList) {
        return nodeList.getLength();
    }

    private void getKeyValueForNode(Element element, String nodeName) {
        String key = element.getAttribute(ATTRIBUTES[0]);
        String value = element.getAttribute(ATTRIBUTES[1]);

        value = getValue(value, element);
        DOMNodeKeyValue keyValue = new DOMNodeKeyValue(key, value);
        domConf.put(keyValue, nodeName);
    }

    private String getValue(String value, Element element) {
        if (value.isEmpty()) {
            return element.getAttribute(ATTRIBUTES[2]);
        }
        return value;
    }

    private String getNodeName(Node node) {
        return node.getNodeName();
    }
}
