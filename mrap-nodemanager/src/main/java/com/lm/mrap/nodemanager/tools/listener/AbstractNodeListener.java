package com.lm.mrap.nodemanager.tools.listener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 节点监控器，指定一系列的节点，监控其本身以及其子节点，
 * 当有节点上线或者下线时会主动通知特定事件，进行处理，抽象类
 * 节点管理器对于事件的触发是随机的并且是并发的，这样对于使用监听器
 * 实现具体应用的提升了不少并发处理难度，而且对于节点监视功能来说不需要太强的性能，
 * 所以我们将所有的事件收集到队列中，将并发问题串行化，简化了监控器的编程模型
 * 监控路径：是被监控的节点的路径
 * 监控深度，一个从0开始的数字，表示监控路径的深度，比如路径 /rootpath，如果监控深度为0，
 * 则只监控 /rootpath 节点，如果监控深度是1，表示除了监控 /rootpath，还会监控
 * /rootpath 的子节点--- /rootpath/*，监控深度是2表示监控 /rootpath 的子节点
 * 的子节点，以此类推，监控深度不能特别长，我们默认限制深度不能大于2，如果需要可以修改
 * 配置{@link com.lm.mrap.nodemanager.NodeManagerConfig#NODE_LISTENER_DEPTH}
 * 初始化时，可以为每个被监控路径指定不同的深度监控
 * <p>
 * 监控事件： 节点被创建时触发--- NODE_CREATE
 * 节点被删除时触发--- NODE_DELETE
 * 节点那日容改变时触发--- NODE_DATA_CHANGE
 * 有新的子节点时触发---CHILD_CHANGE
 * 有子节点被删除时触发--- CHILD_DELETE
 * 不同的监控事件有不用的处理方法，应用需要根据不同需要重写不同方法
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/1 10:39
 */
public abstract class AbstractNodeListener {

    /**
     * 在节点路径上，每个节点都会有一个相对位置，这个位置就是自己在这个路径上的深度
     * 监听器监听一个节点还需要知道这个节点的监控深度，所以每个节点必须维护一个自己的深度信息
     * 于是创建这个类来标识这两种信息
     */
    protected static class NodeDepath {
        // 当前所处的深度位置
        private final short currentDepth;
        // 这个节点需要监控的深度
        private final short depth;

        public NodeDepath(short currentDepth, short depth) {
            this.currentDepth = currentDepth;
            this.depth = depth;
        }

        public short getCurrentDepth() {
            return currentDepth;
        }

        public short getDepth() {
            return depth;
        }
    }

    // 被监听节点的节点名
    protected final Map<String, NodeDepath> listenNodesMap = new ConcurrentHashMap<>();

    protected final Map<String, List<AtomicBoolean>> listenNodeStatusMap = new ConcurrentHashMap<>();

}
