package com.lm.mrap.db.monitor.zk;

import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.nodemanager.Node;
import com.lm.mrap.nodemanager.NodeLifecycleType;
import com.lm.mrap.nodemanager.NodeManager;

import java.util.List;

/**
 * @author liming
 * @version 1.0
 * @description: zk 节点操作工具类
 * @date 2022/11/11 下午4:32
 */
public class ZkNodeUtil {

    private static NodeManager nodeManager = ZkNodeManagerFactory.nodeManager;

    public static String concatPath(String parentPath, String childPath) {

        return parentPath +
                StringUtil.SLASH_STRING +
                childPath;
    }

    public static boolean writeNodeData(String path, String data) {

        createZKPathIfNotExists(path);

        Node node = getNode(path);

        // 判断数据内容不重复
        if (!node.readAsString().equals(data)) {
            node.write(data);
        }

        return true;
    }

    public static String readNodeData(String path) {

        String res = "";

        if (nodeManager.nodeExists(path)) {

            Node node = getNode(path);
            String tmp = node.readAsString();

            if (!(StringUtil.EMPTY_STRING).equals(tmp)) {

                res = tmp;
            }
        }

        return res;
    }



    public static void createZKPathIfNotExists(String path) {

        List<String> pathSplit = StringUtil.strSplit(path, StringUtil.SLASH_STRING);

        String tmp = StringUtil.EMPTY_STRING;

        for (int index = 1; index < pathSplit.size(); index++) {

            String currentPath = ZkNodeUtil.concatPath(tmp, pathSplit.get(index));

            if (!currentPath.equals(StringUtil.EMPTY_STRING)
                    && !nodeManager.nodeExists(currentPath)) {

                nodeManager.createNode(currentPath, NodeLifecycleType.PERSIST);
            }

            tmp = currentPath;

        }
    }

    public static void createTemporaryZKPathIfNotExists(String path) {

        List<String> pathSplit = StringUtil.strSplit(path, StringUtil.SLASH_STRING);

        String tmp = StringUtil.EMPTY_STRING;

        for (int index = 1; index < pathSplit.size(); index++) {

            String currentPath = ZkNodeUtil.concatPath(tmp, pathSplit.get(index));

            if (!currentPath.equals(StringUtil.EMPTY_STRING)
                    && !nodeManager.nodeExists(currentPath)) {

                nodeManager.createNode(currentPath, NodeLifecycleType.TEMPORARY);
            }

            tmp = currentPath;

        }
    }

    public static Node getNode(String path) {

        return nodeManager.openNode(path);
    }

    public static void deleteNode(String path) {

        nodeManager.deleteNode(path);
    }

    public static boolean isExists(String path) {

        return nodeManager.nodeExists(path);
    }

}
