package com.lm.mrap.sync.thread.fileparser;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: 文件被读取到内存之后需要一个解析器来解析文件的内容，由于可能会有各种各样不同的文件格式，需要使用不同的解析器来解析它。
 * 目前同步程序同步的是规定的 keyvalue 数据，解析的目的就是将文件内容中的 keyvalue对 全部取出来。
 * @date 2022/11/17 上午10:55
 */
public interface Parser {

    BatchData nextBatch() throws IOException;

    void clean() throws IOException;

    long getCount();
}
