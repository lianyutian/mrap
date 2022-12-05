package com.lm.mrap.sync.thread.fileparser;

import com.lm.mrap.logger.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.io.hfile.CacheConfig;
import org.apache.hadoop.hbase.io.hfile.HFile;
import org.apache.hadoop.hbase.io.hfile.HFileScanner;

import java.io.IOException;
import java.util.UUID;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/17 上午11:01
 */
public class HFileParser implements Parser {

    private final ByteBuf rowData;

    private final MemoryFileSystem fileSystem;

    private final String uuid = UUID.randomUUID().toString();

    private final Configuration configuration;

    private HFile.Reader reader;

    private HFileScanner scanner;

    private Path path;

    private byte[] currentArray;

    private final int batchSize;

    private BatchData tempBatchData;

    private long count = 0;

    public HFileParser(ByteBuf rowData, FileSystem fileSystem, Configuration configuration, int batchSize) throws IOException {
        this.rowData = rowData;
        this.fileSystem = (MemoryFileSystem) fileSystem;
        this.configuration = configuration;
        this.batchSize = batchSize;

        readHFile();
    }

    private void readHFile() throws IOException {

        path = new Path("memory://NativeMemory/" + uuid);

        fileSystem.put(path, rowData);

        reader = HFile.createReader(
                fileSystem,
                path,
                new CacheConfig(configuration),
                true,
                configuration
        );

        reader.getHFileInfo();

        scanner = reader.getScanner(configuration, false, true);

        if (!scanner.seekTo()) {

            Logger.error(
                    "Parser current hfile faild",
                    path.toString()
            );
            throw new IllegalArgumentException();
        }
    }


    @Override
    public BatchData nextBatch() throws IOException {

        int batchSize = 0;
        byte[] array;
        BatchData batchData = tempBatchData;

        if (!scanner.isSeeked()) {

            Logger.info(
                    "HFile 文件内容已经解析完成", rowData.writerIndex() + ""
            );
            return null;
        }

        do {

            if (batchSize >= this.batchSize) {
                break;
            }

            Cell cell = scanner.getCell();

            array = cell.getValueArray();

            if (currentArray == null) {
                currentArray = array;
            }

            if (batchData == null) {
                batchData = new BatchData(Unpooled.wrappedBuffer(currentArray));
            }

            int keyOff = cell.getRowOffset();
            int valueOff = cell.getValueOffset();

            int keyLength = cell.getRowLength();
            int valueLenght = cell.getValueLength();

            if (currentArray != array) {

                tempBatchData = new BatchData(Unpooled.wrappedBuffer(array));
                tempBatchData.putInfo(
                        keyOff,
                        valueOff,
                        keyLength,
                        valueLenght
                );

                currentArray = array;
                break;
            }

            batchData.putInfo(
                    keyOff,
                    valueOff,
                    keyLength,
                    valueLenght
            );

            batchSize += keyLength;
            batchSize += valueLenght;

            count++;
        } while (scanner.next());

        return batchData;
    }


    @Override
    public void clean() throws IOException {

        if (reader != null) {
            reader.close();
        }

        fileSystem.remove(path);
    }

    @Override
    public long getCount() {
        return count;
    }
}
