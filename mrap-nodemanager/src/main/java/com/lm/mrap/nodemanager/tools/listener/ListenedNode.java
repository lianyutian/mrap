package com.lm.mrap.nodemanager.tools.listener;

import com.lm.mrap.common.exceptions.CommonException;
import com.lm.mrap.common.utils.StringUtil;

/**
 * 监听节点
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/1 13:54
 */
public class ListenedNode {

    private final String nodePath;
    private final short depth;

    public ListenedNode(String nodePath, short depth) {

//        CommonException.throwNotAllow(
//                StringUtil.la
//        );

        this.nodePath = nodePath;
        this.depth = depth;
    }
}
