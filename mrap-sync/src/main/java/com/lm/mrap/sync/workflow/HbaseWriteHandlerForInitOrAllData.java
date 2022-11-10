package com.lm.mrap.sync.workflow;

import com.lm.mrap.common.utils.DateUtil;
import com.lm.mrap.db.kv.config.HBaseConfig;

import java.util.HashMap;

/**
 * @author liming
 * @version 1.0
 * @description: Hbase 表数据初始化或全量数据导入
 * @date 2022/11/7 下午4:32
 */
public class HbaseWriteHandlerForInitOrAllData {

    private static final boolean RESULT = false;

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

    private static HBaseClient

}
