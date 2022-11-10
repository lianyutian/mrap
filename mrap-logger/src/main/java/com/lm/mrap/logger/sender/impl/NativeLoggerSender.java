package com.lm.mrap.logger.sender.impl;

import com.lm.mrap.logger.LogExceptions;
import com.lm.mrap.logger.LogSaveStrategy;
import com.lm.mrap.logger.sender.LoggerSender;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author liming
 * @version 1.0
 * @description: 将日志写入本地磁盘文件
 * @date 2022/10/28 下午3:32
 */
public class NativeLoggerSender implements LoggerSender {

    private static final Set<OpenOption> OPTION_CONFIG = new HashSet<>();

    private static final FileAttribute<?> FILE_ATTRIBUTE;

    static {

        OPTION_CONFIG.add(StandardOpenOption.APPEND);
        OPTION_CONFIG.add(StandardOpenOption.CREATE);
        OPTION_CONFIG.add(StandardOpenOption.WRITE);
        OPTION_CONFIG.add(StandardOpenOption.SYNC);

        Set<PosixFilePermission> permissions = new HashSet<>();
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);

        FILE_ATTRIBUTE = PosixFilePermissions.asFileAttribute(permissions);

    }

    private final String dirPath;

    private final String name;

    private final LogSaveStrategy logSaveStrategy;

    private final FileSystem fileSystem = FileSystems.getDefault();

    private FileChannel fileChannel = null;

    private long currentFilePosition = 0L;

    public NativeLoggerSender(String dirPath, String name, LogSaveStrategy logSaveStrategy) throws IOException {

        File dirFile = new File(dirPath);

        if (!dirFile.isDirectory()) {
            dirFile.mkdirs();
        }

        this.dirPath = dirPath;
        this.name = name;
        this.logSaveStrategy = logSaveStrategy;

        fileChannel = getFileChannel();
        currentFilePosition = fileChannel.size();
    }

    private FileChannel getFileChannel() throws IOException {

        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("windows")) {
            return fileSystem.provider().newFileChannel(
                    fileSystem.getPath(dirPath, logSaveStrategy.getStrategyExchange().newName(name)),
                    OPTION_CONFIG
            );
        } else {
            return fileSystem.provider().newFileChannel(
                    fileSystem.getPath(dirPath, logSaveStrategy.getStrategyExchange().newName(name)),
                    OPTION_CONFIG,
                    FILE_ATTRIBUTE
            );
        }
    }

    @Override
    public void write(List<ByteBuf> datas) {

        if (logSaveStrategy.getStrategyExchange().isChanged()) {

            try {
                fileChannel.close();
                fileChannel = getFileChannel();
                currentFilePosition = 0;
            } catch (IOException e) {
                throw new LogExceptions.WriteFaildeException("关闭日志文件，或者新建日志文件时出现错误");
            }
        }

        CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer(datas.size());

        int bytesSize = 0;

        for (ByteBuf compent : datas) {
            compent.writeByte('\n');
            bytesSize += compent.readableBytes();
            compositeByteBuf.addComponent(true, compent);
        }

        try {
            compositeByteBuf.readBytes(fileChannel, currentFilePosition, bytesSize);
            fileChannel.force(false);
        } catch (IOException e) {
            throw new LogExceptions.WriteFaildeException("写入日志失败", e);
        }
    }

    @Override
    public void close() {

        if (fileChannel != null) {

            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
