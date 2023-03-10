package com.lm.mrap.nodemanager;

import com.lm.mrap.common.config.CommonConfig;

/**
 * 节点配置
 *
 * @author liming
 * @version 1.0
 * @since 2023/2/23 16:46
 */
public class NodeManagerConfig {

    public static final String NODE_MANAGER_HOSTS = CommonConfig.getStringConfigOrElse("nodemanager.hosts", "localhost:2181");

    public static final int NODE_MANAGER_SESSION_TIMEOUT = CommonConfig.getIntegerConfigOrElse("nodemanager.sessionTimeout", 6000);

    public static final int NODE_MANAGER_CONNECT_TIMEOUT = CommonConfig.getIntegerConfigOrElse("nodemanager.connecTimeout", 6000);

    public static final String NODE_ROOT_PATH = CommonConfig.getStringConfigOrElse("nodemanager.rootPath", "/mrap");

    public static final short NODE_LISTENER_DEPTH = CommonConfig.getIntegerConfigOrElse("nodemanager.listenDepth", 2).shortValue();

    public static final int NODE_LISTENER_WAIT_INTERVVAL = CommonConfig.getIntegerConfigOrElse("nodemanager.listener.interval", 1);

    public static final long NODE_LOCK_WAIT_INTERVAL = CommonConfig.getLongConfigOrElse("nodemanager.nodeLock.interval", 1000L);

}
