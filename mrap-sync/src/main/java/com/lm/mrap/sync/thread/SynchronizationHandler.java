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
import com.lm.sync.utils.*;
import lombok.extern.java.Log;
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
import static com.lm.mrap.sync.config.HdfsConfig.*;
import static com.lm.mrap.sync.config.SyncConfig.CORE_POOL_SIZE;

/**
 * @author liming
 * @version 1.0
 * @description: 数据同步任务处理类
 * @date 2022/10/20 上午10:56
 */
public class SynchronizationHandler implements Runnable {
    private static final LinkedBlockingQueue<String> TABLE_NAMES = new LinkedBlockingQueue<>();

    private static final Configuration CONFIGURATION = HadoopConfigFactory.configuration;

    private static final boolean IS_CONTINUE = true;

    /**
     * 任务并行度
     */
    private static final ArrayBlockingQueue<String> CALCULATE_TASK = new ArrayBlockingQueue<>(CORE_POOL_SIZE * 2);

    private static HdfsDealUtil hdfsDealUtils;

    static {
        try {
            hdfsDealUtils = new HdfsDealUtil(CONFIGURATION);
        } catch (IOException e) {
            Logger.error("SynchronizationHandler: IOException init hdfsDealUtils {}", e.getMessage());
        }
    }

    @Override
    public void run() {
        while (IS_CONTINUE) {
            String tableName = "";
            String  uuid = "";
            String  tablePath = "";
            long startTime = 0L;

            try {
                SleepUitl.second(10);

                if (TABLE_NAMES.size() > 0) {
                    tableName = TABLE_NAMES.poll();

                    if (tableName != null && CALCULATE_TASK.offer(tableName)) {

                        uuid = DateUtil.getCurrentDay("yyyyMMdd") + "_" + (int) (1 + Math.random() * 10000);

                        ConcurrentHashMap<DOMNodeKeyValue, String> xmlConfig = parseXMLConfig(tableName, uuid);

                        startTime = System.currentTimeMillis();

                        tablePath = TABLE_IS_READED + tableName;

                        if (xmlConfig == null) {
                            CALCULATE_TASK.remove(tableName);

                            deleteTableIsEsists(
                                    uuid,
                                    tableName,
                                    tablePath,
                                    startTime,
                                    "the xml config is null"
                            );

                            return;
                        }

                        Enumeration<DOMNodeKeyValue> keys = xmlConfig.keys();
                        ConcurrentHashMap<String, String> writeConfig = getWriteConfig(keys);

                        WriteDataProcess writeDataProcess = new WriteDataProcess();
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

                        CALCULATE_TASK.remove(tableName);

                    }
                }


            } catch (InterruptedException | IOException | ParserConfigurationException | SAXException e) {
                throw new RuntimeException(e);
            }
        }
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
     * @param uuid uuid
     * @return 解析结果
     * @throws IOException IOException
     * @throws ParserConfigurationException ParserConfigurationException
     * @throws SAXException SAXException
     */
    private ConcurrentHashMap<DOMNodeKeyValue, String> parseXMLConfig(String tableName, String uuid)
            throws IOException, ParserConfigurationException, SAXException {

        ConcurrentHashMap<DOMNodeKeyValue, String> xmlConfig = null;

        DOMBuilder domBuilder = new DOMBuilder();

        String sparkConfigXmlPath = TABLE_DATA_PATH + tableName + XML_FILE;
        if (hdfsDealUtils.exists(sparkConfigXmlPath) && hdfsDealUtils.isHasOtherAllPersion(sparkConfigXmlPath)) {
            Document document = domBuilder
                    .getDocumentBuilder()
                    .parse(hdfsDealUtils
                            .getFileInputStream(sparkConfigXmlPath));

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

    public static synchronized void addTableNameQueue(String tableName) throws InterruptedException {

        if (TABLE_NAMES.contains(tableName)) {

            Logger.info(
                    tableName,
                    "SynchronizationHandler.addTableNameQueue",
                    "table is exist, it not necessary to add to queue"
            );

        } else {

            if (CALCULATE_TASK.contains(tableName)) {

                Logger.info(
                        tableName,
                        "SynchronizationHandler.addTableNameQueue",
                        "This table is currently being synchronized and not added to the task");

            } else {

                TABLE_NAMES.put(tableName);
                Logger.info(
                        tableName,
                        "SynchronizationHandler.addTableNameQueue",
                        "add a task"
                );

            }
        }

    }
}
