package com.lm.mrap.sync.thread.kvwriter;

import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.db.kv.config.HBaseConfig;
import com.lm.mrap.db.kv.impl.HBaseClient;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.thread.fileparser.KeyValue;
import org.apache.hadoop.hbase.client.Put;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/15 上午10:50
 */
public class HBaseWriter implements KVWriter {

    private final byte[] columnFamily = HBaseConfig.COLUMN_FAMILY.getBytes();

    private final String tableName;

    private final int batchSize;

    private final HBaseClient hBaseClient;

    private final List<Put> currentBatch;

    private int count = 0;

    private final byte[] columnName;

    private long lineCount = 0L;


    public HBaseWriter(String tableName, int batchSize, byte[] columnName) {
        this.tableName = tableName;
        this.batchSize = batchSize;
        this.columnName = columnName;

        try {

            this.hBaseClient = new HBaseClient(tableName);
            this.currentBatch = new ArrayList<>(batchSize);

        } catch (IOException e) {

            Logger.error(
                    "HBaseWriter， 初始化HBaseClient出现错误",
                    tableName,
                    StringUtil.exToString(e)
            );

            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(KeyValue keyValue) {

        Put put = new Put(keyValue.getKey());
        put.addColumn(
                columnFamily,
                columnName,
                keyValue.getValue()
        );

        currentBatch.add(put);
        count++;

        if (count >= batchSize) {
            commit();
        }

    }

    @Override
    public void commit() {

        if (currentBatch.isEmpty()) {
            return;
        }

        try {

            hBaseClient.putDataByBatch(currentBatch);
        } catch (IOException e) {

            Logger.error(
                    "HBaseWriter提交数据到HBase的时候出现了问题",
                    tableName,
                    StringUtil.exToString(e)
            );
        } finally {

            lineCount += count;
            currentBatch.clear();
            count = 0;
        }

    }

    @Override
    public void close() {
        commit();
    }

    @Override
    public void closeIgnore() {

    }

    @Override
    public long getCount() {
        return lineCount;
    }

    @Override
    public String tableName() {
        return tableName;
    }
}
