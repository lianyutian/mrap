package com.lm.mrap.sync.thread.fileparser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Iterator;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/15 下午2:54
 */
public class BatchData implements Iterator<KeyValue> {

    private final ByteBuf rawData;

    private final ByteBuf infoData;

    public BatchData(ByteBuf rawData) {
        this.rawData = rawData;
        this.infoData = Unpooled.buffer();
    }

    @Override
    public boolean hasNext() {
        return infoData.isReadable();
    }

    @Override
    public KeyValue next() {

        int keyOffset = infoData.readInt();
        int valueOffset = infoData.readInt();
        short keyLength = infoData.readShort();
        int valueLength = infoData.readInt();

        byte[] key = new byte[keyLength];
        byte[] value = new byte[valueLength];

        rawData.getBytes(keyOffset, key);
        rawData.getBytes(valueOffset, value);

        return new KeyValue(key, value);
    }

    void putInfo(int keyOffset, int valueOffset, int keyLength, int valueLength) {

        infoData.writeInt(keyOffset);
        infoData.writeInt(valueOffset);
        infoData.writeShort(keyLength);
        infoData.writeShort(valueLength);
    }
}
