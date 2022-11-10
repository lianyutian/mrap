package com.lm.mrap.sync.thread.filereader;

import io.netty.buffer.ByteBuf;

/**
 * @author liming
 * @version 1.0
 * @description: reader 从一个文件中读取数据进入 ByteBuf
 * @date 2022/11/4 下午5:31
 */
public interface Reader {

    ByteBuf read(String path);

    void close();
}
