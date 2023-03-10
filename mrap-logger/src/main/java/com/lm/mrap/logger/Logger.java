package com.lm.mrap.logger;

import com.lm.mrap.common.config.CommonConfig;
import com.lm.mrap.logger.config.LoggerConfig;
import com.lm.mrap.logger.sender.impl.KafkaLoggerSender;
import com.lm.mrap.logger.sender.impl.NativeLoggerSender;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lm.mrap.logger.config.LoggerConfig.*;

/**
 * @author liming
 * @version 1.0
 * @description: 日志写入入口，所有需要写入日志的系统都可以通过这个类来写入日志
 * @date 2022/10/27 下午3:14
 */
public class Logger {

    private static LoggerCollector collector = null;

    private static final String INFO = "INFO";

    private static final String ERROR = "ERROR";

    private static final String DEBUG = "DEBUG";

    private static final String WARN = "WARN";

    private static final String UNKNOW = "UNKNOW";

    private static AtomicInteger LOG_ERROR_FLAG = new AtomicInteger(0);

    public static void init(String name) throws IOException {

        collector = new LoggerCollector(
                LOG_COLLECTOR_BUFFER,
                LOG_COLLECTOR_MAXBUFFER,
                LOG_COLLECTOR_WRITE_INTERVAL,
                new KafkaLoggerSender(
                        name,
                        LOG_SAVE_STRATEGY
                ),
                new NativeLoggerSender(
                        LOG_NATIVE_PATH,
                        name,
                        LOG_SAVE_STRATEGY
                )
        );

        collector.start();
    }

    public static void info(String item, String... items) {
        log(INFO, item, items);
    }

    public static void error(String item, String... items) {
        log(ERROR, item, items);
    }

    public static void debug(String item, String... items) {
        if (LOG_DEBUG) {
            log(DEBUG, item, items);
        }
    }

    public static void warn(String item, String... items) {
        log(WARN, item, items);
    }

    public static void log(String mark, String item, String... items) {

        try {
            ByteBuf cache = LoggerFormat.format(
                    mark == null ? UNKNOW : mark,
                    item == null ? StringUtil.EMPTY_STRING : item,
                    items
            );

            if (collector == null) {
                System.out.println(cache.toString(CommonConfig.DEFAULT_CHARSET));
            } else {
                collector.send(cache);
            }
        } catch (Exception e) {

            if (LOG_ERROR_FLAG.incrementAndGet() < ERROR_MESSAGE_PRINT_COUNT) {
                e.printStackTrace();
            }
        }
    }

}

