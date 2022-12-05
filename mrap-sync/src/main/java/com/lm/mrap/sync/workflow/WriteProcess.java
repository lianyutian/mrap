package com.lm.mrap.sync.workflow;

import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.config.SyncConfig;
import com.lm.mrap.sync.config.SyncConsts;
import com.lm.mrap.sync.thread.fileparser.BatchData;
import com.lm.mrap.sync.thread.fileparser.FileParserThread;
import com.lm.mrap.sync.thread.fileparser.FileParserType;
import com.lm.mrap.sync.thread.fileparser.MemoryFileSystem;
import com.lm.mrap.sync.thread.filereader.FileReaderThread;
import com.lm.mrap.sync.thread.filereader.Reader;
import com.lm.mrap.sync.thread.filereader.ReaderType;
import com.lm.mrap.sync.thread.kvwriter.HBaseWriter;
import com.lm.mrap.sync.thread.kvwriter.KVWriter;
import com.lm.mrap.sync.thread.kvwriter.KVWriterThread;
import com.lm.mrap.sync.utils.HdfsDealUtil;

import java.io.FileReader;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author liming
 * @version 1.0
 * @description: 写进程
 * @date 2022/11/4 下午5:21
 */
public class WriteProcess {

    public interface WriterCreator {
        KVWriter create();
    }

    private static void commonWrite(
            List<String> filePaths,
            Reader reader,
            int writeThreadNum,
            String tableName,
            WriterCreator writer,
            String uuid
    ) throws Exception {

        CountDownLatch lock = new CountDownLatch(1);

        FileReaderThread fileReaderThread = new FileReaderThread(
                filePaths,
                reader,
                SyncConfig.FILE_READER_MAX_FILE,
                SyncConfig.FILE_READER_MAX_BYTES_SIZE,
                SyncConfig.PARSER_THREAD_NUM
        );

        fileReaderThread.start();

        final LinkedBlockingQueue<BatchData> batchDataQueue = new LinkedBlockingQueue<>(SyncConfig.BATCH_QUEUE_LIMIT);

        MemoryFileSystem memoryFileSystem = new MemoryFileSystem();

        final FileParserThread[] parserThreads = new FileParserThread[SyncConfig.PARSER_THREAD_NUM];

        for (int i = 0; i < SyncConfig.PARSER_THREAD_NUM; i++) {

            FileParserThread parserThread = new FileParserThread(
                    batchDataQueue,
                    SyncConfig.BATCH_SIZE,
                    memoryFileSystem,
                    fileReaderThread,
                    FileParserType.HFILE
            );

            parserThread.start();

            parserThreads[i] = parserThread;
        }

        final KVWriterThread[] writerThrads = new KVWriterThread[writeThreadNum];

        for (int i = 0; i < writeThreadNum; i++) {

            KVWriterThread writerThread = new KVWriterThread(
                    batchDataQueue,
                    writer.create()
            );

            writerThread.start();

            writerThrads[i] = writerThread;
        }

        SyncConsts.TIMER.newTimeout(
                timeout -> {

                    boolean isAllQuit = true;

                    for (FileParserThread parserThread : parserThreads) {
                        if (!parserThread.isTerminal()) {
                            isAllQuit = false;
                        }

                        if (!isAllQuit) {
                            break;
                        }
                    }

                    if (isAllQuit) {

                        for (int i = 0; i < writeThreadNum; i++) {
                            batchDataQueue.offer(FileParserThread.PARSER_THREAD_END_FLAG);
                        }

                        long parserLine = 0;
                        for (FileParserThread parserThread : parserThreads) {
                            parserLine += parserThread.getCount();
                        }

                        Logger.info(
                                "FileParserThread一共读取",
                                parserLine + "行",
                                tableName
                        );
                    }

                    isAllQuit = true;

                    for (KVWriterThread writerThrad : writerThrads) {

                        if (!writerThrad.isTerminal()) {
                            isAllQuit = false;
                        }

                        if (!isAllQuit) {
                            break;
                        }
                    }

                    if (!isAllQuit) {

                        timeout.timer().newTimeout(
                                timeout.task(),
                                1,
                                TimeUnit.SECONDS
                        );
                    } else {

                        long count = 0;
                        for (KVWriterThread kvWriterThread : writerThrads) {
                            count += kvWriterThread.getCount();
                        }

                        Logger.info(
                                uuid,
                                "写入数据任务全部完成",
                                tableName,
                                count + "行被写入",
                                tableName
                        );

                        lock.countDown();
                    }
                },
                1,
                TimeUnit.SECONDS
        );

        lock.await(SyncConfig.WRITER_TASK_WAIT, TimeUnit.SECONDS);

        if (lock.getCount() == 1) {

            fileReaderThread.interrupt();

            for (FileParserThread parserThread : parserThreads) {
                parserThread.interrupt();
            }

            for (KVWriterThread writerThrad : writerThrads) {
                writerThrad.interrupt();
                writerThrad.terminal();
            }

            throw new TimeoutException("任务执行超时");
        }
    }


    public static boolean writeHbase(String tableName,
                                     String columnName,
                                     HdfsDealUtil hdfsDealUtil,
                                     List<String> filePaths,
                                     ReaderType type,
                                     boolean isUpdate,
                                     ConcurrentHashMap<String, String> xmlConfig,
                                     String uuid) {

        try {
            if (!isUpdate) {

                HbaseWriteHandlerForInitOrAllData.handler(
                        xmlConfig,
                        hdfsDealUtil,
                        tableName,
                        uuid
                );
            } else {

                WriterCreator hbaseWriter = () -> new HBaseWriter(
                        tableName,
                        SyncConfig.HBASE_WRITE_BATCH_SIZE,
                        columnName.getBytes()
                );

                commonWrite(
                        filePaths,
                        type.reader(),
                        SyncConfig.HBASE_WRITER_THREAD,
                        tableName,
                        hbaseWriter,
                        uuid
                );
            }

            return true;
        } catch (Exception e) {

            Logger.error(
                    uuid,
                    "WriteHBase",
                    tableName,
                    StringUtil.exToString(e)
            );
        }

        return false;
    }
}
