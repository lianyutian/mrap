package com.lm.mrap.common.exceptions;

/**
 * 有些对象必须满足一定的形式才能合法地使用。当对象不合法时，必须抛出这个异常
 *
 * @author liming
 * @version 1.0
 * @since 2023/2/28 10:58
 */
public class NotAllowException extends RuntimeException {

    private final static String COMMON = " --- 是不允许的";

    public NotAllowException(String message) {
        super(
                CommonException.combineMessage(message, COMMON)
        );
    }

    public NotAllowException(String message, Throwable throwable) {
        super(
                CommonException.combineMessage(message, COMMON),
                throwable
        );
    }

    public NotAllowException(Throwable throwable) {
        super(throwable);
    }
}
