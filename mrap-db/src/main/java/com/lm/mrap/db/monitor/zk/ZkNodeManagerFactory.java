package com.lm.mrap.db.monitor.zk;

import com.lm.mrap.logger.Logger;
import com.lm.mrap.nodemanager.NodeManager;
import com.lm.mrap.nodemanager.NodeManagerConfig;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: zk 节点管理工厂类
 * @date 2022/11/11 下午4:36
 */
public class ZkNodeManagerFactory {
    public static NodeManager nodeManager;

    static {

        try {
            nodeManager = NodeManager.createNodeManager(
                    NodeManagerConfig.NODE_MANAGER_HOSTS,
                    NodeManagerConfig.NODE_MANAGER_SESSION_TIMEOUT,
                    NodeManagerConfig.NODE_MANAGER_CONNECT_TIMEOUT
            );
        } catch (IOException e) {
            e.printStackTrace();

            Logger.error(
                    "ZkNodeManagerFactory init",
                    "IOException",
                    e.getMessage()
            );
        } catch (InterruptedException e) {
            e.printStackTrace();

            Logger.error(
                    "ZkNodeManagerFactory init",
                    "InterruptedException",
                    e.getMessage()
            );
        }

    }


}
