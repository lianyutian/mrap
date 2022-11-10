package com.lm.mrap.sync.thread.filereader;

import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import com.lm.mrap.sync.utils.HdfsDealUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;

import java.io.IOException;


/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/4 下午5:37
 */
public class HDFSReader implements Reader {

    private final HdfsDealUtil hdfsDealUtil;

    public HDFSReader(Configuration configuration) {

        try {
            hdfsDealUtil = new HdfsDealUtil(configuration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteBuf read(String path) {

        try (FSDataInputStream fsDataInputStream = (FSDataInputStream) hdfsDealUtil.getFileInputStream(path)){

            if (path == null || !hdfsDealUtil.exists(path)) {

                Logger.error(
                        "HDFSReader读取文件时",
                        StringUtil.nullString(path),
                        "这个文件不存在"
                );

                return Unpooled.EMPTY_BUFFER;
            }

            int fileLen = fsDataInputStream.available();

            if (fileLen == 0) {
                return Unpooled.EMPTY_BUFFER;
            }

            Logger.info("HDFSReader正在读取文件", path);

            byte[] cacheArray = new byte[fileLen];

            fsDataInputStream.readFully(0, cacheArray);

            Logger.info("HDFSReader读取完成", path, Integer.toString(fileLen));

            return Unpooled.wrappedBuffer(cacheArray);
        } catch (IOException e) {

            Logger.error(
                    "HDFSReader读取文件时出现异常",
                    path,
                    StringUtil.nullString(path),
                    StringUtil.exToString(e)
            );

            return Unpooled.EMPTY_BUFFER;
        }
    }

    @Override
    public void close() {
        hdfsDealUtil.close();
    }
}
