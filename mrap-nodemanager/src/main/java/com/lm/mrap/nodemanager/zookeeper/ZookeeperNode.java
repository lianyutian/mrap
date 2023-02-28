package com.lm.mrap.nodemanager.zookeeper;

import com.lm.mrap.common.config.CommonConfig;
import com.lm.mrap.common.exceptions.CommonException;
import com.lm.mrap.common.exceptions.ExceptionConditions;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.nodemanager.*;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.jooq.lambda.tuple.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 将zookeeper中的一个文件映射为一个节点，其中每个文件可以写入数据，也可以被建立子文件
 */
public class ZookeeperNode implements Node {

    private final ZooKeeper zkClient;

    private final String path;

    private final String name;

    private final String parentPath;

    private final ZookeeperNodeManager zkNodeManager;

    public ZookeeperNode(String path, ZooKeeper zkClient, ZookeeperNodeManager zkNodeManager) {

        // 传入的构造方法的参数都不能为null
        CommonException.throwNotAllowIfNull(
                "节点路径是null，或者zkClient是null，或者zkNodeManager是null",
                CommonException.Logic.OR,
                path,
                zkClient,
                zkNodeManager
        );

        // path参数不能是空串，并且也必须是一个路径（包含/）
        CommonException.throwNotAllow(
                "节点路径是空的字符串或不是路径",
                ExceptionConditions.combineCondition(
                        ExceptionConditions.STRING_IS_EMPTY,
                        ExceptionConditions.STRING_NOT_PATH,
                        CommonException.Logic.OR),
                new Tuple2<>(path, path));

        this.zkNodeManager = zkNodeManager;

        // 如果path是 / 路径则直接使用，如果path最后一个字符是 / 则将它去掉
        this.path = path.endsWith(StringUtil.SLASH_STRING) && path.length() != 1
                ? path.substring(0, path.length() - 1) : path;

        this.zkClient = zkClient;

        // 计算父节点路径，如果节点是根路径，则没有父节点，如果不是根路径，则从源路径中截取它的父路径
        if (path.equals("/")) {
            this.parentPath = null;
            this.name = "/";
        } else {
            String[] pathItems = path.split(StringUtil.SLASH_STRING);

            this.name = pathItems[pathItems.length - 1];

            int slashIndex = path.lastIndexOf(StringUtil.SLASH_STRING);

            if (slashIndex == 0) {
                this.parentPath = StringUtil.SLASH_STRING;
            } else {
                this.parentPath = path.substring(0, path.lastIndexOf(StringUtil.SLASH_STRING));
            }
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public Node parent() {
        return zkNodeManager.openNode(parentPath);
    }

    @Override
    public List<Node> children() {

        List<String> childNames;

        try {
            childNames = zkClient.getChildren(path, false);
        } catch (Throwable e) {
            throw new NodeExceptions.NodeGetChildException(e);
        }

        List<Node> childNodes = new ArrayList<>(childNames.size());

        for (String childName : childNames) {
            childNodes.add(
                    zkNodeManager.openNode(StringUtil.combinePath(path, childName))
            );
        }

        return childNodes;
    }

    @Override
    public void write(byte[] data) {

        try {

            zkClient.setData(path, data, ZookeeperNodeManager.VERSION_ID);
        } catch (Throwable e) {

            throw new NodeExceptions.NodeWriteException(e);
        }
    }

    @Override
    public void write(String data) {
        write(data.getBytes(CommonConfig.DEFAULT_CHARSET));
    }

    @Override
    public byte[] readAsBytes() {

        try {

            return zkClient.getData(
                    path,
                    false,
                    zkClient.exists(path, false)
            );
        } catch (Throwable e) {

            throw new NodeExceptions.NodeReadException(e);
        }
    }

    @Override
    public String readAsString() {

        return new String(
                readAsBytes(),
                CommonConfig.DEFAULT_CHARSET
        );
    }

    @Override
    public <H, E extends NodeWatchEvent<H>> void watch(E event, H handler, AtomicBoolean watchAgain) {

        try {

            if (event == NodeWatchEvent.WHEN_CHILD_CHANGED) {

                if (!zkNodeManager.nodeExists(path)) {
                    throw new NodeExceptions.NodeWatchException("路径：" + path + "是不存在的");
                }

                zkClient.getChildren(path, watchedEvent -> {

                    Watcher.Event.EventType eventType = watchedEvent.getType();

                    if (eventType == Watcher.Event.EventType.NodeDeleted
                            || eventType == Watcher.Event.EventType.NodeChildrenChanged) {

                        if (watchAgain.get()) {
                            watch(event, handler, watchAgain);
                        }

                        Logger.info(
                                StringUtil.combineString(
                                        "节点：",
                                        path,
                                        " 有新的子节点被发现")
                        );

                        List<Node> childNodes = children();
                        ((ChildNodeCreationUpdateHandler) handler).process(childNodes);
                    }
                });

            }

            if (event == NodeWatchEvent.WHEN_NODE_CREATION || event == NodeWatchEvent.WHEN_NODE_DELETED) {

                zkClient.exists(path, watchedEvent -> {

                    Watcher.Event.EventType eventType = watchedEvent.getType();

                    if (eventType == Watcher.Event.EventType.NodeCreated) {

                        if (watchAgain.get()) {
                            watch(event, handler, watchAgain);
                        }

                        Logger.info(
                                StringUtil.combineString(
                                        "有节点：",
                                        path,
                                        " 被创建了"
                                )
                        );

                        ((NodeCreationUpdateHandler) handler).process(ZookeeperNode.this);
                    }

                    if (eventType == Watcher.Event.EventType.NodeDeleted) {

                        if (watchAgain.get()) {
                            watch(event, handler, watchAgain);
                        }

                        Logger.info(
                                StringUtil.combineString(
                                        "有节点：",
                                        path,
                                        " 被删除了"
                                )
                        );

                        ((NodeDeletedHandler) handler).process(ZookeeperNode.this.path());
                    }
                });
            }

            if (event == NodeWatchEvent.WHEN_NODE_UPDATED) {

                if (!zkNodeManager.nodeExists(path)) {
                    throw new NodeExceptions.NodeWatchException("路径：" + path + " 是不存在的");
                }

                zkClient.getData(path, watchedEvent -> {

                            if (watchedEvent.getType() == Watcher.Event.EventType.NodeDataChanged) {

                                if (watchAgain.get()) {
                                    watch(event, handler, watchAgain);
                                }

                                Logger.info(
                                        StringUtil.combineString(
                                                "有节点：",
                                                path,
                                                " 内容改变了"
                                        )
                                );

                                ((NodeCreationUpdateHandler) handler).process(ZookeeperNode.this);
                            }
                        },
                        zkClient.exists(path, false));
            }

        } catch (Throwable e) {

            throw new NodeExceptions.NodeWatchException(e);
        }
    }
}
