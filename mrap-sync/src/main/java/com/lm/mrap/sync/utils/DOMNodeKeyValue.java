package com.lm.mrap.sync.utils;

/**
 * @author liming
 * @version 1.0
 * @description: XML节点类
 * @date 2022/10/20 下午2:08
 */
public class DOMNodeKeyValue {
    private String key;

    private String value;

    public DOMNodeKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getNodeKey() {
        return key;
    }

    public String getNodeValue() {
        return value;
    }
}
