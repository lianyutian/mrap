package com.lm.mrap.sync.thread.filereader;

import org.apache.hadoop.conf.Configuration;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/4 下午5:31
 */
public enum ReaderType {
    /**
     * 读取本地
     */
    LOCAL(new LocalFileReader()),

    /**
     * 读取 HDFS
     */
    HDFS(new HDFSReader(new Configuration()));

    private final Reader reader;

    ReaderType(Reader reader) {
        this.reader = reader;
    }

    public Reader reader() {
        return reader;
    }
}
