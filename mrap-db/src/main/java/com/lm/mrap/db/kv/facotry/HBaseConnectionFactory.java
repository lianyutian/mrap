package com.lm.mrap.db.kv.facotry;

import com.lm.mrap.logger.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: HBase Connection 工厂类
 * @date 2022/11/7 下午5:03
 */
public class HBaseConnectionFactory {

    private static final Configuration CONFIGURATION;

    private static final Connection CONNECTION;

    static {

        try {

            CONFIGURATION = HBaseConfiguration.create();
            CONNECTION = ConnectionFactory.createConnection(CONFIGURATION);
        } catch (IOException e) {

            Logger.error(
                    "HBaseConnectionFactory",
                    "IOException",
                    e.getMessage()
            );

            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() {
        return CONNECTION;
    }

    public static void close() {

        try {
            CONNECTION.close();
        } catch (IOException e) {

            Logger.error(
                    "HBaseConnectionFactory",
                    "IOException",
                    e.getMessage()
            );
        }
    }
}
