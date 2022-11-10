package com.lm.mrap.logger;

import com.lm.mrap.common.utils.SleepUitl;
import com.lm.mrap.logger.config.LoggerConfig;
import com.lm.mrap.logger.sender.LoggerSender;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author liming
 * @version 1.0
 * @description: 负责收集日志，并且每隔一个时间段执行一次写入操作
 * @date 2022/10/27 下午6:03
 */
public class LoggerCollector extends Thread {

    private final LoggerSender primarySender;

    private final LoggerSender backupSender;

    private final int writeInterval;

    private final long bufferSize;

    private final long maxBufferSize;

    private final AtomicLong currentCachedSize = new AtomicLong(0L);

    private final ConcurrentLinkedQueue<ByteBuf> buffQueue = new ConcurrentLinkedQueue<>();

    private volatile boolean isQuit;

    public LoggerCollector(long bufferSize, long maxBufferSize, int writeInterval, LoggerSender primarySender, LoggerSender backupSender) {
        this.bufferSize = bufferSize;
        this.maxBufferSize = maxBufferSize;
        this.writeInterval = writeInterval;
        this.primarySender = primarySender;
        this.backupSender = backupSender;
    }

    public void send(ByteBuf log) {
        if (log != null && currentCachedSize.get() < maxBufferSize) {
            buffQueue.add(log);
            currentCachedSize.addAndGet(log.readableBytes());
        }
    }

    private void write(List<ByteBuf> buffers) {
        try {
            primarySender.write(buffers);
        } catch (LogExceptions.WriteFaildeException e) {
            e.printStackTrace();

            try {
                backupSender.write(buffers);
            } catch (LogExceptions.WriteFaildeException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void run() {

        long currentSendedSize = 0L;

        List<ByteBuf> currentSendedBuffers = new ArrayList<>();

        long currentSendedTime = System.currentTimeMillis() + writeInterval;

        while (!isQuit || !buffQueue.isEmpty()) {

            ByteBuf data = buffQueue.poll();

            boolean isSend = !currentSendedBuffers.isEmpty() &&
                    (currentSendedSize >= bufferSize) || System.currentTimeMillis() >= currentSendedTime;
            if (isSend)  {

                write(currentSendedBuffers);

                currentSendedBuffers = new ArrayList<>();
                currentCachedSize.addAndGet(-currentSendedSize);
                currentSendedSize = 0;
                currentSendedTime = System.currentTimeMillis() + writeInterval;

            }

            if (data == null) {

                try {
                    SleepUitl.milliSeconds(LoggerConfig.DEFAULT_QUEUE_WAIT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                continue;
            }

            currentSendedSize += data.readableBytes();
            currentSendedBuffers.add(data);

        }

        if (!currentSendedBuffers.isEmpty()) {
            write(currentSendedBuffers);
        }
        isQuit = false;

    }

    public void quit() {

        isQuit = true;

        while (isQuit) {
            try {
                SleepUitl.milliSeconds(LoggerConfig.DEFAULT_QUEUE_WAIT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
