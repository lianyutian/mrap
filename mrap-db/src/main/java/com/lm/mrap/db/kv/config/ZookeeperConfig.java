package com.lm.mrap.db.kv.config;

import com.lm.mrap.common.config.CommonConfig;

/**
 * @author liming
 * @version 1.0
 * @description: zk 节点配置类
 * @date 2022/11/11 下午4:20
 */
public class ZookeeperConfig {

    public static final String ZK_PARENT_NODE_PATH = CommonConfig.getStringConfigOrElse("sync.zookeeper.parent_noe_path", "/Ada/table");
}
