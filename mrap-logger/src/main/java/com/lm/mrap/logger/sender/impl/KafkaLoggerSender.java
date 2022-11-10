package com.lm.mrap.logger.sender.impl;

import com.lm.mrap.logger.LogExceptions;
import com.lm.mrap.logger.LogSaveStrategy;
import com.lm.mrap.logger.config.LoggerConfig;
import com.lm.mrap.logger.sender.LoggerSender;
import io.netty.buffer.ByteBuf;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author liming
 * @version 1.0
 * @description: 将日志发送给 kafka
 * @date 2022/10/27 下午4:49
 */
public class KafkaLoggerSender extends Thread implements LoggerSender {

    private static final Properties PROPES = new Properties();

    static {
        //PROPES.put("bootstrap.servers", LoggerConfig.KAFKA_HOSTS);
        PROPES.put("bootstrap.servers", "192.168.1.130:9092,192.168.1.131:9092,192.168.1.132:9092");
        PROPES.put("acks", LoggerConfig.LOG_KAFKA_ACKS);
        PROPES.put("delivery.timeout.ms", LoggerConfig.LOG_KAFKA_TIMEOUT);
        PROPES.put("batch.size", LoggerConfig.LOG_KAFKA_BATCH);
        PROPES.put("linger.ms", LoggerConfig.LOG_KAFKA_TCPDELAY);
        PROPES.put("buffer.memory", LoggerConfig.LOG_KAFKA_BUFFER);
        PROPES.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        PROPES.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        PROPES.put("retries", 1);
    }

    private final Producer<byte[], byte[]> producer = new KafkaProducer<>(
            PROPES,
            new ByteArraySerializer(),
            new ByteArraySerializer()
    );

    private final String name;

    private final LogSaveStrategy logSaveStrategy;

    private String newName;

    private volatile boolean isQuit = false;

    private final LinkedBlockingQueue<List<ByteBuf>> dataQueue = new LinkedBlockingQueue<>(1);

    public KafkaLoggerSender(String name, LogSaveStrategy logSaveStrategy) {
        this.name = name;
        this.logSaveStrategy = logSaveStrategy;
        this.newName = logSaveStrategy.getStrategyExchange().newName(name);
        start();
    }

    @Override
    public void run() {

        while (!isQuit) {

            List<ByteBuf> datas = null;

            try {
                datas = dataQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (datas != null) {

                if (logSaveStrategy.getStrategyExchange().isChanged()) {

                    newName = logSaveStrategy.getStrategyExchange().newName(name);

                }

                try {

                    for (ByteBuf data : datas) {

                        producer.send(
                                new ProducerRecord<>(
                                        newName,
                                        getBytes(data)

                        ));
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] getBytes(ByteBuf buf) {

        if (buf.hasArray()) {
            return buf.array();
        } else {
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);

            return data;
        }
    }

    @Override
    public void write(List<ByteBuf> datas) {

        boolean offered = false;

        try {
            offered = dataQueue.offer(
                    datas,
                    LoggerConfig.LOG_COLLECTOR_WRITE_INTERVAL,
                    TimeUnit.MILLISECONDS
            );
        } catch (Throwable e) {
            throw new LogExceptions.WriteFaildeException("Kafka 队列等待时被打断");
        }

        if (!offered) {
            throw new LogExceptions.WriteFaildeException("Kafka 写入超时");
        }
    }

    @Override
    public void close() {

        producer.flush();
        producer.close();
    }
}
