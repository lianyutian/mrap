package com.lm.mrap.sync.thread.kvwriter;

import com.lm.mrap.sync.thread.fileparser.KeyValue;

public interface KVWriter {
    void add(KeyValue keyValue);

    void commit();

    void close();

    void closeIgnore();

    long getCount();

    String tableName();
}
