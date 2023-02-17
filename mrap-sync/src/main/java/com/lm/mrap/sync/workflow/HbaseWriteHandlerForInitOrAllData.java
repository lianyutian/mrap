package com.lm.mrap.sync.workflow;

import com.lm.mrap.common.utils.DateUtil;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.db.kv.config.HBaseConfTable;
import com.lm.mrap.db.kv.config.HBaseConfTableType;
import com.lm.mrap.db.kv.config.HBaseConfig;
import com.lm.mrap.db.kv.impl.HBaseClient;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.utils.HdfsDealUtil;
import com.lm.mrap.sync.utils.MapUtil;
import com.lm.mrap.sync.utils.SeqUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.lm.mrap.db.kv.config.ZookeeperConfig.ZK_PARENT_NODE_PATH;
import static com.lm.mrap.sync.config.HdfsConfig.*;
import static com.lm.mrap.sync.config.SyncConsts.XML_CONF_KEY;

/**
 * @author liming
 * @version 1.0
 * @description: Hbase 表数据初始化或全量数据导入
 * @date 2022/11/7 下午4:32
 */
public class HbaseWriteHandlerForInitOrAllData {

    private static boolean result = false;

    private static final HashMap<String, String> QUALIFIER_VALUE = new HashMap<>();

    private static final String CURRENT_DATE = DateUtil.getCurrentDay("yyyy-MM-dd HH:mm:ss:sss");

    private static final String OLD_TABLE = HBaseConfig.EXCHANGE_TABLE_QUAILIFIERS[0];

    private static final String ACTIVE_TABLE = HBaseConfig.EXCHANGE_TABLE_QUAILIFIERS[1];

    private static final String TABLE_NAME_STR = HBaseConfig.HIS_INFO_TABLE_QUAILIFIERS[0];

    private static final String WRITE_TIME_STR = HBaseConfig.HIS_INFO_TABLE_QUAILIFIERS[1];

    private static final String MODEL_NAME_STR = HBaseConfig.HIS_INFO_TABLE_QUAILIFIERS[2];

    private static final String WRITE_METHOD_INIT_STR = "Init";

    private static final String WRITE_METHOD_ALL_STR = "All";

    private static final String WRITE_METHOD_APPEND_BULKLOAD_STR = "AppendBulkload";

    private static final int TABLE_REGISTER_INFO_LENGTH = 6;

    private static HBaseClient HIS_TABLE_CLIENT;

    private static HBaseClient EXCHANGE_TABLE_CLIENT;

    static {

        try {

            HIS_TABLE_CLIENT = new HBaseClient(HBaseConfig.HIS_INFO_TABLE);
            EXCHANGE_TABLE_CLIENT = new HBaseClient(HBaseConfig.EXCHANGE_TABLE);
        } catch (IOException e) {

            Logger.error(
                    "HbaseWriteHandlerForInitOrAllData",
                    "Occur error, while init HBaseClient",
                    "IOException",
                    e.getMessage()
            );
        }
    }

    public static boolean handler(ConcurrentHashMap<String, String> xmlConfig, HdfsDealUtil hdfsDealUtil, String tableName,
                                  String uuid) {

        String hFilePath = TABLE_DATA_PATH + tableName + HFILE_PATH;
        String hFileDataFilePath = hFilePath + DATA_FILE_PATH;
        String regionPath = HBASE_REGION_INFO_PATH + tableName;
        String currentTable = tableName;
        String writeMethod = xmlConfig.get(XML_CONF_KEY[8]);
        String clientIps = xmlConfig.get(XML_CONF_KEY[4]);

        if (WRITE_METHOD_ALL_STR.equals(writeMethod)) {

            String currentDay = DateUtil.getCurrentDay("yyyyMMddHHmm");
            currentTable = tableName + "_" + currentDay;

        }

        HBaseClient hBaseClient;

        try {

            hBaseClient = new HBaseClient(currentTable);

            if (!WRITE_METHOD_APPEND_BULKLOAD_STR.equals(writeMethod)) {
                if (hdfsDealUtil.exists(regionPath) && hdfsDealUtil.isHasOtherAllPersion(regionPath)) {

                    List<String> regionBoundary = SeqUtil.hdfsFileToList(regionPath, hdfsDealUtil);
                    hBaseClient.createHbaseTalbe(regionBoundary);

                    if (hBaseClient.isExistsAndEnable()) {

                        Logger.info(
                                uuid,
                                tableName,
                                "HbaseWriteHandlerForInitOrAllData.handler",
                                "It is successful that create a table"
                        );
                    } else {

                        Logger.error(
                                uuid,
                                tableName,
                                "HbaseWriteHandlerForInitOrAllData.handler",
                                "It is failed that create a table. because table is not exists or enabled"
                        );

                        return false;

                    }

                } else {

                    Logger.error(
                            uuid,
                            tableName,
                            "the file is not exist",
                            "region boundary info path: " +  regionPath
                    );

                    return false;
                }
            }

            Logger.info(
                    uuid,
                    tableName,
                    "HbaseWriteHandlerForInitOrAllData.handler",
                    "bulkload start"
            );

            hBaseClient.bulkLoad(hFileDataFilePath, hdfsDealUtil.getUri().toString());

            Logger.info(
                    uuid,
                    tableName,
                    "HbaseWriteHandlerForInitOrAllData.handler",
                    "current table： "+ currentTable,
                    "bulkoad end"
            );

            switch (writeMethod) {

                case WRITE_METHOD_INIT_STR:

                    if (!initProcess(tableName, uuid)) {
                        return false;
                    }
                    break;

                case WRITE_METHOD_ALL_STR:

                    if (!allProcess(tableName, currentTable, hBaseClient, uuid)) {
                        return false;
                    }

                    break;

                default:
                    break;
            }

            String modelNameOfTable = xmlConfig.get(MODEL_NAME_STR);
            if (!updateHisTableInfo(tableName, currentTable, modelNameOfTable)) {
                return false;
            }
        } catch (IOException e) {

            Logger.error(
                    uuid,
                    tableName,
                    "HbaseWriteHandlerForInitOrAllData.handler",
                    "IOException",
                    e.getMessage()
            );
        } catch (Exception e) {

            Logger.error(
                    uuid,
                    tableName,
                    "HBaseClient.bulkload",
                    "IOException",
                    "tableName: " + tableName,
                    "uri: " + hdfsDealUtil.getUri().toString(),
                    "hFileDataFilePath: " + hFileDataFilePath,
                    e.toString()
            );

            return false;
        }

        if (writeNodeData(tableName, currentTable, writeMethod, clientIps)) {
            result = true;
        }

        return result;
    }


    /**
     * 初始化表
     *
     * @param tableName 表名称
     * @param uuid uuid
     * @return 是否初始化成功
     */
    private static boolean initProcess(String tableName, String uuid) throws IOException {

        Logger.info(
                uuid,
                tableName,
                "HbaseWriteHandlerForInitOrAllData.initProcess",
                "drop history table of business",
                "start"
        );

        if (HIS_TABLE_CLIENT.deleteByRoeKey(tableName)) {

            Logger.info(
                    uuid,
                    tableName,
                    "HbaseWriteHandlerForInitOrAllData.initProcess",
                    "exchange table info",
                    "start"
            );

            return EXCHANGE_TABLE_CLIENT.put(
                    tableName,
                    MapUtil.updateByQualifier(
                            tableName,
                            new HashMap<>(),
                            new HBaseConfTable(HBaseConfTableType.EXCHANGE_TABLE)
                    )
            );
        }

        return false;
    }

    private static boolean allProcess(String tableName, String currentTable, HBaseClient hBaseClient, String uuid) throws IOException {

        HashMap<String, String> hashMap = getHashMap(tableName);

        HashMap<HashMap<String, String>, String> historyTableInfo = getHistoryTableInfo(HIS_TABLE_CLIENT, tableName);

        List<String> hisTables = getHisTable(historyTableInfo, hashMap);

        if (hisTables.size() > 0) {

            dropHisTable(hBaseClient, hisTables);

        } else {

            Logger.info(
                    uuid,
                    tableName,
                    "HbaseWriteHandlerForInitOrAllData.allProcess",
                    "table name: " + tableName,
                    "it is don't need to drop history table",
                    "the qty of history table: " + 0
            );
        }

        HashMap<String, String> willUpdateExchangeTableMap = MapUtil.updateByQualifier(
                currentTable,
                hashMap,
                new HBaseConfTable(HBaseConfTableType.EXCHANGE_TABLE)
        );

        if (EXCHANGE_TABLE_CLIENT.put(tableName, willUpdateExchangeTableMap)) {
            return true;
        }

        return false;
    }

    private static void removeInfo(HashMap<String, String> hashMap, HashSet<String> hisHashSet, String removeTable) {

        if (hashMap.containsKey(removeTable)) {

            hisHashSet.remove(hashMap.get(removeTable));
        }
    }

    /**
     * 获取 EXCHANGE_TABLE_INFO 表中对应taleName的当前表和上一个版本表数据
     *
     * @param tableName 表名
     * @return 字段map
     * @throws IOException IOException
     */
    private static HashMap<String, String> getHashMap(String tableName) throws IOException {

        HashMap<HashMap<String, String>, String> exchangeTableQualifierValue =
                getExchangeTalbeInfo(EXCHANGE_TABLE_CLIENT, tableName);

        HashMap<String, String> hashMap = new HashMap<>();

        exchangeTableQualifierValue.forEach((qualifierNameMap, value) -> {
            qualifierNameMap.forEach((qualifierName, time) -> {
                hashMap.put(qualifierName, value);
            });
        });

        return hashMap;
    }

    /**
     * 获取 HIS_INFO_TABLE 表中对应 tableName 所有历史版本
     * @param hisTableClient
     * @param tableName
     * @return
     * @throws IOException
     */
    private static HashMap<HashMap<String, String>, String> getHistoryTableInfo(HBaseClient hisTableClient, String tableName) throws IOException {

        return hisTableClient.getAllVersionsInfo(tableName);
    }

    private static HashMap<HashMap<String, String>, String> getExchangeTalbeInfo(HBaseClient exchangeTableClient, String tableName) throws IOException {
        return exchangeTableClient.get(tableName);
    }

    private static List<String> getHisTable(HashMap<HashMap<String, String>, String> historyTableInfo, HashMap<String, String> hashMap) {
        HashSet<String> hisHashSet = new HashSet<>();
        historyTableInfo.forEach((qualifierNameMap, value) -> {
            qualifierNameMap.forEach((qualifierName, time) -> {
                if (TABLE_NAME_STR.equals(qualifierName)) {
                    hisHashSet.add(value);
                }
            });
        });

        // 保留表上一个版本
        removeInfo(hashMap, hisHashSet, OLD_TABLE);
        // 保留表当前版本
        removeInfo(hashMap, hisHashSet, ACTIVE_TABLE);

        return new ArrayList<>(hisHashSet);
    }


    private static void dropHisTable(HBaseClient hBaseClient, List<String> hisTables) throws IOException {

        if (hisTables.size() > 0) {
            hBaseClient.dropTables(hisTables);
        }
    }

    private static boolean updateHisTableInfo(String tableName, String currentTable, String modelNameOfTable) throws IOException {

        QUALIFIER_VALUE.put(TABLE_NAME_STR, currentTable);
        QUALIFIER_VALUE.put(WRITE_TIME_STR, CURRENT_DATE);
        QUALIFIER_VALUE.put(MODEL_NAME_STR, modelNameOfTable);

        if (HIS_TABLE_CLIENT.put(tableName, QUALIFIER_VALUE)) {
            QUALIFIER_VALUE.clear();
            return true;
        }

        return false;
    }

    private static boolean writeNodeData(String tableName, String currentTable, String writeMethod, String clientIps) {

        boolean isWriteDone = false;

        String tableNodePath = ZK_PARENT_NODE_PATH + StringUtil.SLASH_STRING + tableName;

        String registerInfo;

        return result;

    }

}
