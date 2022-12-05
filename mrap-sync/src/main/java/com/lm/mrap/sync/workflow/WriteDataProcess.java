package com.lm.mrap.sync.workflow;

import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.thread.filereader.ReaderType;
import com.lm.mrap.sync.utils.HadoopConfiguration;
import com.lm.mrap.sync.utils.HdfsDealUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.lm.mrap.sync.config.HdfsConfig.DATA_FILE_PATH;
import static com.lm.mrap.sync.config.HdfsConfig.HFILE_PATH;
import static com.lm.mrap.sync.config.HdfsConfig.TABLE_DATA_PATH;
import static com.lm.mrap.sync.config.SyncConsts.SYNC_WRITE_METHOD;
import static com.lm.mrap.sync.config.SyncConsts.WRITE_METHOD_INIT_STR;
import static com.lm.mrap.sync.config.SyncConsts.XML_CONF_KEY;

/**
 * @author liming
 * @version 1.0
 * @description: 数据写入进程
 * @date 2022/11/4 下午1:48
 */
public class WriteDataProcess {
    private static final int COLUMN_NAME_INDEX = 1;

    private static final int REDIS_IP_PORT_INDEX = 4;

    private static final int IS_SLOT_UPDATE_INDEX = 5;

    private static final int TABLE_REGISTER_INFO_LENGTH = 6;

    private static final int WRITE_METHOD_INDEX = 8;

    private static final int WRITE_ROLE_INDEX = 9;

    private static final String WRITE_ROLE_MIXED = "Redis/Hbase";

    private static final String WRITE_ROLE_HBASE = "Hbase";

    private static final String WRITE_ROLE_REDIS = "Redis";

    private HdfsDealUtil hdfsDealUtil;

    public WriteDataProcess() throws IOException {
        HadoopConfiguration hadoopConfiguration = new HadoopConfiguration();
        hdfsDealUtil = hadoopConfiguration.getHdfsDealUtil();
    }

    public boolean handler(ConcurrentHashMap<String, String> xmlConfig, String uuid) throws IOException {

        boolean result = false;

        String columnName = xmlConfig.get(XML_CONF_KEY[COLUMN_NAME_INDEX]);
        //String redisIps = xmlConfig.get(XML_CONF_KEY[REDIS_IP_PORT_INDEX]);
        String tableName = xmlConfig.get(XML_CONF_KEY[IS_SLOT_UPDATE_INDEX]);
        String writeMethod = xmlConfig.get(XML_CONF_KEY[WRITE_METHOD_INDEX]);
        int writeRole = Integer.parseInt(xmlConfig.get(XML_CONF_KEY[WRITE_ROLE_INDEX]));

        String hFilePath = TABLE_DATA_PATH + tableName + HFILE_PATH;
        // HFile数据文件路径
        String hFileDataFilePath = hFilePath + DATA_FILE_PATH + "/data";

        //List<String> redisIpPorts = StringUtil.strSplit(redisIps, StringUtil.COMMA_SYMBOL);
        //String redisInfoPath = REDIS_INFO_PATH + tableName;

        // HFile数据文件路径集合
        List<String> hFileDataFilePathList = hdfsDealUtil.getPaths(hFileDataFilePath);

        if (hFileDataFilePathList.size() == 0) {

            Logger.info(
                    uuid,
                    tableName,
                    "WriteDataProcess",
                    "result: " + result,
                    "path: " + hFileDataFilePath + " is empty, please check it"
            );

            return result;
        }

        boolean isUpdate = SYNC_WRITE_METHOD[0].equals(writeMethod);
        boolean isInit = WRITE_METHOD_INIT_STR.equals(writeMethod);

        long startTime = System.currentTimeMillis();

        if (writeRole == 2) {

            Logger.info(
                    "A_S",
                    uuid,
                    tableName,
                    WRITE_ROLE_HBASE,
                    String.valueOf(startTime),
                    "Sync",
                    "write role: " + writeRole
            );

            result = WriteProcess.writeHbase(
                    tableName,
                    columnName,
                    hdfsDealUtil,
                    hFileDataFilePathList,
                    ReaderType.HDFS,
                    isUpdate,
                    xmlConfig,
                    uuid
            );
        }

        Logger.info(
                uuid,
                tableName,
                "WriteDataProcess",
                "result: " + result,
                "write data to redis or hbase"
        );

        return result;
    }
}
