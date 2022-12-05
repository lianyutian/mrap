package com.lm.mrap.sync.main;

import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.thread.MonitorHdfsThread;
import com.lm.mrap.sync.thread.SynchronizationHandler;
import com.lm.mrap.sync.utils.SeqUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.lm.mrap.sync.config.SyncConfig.CORE_POOL_SIZE;
import static com.lm.mrap.sync.config.SyncConfig.MODEL_NAME;

/**
 * @author liming
 * @version 1.0
 * @description: 数据同步入口类
 * @date 2022/10/11 下午5:20
 */
public class DataSynchronization {

    public static void main(String[] args) throws Exception {

        Logger.init(MODEL_NAME);

        List<Future<?>> synchronizationTasks = new ArrayList<>();

        //SynchronizationHandler synchronizationHandler = new SynchronizationHandler();
        SynchronizationHandler synchronizationHandler = (SynchronizationHandler) Class
                .forName("com.lm.mrap.sync.thread.SynchronizationHandler")
                .getConstructor()
                .newInstance();

        ExecutorService pool = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

        for (int i = 1; i <= CORE_POOL_SIZE; i++) {
            synchronizationTasks.add(pool.submit(synchronizationHandler));
        }

        // 监控线程 默认监控hdfs上 /mrap-hdfs/devsup/table_is_readed/ 路径下是否有需要同步的表文件
        MonitorHdfsThread monitorHdfsThread = new MonitorHdfsThread(SeqUtil.fileToList());
        monitorHdfsThread.start();

        for (Future<?> task : synchronizationTasks) {
            task.get();
        }
    }
}
