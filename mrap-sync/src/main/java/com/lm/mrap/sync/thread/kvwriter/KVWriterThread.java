package com.lm.mrap.sync.thread.kvwriter;

import com.lm.mrap.common.utils.SleepUitl;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.thread.fileparser.BatchData;
import com.lm.mrap.sync.thread.fileparser.FileParserThread;
import com.lm.mrap.sync.thread.fileparser.KeyValue;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/17 下午4:07
 */
public class KVWriterThread extends Thread {

    private final LinkedBlockingQueue<BatchData> dataQueue;

    private final KVWriter writer;

    private volatile boolean isQuit = false;

    public KVWriterThread(LinkedBlockingQueue<BatchData> dataQueue, KVWriter writer) {

        this.dataQueue = dataQueue;
        this.writer = writer;
    }

    @Override
    public void run() {

        while (!isQuit) {

            BatchData batchData = dataQueue.poll();

            if (batchData == FileParserThread.PARSER_THREAD_END_FLAG) {

                Logger.info(
                        "KVWriterThread关闭",
                        writer.tableName()
                );
                break;
            }

            if (batchData == null) {

                try {

                    SleepUitl.milliSeconds(100);
                    continue;
                } catch (InterruptedException e) {

                    Logger.error(
                            "KVWriterThread在等待队列数据时被打断",
                            writer.tableName(),
                            StringUtil.exToString(e)
                    );
                    break;
                }
            }

            try {

                while (batchData.hasNext()) {

                    KeyValue keyValue = batchData.next();
                    writer.add(keyValue);
                }

                writer.commit();
            } catch (Exception e) {

                Logger.error(
                        "KVWriterThread写入数据的时候出现异常",
                        writer.tableName(),
                        StringUtil.exToString(e)
                );

                if (isInterrupted()) {
                    break;
                }
            }
        }

        if (!isInterrupted()) {

            try {

                writer.close();
            } catch (Exception e) {

                Logger.error(
                        "KVWriterThread关闭，清理writer连接",
                        StringUtil.exToString(e)
                );
                writer.closeIgnore();
            }
        }

        isQuit = true;
    }

    public boolean isTerminal() {
        return isQuit;
    }

    public long getCount() {
        return writer.getCount();
    }

    public void terminal() {
        writer.closeIgnore();
    }
}
