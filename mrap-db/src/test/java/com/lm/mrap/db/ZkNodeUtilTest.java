package com.lm.mrap.db;

import com.lm.mrap.db.monitor.zk.ZkNodeUtil;
import org.junit.Test;

/**
 * @author liming
 * @version 1.0
 * @description: zk 节点操作工具类
 * @date 2022/11/11 下午4:32
 */
public class ZkNodeUtilTest {



    @Test
    public void testWriteNodeData() {
        String path = "";
        ZkNodeUtil.writeNodeData(path, "/sync/table/test_table");
    }

}
