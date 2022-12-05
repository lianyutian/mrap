package com.lm.mrap.sync.thread.fileparser;

import com.lm.mrap.common.utils.SleepUitl;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.thread.filereader.FileReaderThread;
import io.netty.buffer.ByteBuf;
import lombok.extern.java.Log;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author liming
 * @version 1.0
 * @description: 文件解析线程
 * @date 2022/11/17 上午10:52
 */
public class FileParserThread extends Thread {

    public static final BatchData PARSER_THREAD_END_FLAG = new BatchData(null);

    private final LinkedBlockingQueue<BatchData> batchDataQueue;

    private final int batchSize;

    private final FileSystem fileSystem;

    private final FileReaderThread fileReaderThread;

    private final FileParserType fileParserType;

    private volatile boolean isQuit = false;

    private long count = 0;

    public FileParserThread(LinkedBlockingQueue<BatchData> batchDataQueue,
                            int batchSize,
                            FileSystem fileSystem,
                            FileReaderThread fileReaderThread,
                            FileParserType fileParserType) {

        this.batchDataQueue = batchDataQueue;
        this.batchSize = batchSize;
        this.fileSystem = fileSystem;
        this.fileReaderThread = fileReaderThread;
        this.fileParserType = fileParserType;
    }

    @Override
    public void run() {

        while (!isQuit) {

            // 从数据队列中拿出未解析的数据
            ByteBuf data = fileReaderThread.pollData();

            if (data == FileReaderThread.END_FLAG) {

                Logger.info(
                        "FileParserThread关闭"
                );
                break;
            }

            if (data == null || !data.isReadable()) {

                try {

                    SleepUitl.milliSeconds(100);
                    continue;
                } catch (InterruptedException e) {

                    Logger.error(
                            "FileParserThread等待队列数据时被打断",
                            StringUtil.exToString(e)
                    );
                    break;
                }
            }

            Parser parser = null;

            try {

                parser = fileParserType.getParser(
                        fileSystem,
                        data,
                        batchSize
                );

                BatchData batchData;

                while ((batchData = parser.nextBatch()) != null) {

                    while (!batchDataQueue.offer(batchData)) {
                        SleepUitl.milliSeconds(100);
                    }
                }

                count += parser.getCount();
            } catch (Exception e) {

                Logger.error(
                        "解析文件内容时候出现错误",
                        StringUtil.exToString(e)
                );
                break;
            } finally {

                try {
                    if (parser != null) {
                        parser.clean();
                    }
                } catch (IOException e) {

                    Logger.error(
                            "清除Parser临时数据时出现异常",
                            StringUtil.exToString(e)
                    );
                }
            }
        }

        isQuit = true;
    }

    public boolean isTerminal() {
        return isQuit;
    }

    public long getCount() {
        return count;
    }
}

