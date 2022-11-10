package com.lm.mrap.sync.thread;

import com.lm.mrap.common.utils.SleepUitl;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.factory.HadoopConfigFactory;
import com.lm.mrap.sync.utils.HdfsDealUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.lm.mrap.sync.config.HdfsConfig.TABLE_IS_READED;
import static com.lm.mrap.sync.config.SyncConfig.MONITOR_INTERVAL;
import static com.lm.mrap.sync.config.SyncConsts.SYNC_NO_FLAG;

/**
 * @author liming
 * @version 1.0
 * @description: 监控 HDFS 变化线程
 * @date 2022/10/27 下午3:05
 */
@Slf4j
public class MonitorHdfsThread extends Thread {
    private static final int TABLE_INDEX = 0;

    private static final int READER_FLAG_INDEX = 1;

    private static volatile boolean IS_CONTINUE = true;

    private static Configuration configuration = HadoopConfigFactory.configuration;

    private static HdfsDealUtil hdfsDealUtil;

    private static List<String> monitorTables = new ArrayList<>();

    static {
        try {
            hdfsDealUtil = new HdfsDealUtil(configuration);
        } catch (IOException e) {
            Logger.error("MonitorHdfsThread IOException", e.getMessage());
        }
    }

    public MonitorHdfsThread(List<String> tables) {
        monitorTables = tables;
    }

    @Override
    public void run() {

        Logger.info("MonitorHdfsThread.run start monitor");

        while (IS_CONTINUE) {

            isReadFileOfTables(monitorTables);

            try {

                SleepUitl.minute(MONITOR_INTERVAL);

            } catch (InterruptedException e) {

                Logger.error(
                        "MonitorHdfsThread.run",
                        "InterruptedException",
                        e.getMessage()
                );
            }

        }
    }

    /**
     * 判断 hdfs 路径上表数据是否准备好且有权限访问
     *
     * @param tables
     */
    private static void isReadFileOfTables(List<String> tables) {

        try {

            for (String table : tables) {

                String tablePath = TABLE_IS_READED + table;

                BufferedReader fileBuffer = null;

                if (hdfsDealUtil.exists(tablePath) && hdfsDealUtil.isHasOtherAllPersion(tablePath)) {

                    fileBuffer = hdfsDealUtil.getBufferReader(tablePath);
                    String line = null;

                    while ((line = fileBuffer.readLine()) != null) {
                        splitLineAndPutQueue(line);
                    }
                } else {

                    Logger.info(
                            "MonitorHdfsThread.isReadFileOfTables",
                            "talbe: " + table,
                            "the data is not exits of table, or not null permission for table is read file, please write data to source file or set permission")
                    ;
                }
            }
        } catch (IOException e) {

            Logger.error(
                    "MonitorHdfsThread",
                    "IOException",
                    e.getMessage()
            );
        }

    }

    private static void splitLineAndPutQueue(String line) {

        List<String> tableNameAndReadFlag = StringUtil.strSplit(line, StringUtil.TABLE_SYMBOL);
        String tableReaded = tableNameAndReadFlag.get(READER_FLAG_INDEX);
        String tableName = "";

        if (tableReaded.equals(SYNC_NO_FLAG)) {

            try {

                tableName = tableNameAndReadFlag.get(TABLE_INDEX);
                SynchronizationHandler.addTableNameQueue(tableName);

            } catch (InterruptedException e) {

                Logger.error(
                        tableName,
                        "MonitorHdfsThread.splitLineAndPutQueue",
                        "add to queue, occcur"
                );
            }
        }
    }

    public static boolean deleteTableIsReadFile(String path) {

        try {

            if (hdfsDealUtil.exists(path)) {

                return hdfsDealUtil.noRecursiveDeleteFile(path);

            } else {
                Logger.info(
                        "MonitorHdfsThread.deleteTableIsReadFile",
                        path,
                        "the path not exits, it don't need to delete"
                );

                return true;
            }
        } catch (IOException e) {

            Logger.error(
                    "MonitorHdfsThread.deleteTableIsReadFile",
                    path,
                    e.getMessage()
            );
        }

        return false;
    }
}
