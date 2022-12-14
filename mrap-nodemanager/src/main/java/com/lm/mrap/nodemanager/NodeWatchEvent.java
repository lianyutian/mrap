package com.lm.mrap.nodemanager;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/14 下午2:47
 */
public abstract class NodeWatchEvent<H> {

    public static NodeWatchEvent<ChildNodeCreationUpdateHandler> WHEN_CHILD_CHANGED = new NodeWatchEvent<ChildNodeCreationUpdateHandler>(){};

    public static NodeWatchEvent<NodeCreationUpdateHandler> WHEN_NODE_CREATION = new NodeWatchEvent<NodeCreationUpdateHandler>() {
    };

    public static NodeWatchEvent<NodeCreationUpdateHandler> WHEN_NODE_UPDATED = new NodeWatchEvent<NodeCreationUpdateHandler>() {
    };

    public static NodeWatchEvent<NodeDeletedHandler> WHEN_NODE_DELETED = new NodeWatchEvent<NodeDeletedHandler>() {
    };
}
