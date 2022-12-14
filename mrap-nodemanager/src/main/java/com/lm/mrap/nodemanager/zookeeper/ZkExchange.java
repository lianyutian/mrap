package com.lm.mrap.nodemanager.zookeeper;

import com.lm.mrap.nodemanager.NodeLifecycleType;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import java.util.ArrayList;
import java.util.List;

/**
 * 转换zk节点的类型为自定义节点类型
 */
public class ZkExchange {

    public static CreateMode exchangeCreateMode(NodeLifecycleType nodeLifecycleType) {

        switch (nodeLifecycleType) {

            case PERSIST:
                return CreateMode.PERSISTENT;
            case TEMPORARY:
                return CreateMode.EPHEMERAL;
            case PERSISI_SEQUEUE:
                return CreateMode.PERSISTENT_SEQUENTIAL;
            case TEMPORARY_SEQUEUE:
                return CreateMode.EPHEMERAL_SEQUENTIAL;
            default:
                throw new IllegalArgumentException("zookeeper转换节点生命周期时没有这个类型");
        }
    }

    /**
     * 获取默认的zookeeper节点的权限，
     * 默认权限为拥有所有权限，将所有操作全由操作者控制
     */
    public static final List<ACL> DEFAULT_ACL = new ArrayList<>();

    static {

        DEFAULT_ACL.add(
                new ACL(
                        ZooDefs.Perms.ALL,
                        new Id("world", "anyone")
                )
        );
    }

}
