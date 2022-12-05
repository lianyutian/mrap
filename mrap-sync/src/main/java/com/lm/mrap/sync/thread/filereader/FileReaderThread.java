package com.lm.mrap.sync.thread.filereader;

import com.lm.mrap.common.exceptions.CommonException;
import com.lm.mrap.common.utils.SleepUitl;
import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.EmptyArrays;
import lombok.extern.java.Log;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liming
 * @version 1.0
 * @description: 文件读取线程，将需要写入 hbase 和 redis 的数据文件读取进内存，
 * 但是堆内存大小有限，进程每次能够处理的数据量也是有限的，我们对这个量进行了限制，
 * 限制有两个方面：
 * 第一：同时在内存中未被处理的文件个数
 * 第二：同时存在内存中为被处理的文件大小
 * 这两个限制满足其一就停止读取文件，等待内存中的数据已经被处理到这两个限制的范围内则继续开始读取文件
 * 读入内存中的文件会被存放入一个被规定了容量的阻塞队列中，等待文件分析线程来消费
 * @date 2022/11/15 下午2:04
 */
public class FileReaderThread extends Thread {

    public static final ByteBuf END_FLAG = Unpooled.wrappedBuffer(EmptyArrays.EMPTY_BYTES);

    /**
     * 本次需要读取的文件列表
     */
    private final List<String> fileList;

    /**
     * 本次读取文件的方式
     */
    private final Reader reader;

    /**
     * 内存中限制最大未被处理的文件个数
     */
    private final int MAX_FILE_SIZE;

    /**
     * 内存中限制最大未被处理的文件总大小
     */
    private final long MAX_DATA_SIZE;

    /**
     * 文件分析线程个数
     */
    private final int PARSER_THREAD_SIZE;

    private final LinkedBlockingQueue<ByteBuf> queue;

    private final AtomicLong currentCacheSize = new AtomicLong(0);

    private volatile boolean isQuit = false;

    public FileReaderThread(List<String> fileList,
                            Reader reader,
                            int maxFileSize,
                            int maxDataSize,
                            int parserThreadSize) {

        this.fileList = fileList;
        this.reader = reader;
        this.MAX_FILE_SIZE = maxFileSize;
        this.MAX_DATA_SIZE = maxDataSize;
        this.PARSER_THREAD_SIZE = parserThreadSize;
        this.queue = new LinkedBlockingQueue<>(MAX_FILE_SIZE);
    }

    @Override
    public void run() {

        if (fileList == null || fileList.isEmpty()) {

            try {
                terminal();
            } catch (InterruptedException e) {

                Logger.error("FileReaderThread在放入结束标记时被打断");
                isQuit = true;
            }

            return;
        }

        try {

            for (String path : fileList) {

                while ((queue.size() >= MAX_FILE_SIZE) || currentCacheSize.get() >= MAX_DATA_SIZE) {

                    try {

                        SleepUitl.milliSeconds(100);
                    } catch (InterruptedException e) {

                        Logger.error(
                                "FileReaderThread等待数据被消费时打断",
                                StringUtil.exToString(e)
                        );
                        break;
                    }
                }

                ByteBuf cache = reader.read(path);
                Logger.info("FileReaderThread开始读取文件", path, "" + cache.readableBytes());

                if (!cache.isReadable()) {
                    Logger.warn(
                            "FileReaderThread读取文件时，文件内容为空",
                            path
                    );
                    continue;
                }

                currentCacheSize.addAndGet(cache.readableBytes());
                queue.offer(cache);
            }
        } catch (Exception e) {

            Logger.error(StringUtil.exToString(e));
        } finally {

            try {

                if (!isInterrupted()) {
                    terminal();
                }
            } catch (InterruptedException e) {

                Logger.error(StringUtil.exToString(e));
            }

            isQuit = true;
        }
    }

    private void terminal() throws InterruptedException {

        isQuit = true;
        finish();
    }

    private void finish() throws InterruptedException {

        for (int i = 0; i < PARSER_THREAD_SIZE; i++) {
            queue.put(END_FLAG);
        }
    }

    public ByteBuf pollData() {

        ByteBuf data = queue.poll();

        if (data != null) {
            currentCacheSize.addAndGet(-data.readableBytes());
        }

        return data;
    }


}
