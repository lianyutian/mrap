package com.lm.mrap.sync.workflow;

import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.config.SyncConsts;
import com.lm.mrap.sync.thread.filereader.ReaderType;
import com.lm.mrap.sync.utils.HadoopConfiguration;
import com.lm.mrap.sync.utils.HdfsDealUtil;
import com.lm.mrap.sync.utils.SeqUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import static com.lm.mrap.sync.config.HdfsConfig.*;
import static com.lm.mrap.sync.config.SyncConsts.*;

/**
 * @author liming
 * @version 1.0
 * @description: 数据写入进程
 * @date 2022/11/4 下午1:48
 */
public class WriteDataProcess {
    private static final int TABLE_REGISTER_INFO_LENGTH = 6;

    private static final int REDIS_IP_PORT_INDEX = 4;

    private static final int IS_SLOT_UPDATE_INDEX = 5;

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

        int writeRole = Integer.parseInt(xmlConfig.get(XML_CONF_KEY[9]));
        String tableName = xmlConfig.get(XML_CONF_KEY[5]);
        String columnName = xmlConfig.get(XML_CONF_KEY[1]);

        String hFilePath = TABLE_DATA_PATH + tableName + HFILE_PATH;
        String hFileDataFilePath = hFilePath + DATA_FILE_PATH + "/data";

        String redisIps = xmlConfig.get(XML_CONF_KEY[REDIS_IP_PORT_INDEX]);
        List<String> redisIpPorts = StringUtil.strSplit(redisIps, StringUtil.COMMA_SYMBOL);

        String writeMethod = xmlConfig.get(XML_CONF_KEY[8]);

        String redisInfoPath = REDIS_INFO_PATH + tableName;

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

            result = WriteDataProcess.writeHbase(
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




    }

    private static boolean writeHbase(String tableName, String columnName, HdfsDealUtil hdfsDealUtil, List<String> hFileDataFilePathList, ReaderType hdfs, boolean isUpdate, ConcurrentHashMap<String, String> xmlConfig, String uuid) {

        try {
            if (!isUpdate) {
                HbaseWriteHandlerForInitOrAllData
            }
        }
    }
}
