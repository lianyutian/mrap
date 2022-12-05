package com.lm.mrap.sync.thread.fileparser;

import io.netty.buffer.ByteBuf;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/17 上午10:59
 */
public interface ParserFactory {

    Parser create(FileSystem fileSystem, ByteBuf rowData, int batchSize) throws IOException;
}
