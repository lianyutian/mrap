package com.lm.mrap.logger.config;

import com.lm.mrap.common.config.CommonConfig;
import com.lm.mrap.logger.LogSaveStrategy;

/**
 * @author liming
 * @version 1.0
 * @description: 日志相关配置类
 * @date 2022/10/27 下午4:57
 */
public class LoggerConfig {

    public static final String KAFKA_HOSTS = CommonConfig.getStringConfigOrElse("log.kafka.hosts", "localhost:9092");

    public static final long DEFAULT_QUEUE_WAIT = CommonConfig.getLongConfigOrElse("log.defaultQueueWait", 10L);

    public static final String DEFAULT_DATE_FORMAT = CommonConfig.getStringConfigOrElse("log.defaultDateFormat", "yyyyMMdd");

    public static final String DEFAULT_LOG_DATE_FORMAT = CommonConfig.getStringConfigOrElse("log.defaultDateFormat", "yyyyMMdd HH:mm:ss.SS");

    public static final String LOG_KAFKA_ACKS = CommonConfig.getStringConfigOrElse("log.kafka.acks", "all");

    public static final int LOG_KAFKA_TIMEOUT = CommonConfig.getIntegerConfigOrElse("log.kafka.timeout", 60000);

    public static final int LOG_KAFKA_BATCH = CommonConfig.getIntegerConfigOrElse("log.kafka.batchSize", 16384);

    public static final int LOG_KAFKA_TCPDELAY = CommonConfig.getIntegerConfigOrElse("log.kafka.tcpDelay", 1);

    public static final int LOG_KAFKA_BUFFER = CommonConfig.getIntegerConfigOrElse("log.kafka.bufferSize", 33554432);

    public static final int LOG_KAFKA_WAIT_DELTA = CommonConfig.getIntegerConfigOrElse("log.collector.writeInterval", 500);

    public static final int LOG_COLLECTOR_BUFFER = CommonConfig.getIntegerConfigOrElse("log.collector.buffer", 33554432);

    public static final int LOG_COLLECTOR_MAXBUFFER = CommonConfig.getIntegerConfigOrElse("log.collector.maxBuffer", 33554432 * 20);

    public static final int LOG_COLLECTOR_WRITE_INTERVAL = CommonConfig.getIntegerConfigOrElse("log.collector.writeInterval", 1000);

    public static final boolean LOG_DEBUG = CommonConfig.getBooleanConfigOrElse("log.enableDebug", false);

    public static final String LOG_NAME = CommonConfig.getStringConfigOrElse("log.name", "test");

    public static final LogSaveStrategy LOG_SAVE_STRATEGY = LogSaveStrategy.valueOf(
            CommonConfig.getStringConfigOrElse("log.logSaveStrategy", "ALLINONE")
    );

    public static final String LOG_NATIVE_PATH = CommonConfig.getStringConfigOrElse("log.nativePath", "/tmp/mraplogs");

    public static final int ERROR_MESSAGE_PRINT_COUNT = CommonConfig.getIntegerConfigOrElse("log.errorPrintCount", 10);
    public static final boolean KAFKA_AUTO_CREATE_TOPICS = true;

    public static final int KAFKA_NUM_PARTITIONS = 3;

    public static final int DEFAULT_REPLICATION_FACTOR = 3;

}
