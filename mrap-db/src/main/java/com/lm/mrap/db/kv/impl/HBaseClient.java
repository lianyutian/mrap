package com.lm.mrap.db.kv.impl;

import com.lm.mrap.common.utils.SleepUitl;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.db.kv.facotry.HBaseConnectionFactory;
import com.lm.mrap.db.utils.KeyUtil;
import com.lm.mrap.logger.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.BufferedMutatorParams;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.TableDescriptor;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;
import org.apache.hadoop.hbase.tool.BulkLoadHFilesTool;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import static com.lm.mrap.db.kv.config.HBaseConfig.BLOCK_CACHE_ENABLED;
import static com.lm.mrap.db.kv.config.HBaseConfig.BLOOM_FILTER_TYPE;
import static com.lm.mrap.db.kv.config.HBaseConfig.COLUMN_FAMILY;
import static com.lm.mrap.db.kv.config.HBaseConfig.MAX_VERSIONS;
import static com.lm.mrap.db.kv.config.HBaseConfig.THREAD_SLEEP;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/7 下午5:01
 */
public class HBaseClient {

    private static final Connection CONNECTION = HBaseConnectionFactory.getConnection();

    /**
     * 控制 HBase DDL 相关操作
     */
    private static Admin hbaseAdmin;

    /**
     * 控制 HBase DML 相关操作
     */
    private final Table table;

    private final String tableName;

    /**
     * 封装 HBase 表
     */
    private final TableName hTable;

    static {

        try {
            hbaseAdmin = CONNECTION.getAdmin();
        } catch (IOException e) {

            Logger.error(
                    "HBaseClient",
                    "IOException",
                    e.getMessage()
            );
        }
    }

    public HBaseClient(String tableName) throws IOException {

        this.tableName = tableName;

        this.hTable = TableName.valueOf(tableName.toUpperCase());

        this.table = CONNECTION.getTable(hTable);

    }

    /**
     * 判断表是否存在及是否启用
     *
     * @return 存在且启用
     * @throws IOException IOException
     */
    public boolean isExistsAndEnable() throws IOException {

        if (hbaseAdmin.tableExists(hTable)) {
            return hbaseAdmin.isTableEnabled(hTable);
        }

        return false;
    }

    /**
     * 创建表
     *
     * @param regionArray
     * @throws IOException
     * @throws InterruptedException
     */
    public void createHbaseTalbe(List<String> regionArray) throws IOException, InterruptedException {

        disableAndDeleteTalbe();

        TableDescriptorBuilder tableDescriptorBuilder = createAndAddHtableDescriptorColumnFamily();
        TableDescriptor tableDescriptor = tableDescriptorBuilder.build();

        byte[][] regions = getRegions(regionArray);

        hbaseAdmin.createTable(tableDescriptor, regions);
    }

    /**
     * 删除表
     *
     * @throws IOException IOException
     */
    private void disableAndDeleteTalbe() throws IOException {

        if (hbaseAdmin.tableExists(hTable)) {
            if (hbaseAdmin.isTableEnabled(hTable)) {
                hbaseAdmin.disableTable(hTable);
            }

            try {
                SleepUitl.threadSleep("100-1000");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            hbaseAdmin.deleteTable(hTable);
        }
    }

    private TableDescriptorBuilder createAndAddHtableDescriptorColumnFamily() {

        // 创建表描述建造者
        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(hTable);
        // 创建列描述建造者
        ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = createAndSetColumnFamilyDescriptor();
        tableDescriptorBuilder.setColumnFamily(columnFamilyDescriptorBuilder.build());

        return tableDescriptorBuilder;
    }

    private ColumnFamilyDescriptorBuilder createAndSetColumnFamilyDescriptor() {
        ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder(COLUMN_FAMILY.getBytes());
        // 设置创建表时的参数
        columnFamilyDescriptorBuilder.setMaxVersions(MAX_VERSIONS);
        columnFamilyDescriptorBuilder.setBlockCacheEnabled(BLOCK_CACHE_ENABLED);
        columnFamilyDescriptorBuilder.setValue("BLOOPMFILTER", BLOOM_FILTER_TYPE);

        return columnFamilyDescriptorBuilder;
    }

    private byte[][] getRegions(List<String> regionArray) {

        byte[][] regions = new byte[regionArray.size()][];

        TreeSet<byte[]> rows = new TreeSet<>(Bytes.BYTES_COMPARATOR);
        for (String region : regionArray) {
            rows.add(Bytes.toBytes(region));
        }

        return addRegionsItem(regions, rows.iterator());

    }

    private byte[][] addRegionsItem(byte[][] regions, Iterator<byte[]> regionRowIterator) {

        int regionsIndex = 0;

        while (regionRowIterator.hasNext()) {

            byte[] tempRow = regionRowIterator.next();
            regionRowIterator.remove();
            regions[regionsIndex] = tempRow;
            regionsIndex++;

        }

        return regions;
    }

    public void dropTables(List<String> tables) throws IOException {

        if (tables.isEmpty()) {

            Logger.error(
                    "HBaseClient.dropTables",
                    "list of table name is null"
            );
        }

        for (String tableName : tables) {
            disableAndDeleteTalbe(TableName.valueOf(tableName.toUpperCase()));
        }
    }

    private static void disableAndDeleteTalbe(TableName willDropTable) throws IOException {

        if (hbaseAdmin.tableExists(willDropTable)) {

            if (hbaseAdmin.isTableEnabled(willDropTable)) {
                hbaseAdmin.disableTable(willDropTable);
            }

            try {
                SleepUitl.threadSleep("100-1000");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hbaseAdmin.deleteTable(willDropTable);
        }
    }

    public HashMap<HashMap<String, String>, String> get(String key) throws IOException {

        Get getter = getter(KeyUtil.getPrimaryKey(key));

        return getMap(getter);
    }

    /**
     * 获取 rowkey 对应的所有版本数据
     *
     * @param rowKey rowkey
     * @return rowkey 对应的所有版本数据
     * @throws IOException IOException
     */
    public HashMap<HashMap<String, String>, String> getAllVersionsInfo(String rowKey) throws IOException {

        Get getter = getter(KeyUtil.getPrimaryKey(rowKey));

        getter.readAllVersions();

        return getMap(getter);
    }

    private Get getter(String rowKey) {
        Get get = new Get(Bytes.toBytes(rowKey));
        get.addFamily(Bytes.toBytes(COLUMN_FAMILY));

        return get;
    }

    private HashMap<HashMap<String, String>, String> getMap(Get getter) throws IOException {

        Result result = table.get(getter);

        if (!result.isEmpty()) {
            return parseValue(result.listCells());
        }

        return new HashMap<>();
    }

    private HashMap<HashMap<String, String>, String> parseValue(List<Cell> cells) {

        HashMap<HashMap<String, String>, String> hashMap = new HashMap<>();

        for (Cell cell : cells) {

            hashMap.put(
                    getQualifierTimestamp(cell),
                    Bytes.toString(getCellValue(cell))
            );
        }

        return hashMap;
    }

    private HashMap<String, String> getQualifierTimestamp(Cell cell) {

        HashMap<String, String> qualifierTimestamp = new HashMap<>();

        qualifierTimestamp.put(
                Bytes.toString(getCellQualifier(cell)),
                String.valueOf(cell.getTimestamp())
        );

        return qualifierTimestamp;
    }

    private byte[] getCellQualifier(Cell cell) {

        byte[] qualifier = new byte[cell.getQualifierLength()];

        System.arraycopy(
                cell.getQualifierArray(),
                cell.getQualifierOffset(),
                qualifier,
                0,
                cell.getQualifierLength()
        );

        return qualifier;
    }

    private byte[] getCellValue(Cell cell) {

        byte[] value = new byte[cell.getValueLength()];

        System.arraycopy(
                cell.getValueArray(),
                cell.getValueOffset(),
                value,
                0,
                cell.getValueLength()
        );

        return value;
    }

    public HashMap<HashMap<String, String>, String> get(String key, String qualifier) {

        try {

            Get getter = getter(KeyUtil.getPrimaryKey(key), qualifier);
            Result result = table.get(getter);

            if (!result.isEmpty()) {
                return parseValue(result.listCells());
            }
        } catch (IOException e) {

            Logger.error(
                    "HBaseClient.get",
                    "IOException",
                    "tableName: " + tableName,
                    "key: " + key,
                    "qualifier: " + qualifier,
                    e.getMessage()
            );
        }

        return new HashMap<>();
    }

    private Get getter(String rowKey, String qualifier) {

        Get get = new Get(Bytes.toBytes(rowKey));

        get.readAllVersions();

        get.addColumn(
                Bytes.toBytes(COLUMN_FAMILY),
                Bytes.toBytes(qualifier)
        );

        return get;
    }

    public boolean put(String key, HashMap<String, String> columnFamilyQualifierMap) throws IOException {

        table.put(putter(key, columnFamilyQualifierMap));

        return true;
    }

    private Put putter(String key, HashMap<String, String> qualifierValueMap) {

        Put putter = new Put(Bytes.toBytes(KeyUtil.getPrimaryKey(key)));

        qualifierValueMap.forEach((rowKey, qualifierValue) -> {
            putter.addColumn(
                    Bytes.toBytes(COLUMN_FAMILY),
                    Bytes.toBytes(rowKey),
                    Bytes.toBytes(qualifierValue)
            );
        });

        return putter;
    }

    public void put(BufferedReader bufferedReader) {

        try {

            String line;
            int count = 0;
            List<Put> puts = new ArrayList<>();

            BufferedMutatorParams bufferedMutatorParams = new BufferedMutatorParams(hTable);
            bufferedMutatorParams.writeBufferSize(8388608);

            BufferedMutator bufferedMutator = CONNECTION.getBufferedMutator(bufferedMutatorParams);

            while ((line = bufferedReader.readLine()) != null) {

                Put put = getPutItem(line);
                puts.add(put);
                count++;

                if (count % 1000 == 0) {
                    putDataByBatch(puts, bufferedMutator);
                }
            }

            putDataByBatch(puts, bufferedMutator);

        } catch (IOException e) {

            Logger.error(
                    "HBaseClient.put",
                    "IOException",
                    "tableName: " + tableName,
                    e.getMessage()
            );
        }
    }

    private Put getPutItem(String line) {

        String[] lineSplit = line.split(StringUtil.TABLE_SYMBOL);

        if (lineSplit.length != 4) {

            Logger.error(
                    "HBaseClient.getPutItem",
                    "Occur error",
                    "line.length" + line.length()
            );
        }

        Put put = new Put(Bytes.toBytes(lineSplit[0]));
        put.addColumn(
                Bytes.toBytes(lineSplit[1]),
                Bytes.toBytes(lineSplit[2]),
                Bytes.toBytes(lineSplit[3])
        );

        return put;
    }


    public void putDataByBatch(List<Put> puts) throws IOException {

        table.put(puts);
        puts.clear();

        try {
            SleepUitl.threadSleep(THREAD_SLEEP);
        } catch (InterruptedException e) {

            Logger.error(
                    "HBaseClient.putDataByBatch",
                    "InterruptedException",
                    "timeStr: " + THREAD_SLEEP,
                    e.getMessage()
            );
            e.printStackTrace();
        }
    }

    public void putDataByBatch(List<Put> puts, BufferedMutator bufferedMutator) throws IOException {

        bufferedMutator.mutate(puts);

        puts.clear();
        try {
            SleepUitl.threadSleep(THREAD_SLEEP);
        } catch (InterruptedException e) {

            Logger.error(
                    "HBaseClient.putDataByBatch",
                    "InterruptedException",
                    "timeStr: " + THREAD_SLEEP,
                    e.getMessage()
            );
            e.printStackTrace();
        }
    }

    public boolean deleteByRoeKey(String rowKey) throws IOException {

        Delete delete = new Delete(Bytes.toBytes(KeyUtil.getPrimaryKey(rowKey)));
        table.delete(delete);

        return true;
    }

    public void bulkLoad(String hFilePath, String uri) throws IOException {

        Configuration configuration = hbaseAdmin.getConfiguration();
        configuration.set("hadoop.tmp.dir", "/data/tmp/hadoop-" + System.getProperty("user.name"));
        configuration.setInt("hbase.mapreduce.bulkload.max.hfiles.perRegion.perFamily", 500000);

        BulkLoadHFilesTool bulkLoadHFilesTool = new BulkLoadHFilesTool(configuration);
        bulkLoadHFilesTool.bulkLoad(hTable, new Path(uri + "/" + hFilePath));

    }

    public void close() throws IOException {

        hbaseAdmin.close();
    }
}
