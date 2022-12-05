package com.lm.mrap.sync.thread.fileparser;

import io.netty.buffer.ByteBuf;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.HBaseConfiguration;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: 解析文件类型
 * @date 2022/11/17 上午10:54
 */
public enum FileParserType {
    /**
     * HFILE格式文件
     */
    HFILE(
            (fileSystem, rowData, batchSize) -> new HFileParser(
                    rowData,
                    fileSystem,
                    HBaseConfiguration.create(),
                    batchSize
            )
    );

    private final ParserFactory parserFactory;

    FileParserType(ParserFactory parserFactory) {

        this.parserFactory = parserFactory;
    }

    public Parser getParser(FileSystem fileSystem, ByteBuf rowData, int batchSize) throws IOException {

        return parserFactory.create(fileSystem, rowData, batchSize);
    }
}
