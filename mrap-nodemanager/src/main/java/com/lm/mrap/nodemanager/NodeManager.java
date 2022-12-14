package com.lm.mrap.nodemanager;

import com.lm.mrap.nodemanager.zookeeper.ZookeeperNodeManager;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface NodeManager {

    static NodeManager createNodeManager(String hosts, int sessionTimeout, int connecTimeout)
            throws IOException, InterruptedException {
        return new ZookeeperNodeManager(hosts, sessionTimeout, connecTimeout);
    }

    void reconnect() throws Exception;

    Node openNode(String path);

    Node createNode(String path, NodeLifecycleType nodeLifecycleType);

    Node createNode(String path, String data, NodeLifecycleType nodeLifecycleType);

    Node createNode(String path, String data, Charset charset, NodeLifecycleType nodeLifecycleType);

    Node createNode(String path, byte[] data, NodeLifecycleType nodeLifecycleType);

    Node createRecurisionNode(String path, NodeLifecycleType nodeLifecycleType);

    Node createRecurisionNode(String path, String data, NodeLifecycleType nodeLifecycleType);

    Node createRecurisionNode(String path, String data, Charset charset, NodeLifecycleType nodeLifecycleType);

    Node createRecurisionNode(String path, byte[] data, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(String path, String nodeName, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(String path, String nodeName, String data, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(String path, String nodeName, String data, Charset charset, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(String path, String nodeName, byte[] data, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(Node parent, String nodeName, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(Node parent, String nodeName, String data, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(Node parent, String nodeName, String data, Charset charset, NodeLifecycleType nodeLifecycleType);

    Node createChildNode(Node parent, String nodeName, byte[] data, NodeLifecycleType nodeLifecycleType);

    boolean nodeExists(String path);

    void deleteNode(String path);

    void deleteNode(Node node);

    List<Node> getChildren(String path);

    <H, E extends NodeWatchEvent<H>> void watch(String path, E event, H handler, AtomicBoolean watchAgain);

    boolean isConnected();

    void close();

}
