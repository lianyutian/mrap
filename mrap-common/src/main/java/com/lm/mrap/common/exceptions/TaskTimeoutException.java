package com.lm.mrap.common.exceptions;

/**
 * 任务超时异常
 *
 * @author liming
 * @version 1.0
 * @since 2023/3/6 10:56
 */
public class TaskTimeoutException extends RuntimeException {

    // 异常信息后缀
    private final static String COMMON = " --- 任务超时";

    public TaskTimeoutException(String message) {
        super(
                CommonException.combineMessage(message, COMMON)
        );
    }

    public TaskTimeoutException(String message, Throwable cause) {
        super(
                CommonException.combineMessage(message, COMMON),
                cause
        );
    }

    public TaskTimeoutException(Throwable cause) {
        super(cause);
    }
}
