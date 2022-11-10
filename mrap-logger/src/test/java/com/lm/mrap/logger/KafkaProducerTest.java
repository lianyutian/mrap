package com.lm.mrap.logger;

import com.lm.mrap.logger.config.LoggerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;

import java.util.Properties;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/3 下午7:52
 */
public class KafkaProducerTest {
    private static final Properties PROPES = new Properties();

    static {
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

    private final Producer<String, String> producer = new KafkaProducer<>(
            PROPES,
            new StringSerializer(),
            new StringSerializer()
    );

    @Test
    public void testSend() {
        ProducerRecord<String, String> record = new ProducerRecord<>("test_log1", "hello kafka");
        producer.send(record);
    }
}
