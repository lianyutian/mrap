package com.lm.mrap.logger;

/**
 * @author liming
 * @version 1.0
 * @description: 日志写入异常类
 * @date 2022/10/28 上午10:18
 */
public class LogExceptions {
    public static class WriteFaildeException extends RuntimeException {

        private static final String MESSAGE_SURFIX = "---写入器写入存储时出现错误";

        public WriteFaildeException(String message) {
            super(message + MESSAGE_SURFIX);
        }

        public WriteFaildeException(String message, Throwable throwable) {
            super(message + MESSAGE_SURFIX, throwable);
        }

        public WriteFaildeException(Throwable throwable) {
            super(throwable);
        }

    }
}
