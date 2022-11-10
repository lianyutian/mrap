package com.lm.mrap.common.exceptions;

/**
 * @author liming
 * @version 1.0
 * @description: 异常操作的公共方法集合
 * @date 2022/10/28 上午10:31
 */
public class CommonException {

    public static String combineMessage(String msg, String suffix) {
        return new StringBuilder().append(msg).append(suffix).toString();
    }
}
