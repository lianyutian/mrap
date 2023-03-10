package com.lm.mrap.nodemanager.zookeeper;

import com.lm.mrap.common.config.CommonConfig;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.nodemanager.Node;
import com.lm.mrap.nodemanager.NodeExceptions;
import com.lm.mrap.nodemanager.NodeLifecycleType;
import com.lm.mrap.nodemanager.NodeManager;
import com.lm.mrap.nodemanager.NodeWatchEvent;
import io.netty.util.internal.EmptyArrays;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZookeeperNodeManager implements NodeManager {

    public final static int VERSION_ID = -1;

    private ZooKeeper zkClient;
    private final String hosts;
    private final int sessionTimeout;
    private final int connectionTimeout;

    public ZookeeperNodeManager(String hosts, int sessionTimeout, int connectionTimeout)
            throws IOException, InterruptedException {
        this.hosts = hosts;
        this.sessionTimeout = sessionTimeout;
        this.connectionTimeout = connectionTimeout;

        reconnect();
    }

    public synchronized void reconnect() throws InterruptedException, IOException {

        long end = System.currentTimeMillis() + connectionTimeout;

        if (zkClient != null) {

            zkClient.close();
        }
        zkClient = new ZooKeeper(hosts, sessionTimeout, null);

        while (zkClient.getState() != ZooKeeper.States.CONNECTED) {

            if (end <= System.currentTimeMillis()) {
                break;
            }

            TimeUnit.MILLISECONDS.sleep(100);
        }

        if (zkClient.getState() != ZooKeeper.States.CONNECTED) {

            throw new IOException("zk host : " + hosts + "不能连接");
        }

    }

    private static String combinePath(String parentPath, String childName) {

        return new StringBuilder()
                .append(parentPath)
                .append(StringUtil.SLASH_STRING)
                .append(childName).toString();
    }

    @Override
    public Node openNode(String path) {
        return new ZookeeperNode(path, zkClient, this);
    }

    @Override
    public Node createNode(String path, NodeLifecycleType nodeLifecycleType) {
        return createNode(path, StringUtil.EMPTY_STRING.getBytes(CommonConfig.DEFAULT_CHARSET), nodeLifecycleType);
    }

    @Override
    public Node createNode(String path, String data, NodeLifecycleType nodeLifecycleType) {
        return createNode(path, data.getBytes(CommonConfig.DEFAULT_CHARSET), nodeLifecycleType);
    }

    @Override
    public Node createNode(String path, String data, Charset charset, NodeLifecycleType nodeLifecycleType) {
        return createNode(path, data.getBytes(charset), nodeLifecycleType);
    }

    @Override
    public Node createNode(String path, byte[] data, NodeLifecycleType nodeLifecycleType) {
        CreateMode createMode = ZkExchange.exchangeCreateMode(nodeLifecycleType);

        try {

            zkClient.create(path, data, ZkExchange.DEFAULT_ACL, createMode);
        } catch  (KeeperException.NodeExistsException e) {
            throw new NodeExceptions.NodeExistsException(e);
        } catch (Exception e) {
            throw new NodeExceptions.NodeCreationException(e);
        }

        return openNode(path);
    }

    @Override
    public Node createRecurisionNode(String path, NodeLifecycleType nodeLifecycleType) {
        return createRecurisionNode(path, EmptyArrays.EMPTY_BYTES, nodeLifecycleType);
    }

    @Override
    public Node createRecurisionNode(String path, String data, NodeLifecycleType nodeLifecycleType) {
        return createRecurisionNode(path, data.getBytes(), nodeLifecycleType);
    }

    @Override
    public Node createRecurisionNode(String path, String data, Charset charset, NodeLifecycleType nodeLifecycleType) {
        return createRecurisionNode(path, data.getBytes(charset), nodeLifecycleType);
    }

    @Override
    public Node createRecurisionNode(String path, byte[] data, NodeLifecycleType nodeLifecycleType) {
        Node node = openNode(path);

        String parentPath = node.parent().path();
        if (!nodeExists(parentPath)) {
            createRecurisionNode(parentPath, data, NodeLifecycleType.PERSIST);
        }

        return createNode(path, data, nodeLifecycleType);
    }

    @Override
    public Node createChildNode(String parentPath, String nodeName, NodeLifecycleType nodeLifecycleType) {

        return createNode(
                combinePath(parentPath, nodeName),
                StringUtil.EMPTY_STRING.getBytes(CommonConfig.DEFAULT_CHARSET),
                nodeLifecycleType);
    }

    @Override
    public Node createChildNode(String parentPath, String nodeName, String data, NodeLifecycleType nodeLifecycleType) {

        return createNode(
                combinePath(parentPath, nodeName),
                data.getBytes(CommonConfig.DEFAULT_CHARSET),
                nodeLifecycleType
        );
    }

    @Override
    public Node createChildNode(String parentPath, String nodeName, String data, Charset charset, NodeLifecycleType nodeLifecycleType) {

        return createNode(
                combinePath(parentPath, nodeName),
                data,
                nodeLifecycleType
        );
    }

    @Override
    public Node createChildNode(String parentPath, String nodeName, byte[] data, NodeLifecycleType nodeLifecycleType) {
        return createNode(
                combinePath(parentPath, nodeName),
                data,
                nodeLifecycleType
        );
    }

    @Override
    public Node createChildNode(Node parent, String nodeName, NodeLifecycleType nodeLifecycleType) {
        return createNode(
                combinePath(parent.path(), nodeName),
                StringUtil.EMPTY_STRING.getBytes(CommonConfig.DEFAULT_CHARSET),
                nodeLifecycleType
        );
    }

    @Override
    public Node createChildNode(Node parent, String nodeName, String data, NodeLifecycleType nodeLifecycleType) {
        return createNode(
                combinePath(parent.path(), nodeName),
                data.getBytes(CommonConfig.DEFAULT_CHARSET),
                nodeLifecycleType
        );
    }

    @Override
    public Node createChildNode(Node parent, String nodeName, String data, Charset charset, NodeLifecycleType nodeLifecycleType) {
        return createNode(
                combinePath(parent.path(), nodeName),
                data.getBytes(charset),
                nodeLifecycleType
        );
    }

    @Override
    public Node createChildNode(Node parent, String nodeName, byte[] data, NodeLifecycleType nodeLifecycleType) {
        return createNode(
                combinePath(parent.path(), nodeName),
                data,
                nodeLifecycleType
        );
    }

    @Override
    public boolean nodeExists(String path) {
        try {

            return zkClient.exists(path, false) != null;
        } catch (Throwable e) {

            throw new NodeExceptions.NodeExistsException(e);
        }
    }

    @Override
    public void deleteNode(String path) {
        try {

            if (nodeExists(path)) {

                List<Node> childNodes = getChildren(path);

                if (childNodes != null && !childNodes.isEmpty()) {
                    for (Node childNode : childNodes) {
                        deleteNode(childNode.path());
                    }
                }

                zkClient.delete(path, VERSION_ID);
            }
        } catch (Throwable throwable) {

            throw new NodeExceptions.NodeDeleteException(throwable);
        }
    }

    @Override
    public void deleteNode(Node node) {

        deleteNode(node.path());
    }

    @Override
    public List<Node> getChildren(String path) {
        return openNode(path).children();
    }

    @Override
    public <H, E extends NodeWatchEvent<H>> void watch(String path, E event, H handler, AtomicBoolean watchAgain) {

        openNode(path).watch(event, handler, watchAgain);
    }

    @Override
    public boolean isConnected() {

        ZooKeeper.States state = zkClient.getState();
        return state.isAlive() && state.isConnected();
    }

    @Override
    public void close() {

        try {
            zkClient.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
