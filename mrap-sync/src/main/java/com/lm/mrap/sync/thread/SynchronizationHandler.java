package com.lm.mrap.sync.thread;

import com.lm.mrap.common.utils.DateUtil;
import com.lm.mrap.common.utils.SleepUitl;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.factory.DOMBuilder;
import com.lm.mrap.sync.factory.HadoopConfigFactory;
import com.lm.mrap.sync.utils.DOMNodeKeyValue;
import com.lm.mrap.sync.utils.DOMTreeModel;
import com.lm.mrap.sync.utils.HdfsDealUtil;
import com.lm.mrap.sync.workflow.WriteDataProcess;
import org.apache.hadoop.conf.Configuration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.lm.mrap.sync.config.DOMConfig.REMOVE_KEYS;
import static com.lm.mrap.sync.config.HdfsConfig.TABLE_DATA_PATH;
import static com.lm.mrap.sync.config.HdfsConfig.TABLE_IS_READED;
import static com.lm.mrap.sync.config.HdfsConfig.XML_FILE;
import static com.lm.mrap.sync.config.SyncConfig.CORE_POOL_SIZE;
import static com.lm.mrap.sync.config.SyncConfig.HANDLER_INTERVAL;

/**
 * @author liming
 * @version 1.0
 * @description: 数据同步任务处理类
 * @date 2022/10/20 上午10:56
 */
public class SynchronizationHandler implements Runnable {
    /**
     * 需要同步表队列
     */
    private static final LinkedBlockingQueue<String> TABLE_NAMES_SYNC_QUEUE = new LinkedBlockingQueue<>();

    private static final Configuration CONFIGURATION = HadoopConfigFactory.configuration;

    private static final boolean IS_CONTINUE = true;

    /**
     * 表同步任务并行度
     */
    private static final ArrayBlockingQueue<String> TABLE_SYNC_TASK_QUEUE = new ArrayBlockingQueue<>(CORE_POOL_SIZE * 2);

    private static HdfsDealUtil hdfsDealUtils;

    static {
        try {
            hdfsDealUtils = new HdfsDealUtil(CONFIGURATION);
        } catch (IOException e) {
            Logger.error(
                    "SynchronizationHandler",
                    " IOException",
                    "init hdfsDealUtils",
                    e.getMessage()
            );
        }
    }

    @Override
    public void run() {
        while (IS_CONTINUE) {

            String tableName = "";
            String uuid = "";
            String tablePath = "";
            long startTime = 0;

            try {
                // 处理每个任务时间间隔
                SleepUitl.second(HANDLER_INTERVAL);

                if (TABLE_NAMES_SYNC_QUEUE.size() == 0) {
                    continue;
                }

                tableName = TABLE_NAMES_SYNC_QUEUE.poll();

                if (tableName != null && TABLE_SYNC_TASK_QUEUE.offer(tableName)) {

                    uuid = DateUtil.getCurrentDay("yyyyMMdd") + "_" + (int) (1 + Math.random() * 10000);

                    ConcurrentHashMap<DOMNodeKeyValue, String> xmlConfig = parseXMLConfig(tableName, uuid);

                    startTime = System.currentTimeMillis();

                    tablePath = TABLE_IS_READED + tableName;

                    if (xmlConfig == null) {
                        TABLE_SYNC_TASK_QUEUE.remove(tableName);

                        deleteTableIsEsists(
                                uuid,
                                tableName,
                                tablePath,
                                startTime,
                                "the xml config is null"
                        );

                        return;
                    }

                    syncTable(tableName, uuid, tablePath, startTime, xmlConfig);
                }

            } catch (Exception e) {

                TABLE_SYNC_TASK_QUEUE.remove(tableName);

                if (!StringUtil.EMPTY_STRING.equals(tablePath)) {

                    deleteTableIsEsists(
                            uuid,
                            tableName,
                            tablePath,
                            startTime,
                            e.getMessage()
                    );
                }

                Logger.error(
                        "SynchronizationHandler.run",
                        StringUtil.exToString(e)
                );
            }
        }
    }

    /**
     * 处理表同步
     *
     * @param tableName 表名
     * @param uuid      uuid
     * @param tablePath 表路径
     * @param startTime 处理时间
     * @param xmlConfig xml配置
     * @throws IOException IOException
     */
    private void syncTable(String tableName, String uuid, String tablePath, long startTime, ConcurrentHashMap<DOMNodeKeyValue, String> xmlConfig) throws IOException {
        Enumeration<DOMNodeKeyValue> keys = xmlConfig.keys();
        // eg: <table_name,test_table>
        ConcurrentHashMap<String, String> writeConfig = getWriteConfig(keys);

        WriteDataProcess writeDataProcess = new WriteDataProcess();
        // 开始执行数据写入处理
        if (writeDataProcess.handler(writeConfig, uuid)) {

            deleteTableIsEsists(
                    uuid,
                    tableName,
                    tablePath,
                    startTime,
                    "It is complement that deal finish of data channel"
            );

        } else {

            if (!StringUtil.EMPTY_STRING.equals(tablePath)) {

                deleteTableIsEsists(

                        uuid,
                        tableName,
                        tablePath,
                        startTime,
                        "It is failed that of writing task"
                );
            }
        }

        TABLE_SYNC_TASK_QUEUE.remove(tableName);
    }

    private static ConcurrentHashMap<String, String> getWriteConfig(Enumeration<DOMNodeKeyValue> keys) {

        ConcurrentHashMap<String, String> writeConfig = new ConcurrentHashMap<>();

        while (keys.hasMoreElements()) {

            DOMNodeKeyValue domNodeKeyValue = keys.nextElement();
            String nodeKey = domNodeKeyValue.getNodeKey();

            if (!nodeKey.equals(REMOVE_KEYS[0]) || !nodeKey.equals(REMOVE_KEYS[1])) {
                writeConfig.put(nodeKey, domNodeKeyValue.getNodeValue());
            }
        }

        return writeConfig;
    }

    /**
     * 解析xml配置文件
     *
     * @param tableName 表名
     * @param uuid      uuid
     * @return 解析结果 eg: <<table_name,test_table>,hbase_table_conf>
     * @throws IOException                  IOException
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws SAXException                 SAXException
     */
    private ConcurrentHashMap<DOMNodeKeyValue, String> parseXMLConfig(String tableName, String uuid)
            throws IOException, ParserConfigurationException, SAXException {

        ConcurrentHashMap<DOMNodeKeyValue, String> xmlConfig = null;

        DOMBuilder domBuilder = new DOMBuilder();

        String sparkConfigXmlPath = TABLE_DATA_PATH + tableName + XML_FILE;
        if (hdfsDealUtils.exists(sparkConfigXmlPath) && hdfsDealUtils.isHasOtherAllPersion(sparkConfigXmlPath)) {
            Document document = domBuilder
                    .getDocumentBuilder()
                    .parse(
                            hdfsDealUtils
                                    .getFileInputStream(sparkConfigXmlPath)
                    );

            xmlConfig = new DOMTreeModel(document).foreachForDOM();

        } else {
            if (tableName != null) {
                Logger.error(uuid, tableName,
                        "SynchronizationHandler.parseXMLConfig",
                        "path: " + sparkConfigXmlPath + "is not exists, or not full permission, please check path");
            }
        }

        return xmlConfig;
    }

    /**
     * 删除表同步文件
     *
     * @param uuid      uuid
     * @param tableName 表名
     * @param tablePath 表路径
     * @param startTime 删除时间
     * @param message   删除原因
     */
    private void deleteTableIsEsists(String uuid, String tableName, String tablePath, long startTime, String message) {

        if (MonitorHdfsThread.deleteTableIsReadFile(tablePath)) {

            Logger.info(
                    uuid,
                    tableName,
                    "SynchronizationHandler.deleteTableIsEsists",
                    "It is success that drop file, table is read file path: " + tablePath,
                    message,
                    String.format(
                            "all cost: (%.2f) minutes",
                            (System.currentTimeMillis() - startTime) / (1000 * 60f)
                    )
            );
        }
    }

    /**
     * 判断表是否需要加入同步队列
     *
     * @param tableName 表名
     * @throws InterruptedException InterruptedException
     */
    public static synchronized void addTableNameQueue(String tableName) throws InterruptedException {

        if (TABLE_NAMES_SYNC_QUEUE.contains(tableName)) {

            Logger.info(
                    tableName,
                    "SynchronizationHandler.addTableNameQueue",
                    "table is exist, it not necessary to add to queue"
            );

        } else {

            if (TABLE_SYNC_TASK_QUEUE.contains(tableName)) {

                Logger.info(
                        tableName,
                        "SynchronizationHandler.addTableNameQueue",
                        "This table is currently being synchronized and not added to the task");

            } else {

                TABLE_NAMES_SYNC_QUEUE.put(tableName);
                Logger.info(
                        tableName,
                        "SynchronizationHandler.addTableNameQueue",
                        "add a task"
                );

            }
        }

    }
}
