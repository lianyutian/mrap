package com.lm.mrap.nodemanager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liming
 * @version 1.0
 * @description: 节点抽象接口
 * @date 2022/11/14 下午2:38
 */
public interface Node {
    String name();

    String path();

    Node parent();

    List<Node> children();

    void write(byte[] data);

    void write(String data);

    byte[] readAsBytes();

    String readAsString();

    <H, E extends  NodeWatchEvent<H>> void watch(E event, H hadndler, AtomicBoolean watchAgain);
}
