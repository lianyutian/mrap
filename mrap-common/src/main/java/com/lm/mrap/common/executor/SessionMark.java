package com.lm.mrap.common.executor;

/**
 * 标识任务会话的类型
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/6 11:22
 */
public enum SessionMark {

    /**
     * 正常情况的会话
     */
    NORMAL,
    /**
     * 重试情况的会话
     */
    RETRY;
}
