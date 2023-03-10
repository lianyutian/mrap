package com.lm.mrap.common.executor;

import org.omg.CORBA.TIMEOUT;

/**
 * 任务状态
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/6 11:23
 */
public enum TaskStatus {

    IDLE, // 任务还未提交到执行队列
    WAITING, // 未被执行正在等待
    RUNNING, // 任务正在执行
    TIMEOUT, // 任务超时
    FAILED, // 任务执行发生重大错误，不可恢复的错误
    SUCCESS // 任务执行成功
}
